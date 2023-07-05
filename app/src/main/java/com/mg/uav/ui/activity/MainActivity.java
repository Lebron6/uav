package com.mg.uav.ui.activity;

import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dji.mapkit.core.camera.DJICameraUpdateFactory;
import com.dji.mapkit.core.maps.DJIMap;
import com.dji.mapkit.core.models.DJIBitmapDescriptorFactory;
import com.dji.mapkit.core.models.DJILatLng;
import com.dji.mapkit.core.models.DJILatLngBounds;
import com.dji.mapkit.core.models.annotations.DJIMarker;
import com.dji.mapkit.core.models.annotations.DJIMarkerOptions;
import com.dji.mapkit.core.models.annotations.DJIPolyline;
import com.dji.mapkit.core.models.annotations.DJIPolylineOptions;
import com.mg.uav.R;
import com.mg.uav.api.BaseUrl;
import com.mg.uav.app.DJIApp;
import com.mg.uav.base.BaseActivity;
import com.mg.uav.callback.BatteryAStateCallback;
import com.mg.uav.callback.BatteryBStateCallback;
import com.mg.uav.callback.ConfirmLandingCallBack;
import com.mg.uav.callback.DiagnosticsInformationCallback;
import com.mg.uav.callback.DownlinkSignalQualityCallback;
import com.mg.uav.callback.FlightControllerStateCallBack;
import com.mg.uav.callback.LiveShowStatusCallback;
import com.mg.uav.callback.UplinkSignalQualityCallback;
import com.mg.uav.client.ChannelCache;
import com.mg.uav.client.SocketClient;
import com.mg.uav.constant.Constant;
import com.mg.uav.constant.SocketConfig;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.entity.FlightInfo;
import com.mg.uav.entity.MissionUploadData;
import com.mg.uav.entity.Movement;
import com.mg.uav.entity.StreamStatus;
import com.mg.uav.entity.StreamValues;
import com.mg.uav.global.DataCache;
import com.mg.uav.manager.LocV1MissionManager;
import com.mg.uav.manager.LocV2MissionManager;
import com.mg.uav.tools.PreferenceUtils;
import com.mg.uav.tools.Utils;
import com.mg.uav.ui.view.LongTouchBtn;
import com.mg.uav.ui.view.ResizeAnimation;
import com.mg.uav.ui.view.VideoFeedView;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dji.common.airlink.PhysicalSource;
import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.airlink.AirLink;
import dji.sdk.airlink.OcuSyncLink;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.RTK;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import dji.ux.widget.MapWidget;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mg.uav.constant.Constant.CONFIRM_LANDING;
import static com.mg.uav.constant.Constant.FLIGHT_CONTROLLER_STATE_CHANGE;
import static com.mg.uav.constant.Constant.RESTART_LIVE;
import static com.mg.uav.constant.Constant.START_LIVE;
import static com.mg.uav.constant.Constant.VISUAL_ANGLE_TYPE;
import static dji.sdk.codec.DJICodecManager.VideoSource.CAMERA;
import static dji.sdk.codec.DJICodecManager.VideoSource.FPV;
import static dji.sdk.sdkmanager.LiveVideoBitRateMode.AUTO;
import static dji.sdk.sdkmanager.LiveVideoResolution.VIDEO_RESOLUTION_1920_1080;
import static dji.sdk.sdkmanager.LiveVideoResolution.VIDEO_RESOLUTION_480_360;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    private VideoFeedView mTextureView;
    private TextView tvLiveUrl, tv_stream_fps, tv_stream_rate;
    private MapWidget mapWidget;
    private ViewGroup parentView;
    private RelativeLayout layoutPreviewerContainer;
    private boolean isMapMini = true;
    private int deviceWidth;
    private int deviceHeight;
    private Button btn_restart_live_show, btn_stop_live;
    private LongTouchBtn btnUp, btnDown, btnForward, btnBackward, btnLeft, btnRight;
    private TextView tv_sped, tv_v_sped, tv_wind_sped, tv_alt;
    private LinearLayout layout_chose_mission, layout_mission_start, layout_change_preview;

    public static final String MISSION_JSON = "mission_json";
    private MissionUploadData missionJson;
    DJIMap aMap;
    DJIPolylineOptions polylineOptions;
    ArrayList<DJILatLng> latLngList = new ArrayList<DJILatLng>();//记录每一个Marker的经纬度
    List<MissionUploadData.Waypoint> uploadMissionWayoints = new ArrayList<>();

    DJIPolyline djiPolyline;
    private List<DJIMarker> markerList = new ArrayList<>();//记录每一个Marker
    private List<DJIPolyline> djiPolylines = new ArrayList<>();//记录每一条Line
    SocketClient socketClient;

    public static void actionStart(Context context, String missionJson) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MISSION_JSON, missionJson);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        connectSocket();
        initDeviceSize();
        initMap(savedInstanceState);
        initUAVMovement();
        //每秒推送数据
        initTimer();
        //每秒刷新码率/FPS
        //        每隔一秒更新码率
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 1000); // 在初始化方法里.
        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIApp.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initLiveStreamManager();
            }
        }, 5000);
    }

    Handler mHandler;


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            showInfo();
            try {
                mHandler.postDelayed(this, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //检测无人机连接状态
    private void showInfo() {
        if (liveStreamManager != null) {
            tv_stream_fps.setText("fps:" + liveStreamManager.getLiveVideoFps() + "");
            tv_stream_rate.setText("rate:" + liveStreamManager.getLiveVideoBitRate() + "");
        }

    }


    private void connectSocket() {
        if (socketClient == null) {
            socketClient = new SocketClient();
        }
        if (PreferenceUtils.getInstance().getSocketPort() != 0 && !TextUtils.isEmpty(PreferenceUtils.getInstance().getSocketHost()) && !TextUtils.isEmpty(PreferenceUtils.getInstance().getFlyNumber())) {
            socketClient.setClientConfig(PreferenceUtils.getInstance().getSocketHost(), PreferenceUtils.getInstance().getSocketPort(), PreferenceUtils.getInstance().getFlyNumber());
            try {
                socketClient.connect();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e("socket connect error:" + e.toString());
            }
        } else {
            Toast.makeText(this, "socket 地址异常,请检查配置", Toast.LENGTH_SHORT).show();
        }

    }

    private void initDeviceSize() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        display.getRealSize(outPoint);
        deviceHeight = outPoint.y;
        deviceWidth = outPoint.x;
    }

    private void initMap(Bundle savedInstanceState) {
        mapWidget.initAMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {
                aMap = map;
                map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick(mapWidget);
                    }
                });
                map.getUiSettings().setZoomControlsEnabled(false);
            }
        });
        mapWidget.onCreate(savedInstanceState);
        setMapWidgetInfo();
    }

    private void setMapWidgetInfo() {
        mapWidget.setFlightPathVisible(true);
        mapWidget.setHomeVisible(true);
        mapWidget.setHomeBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mapWidget.setAircraftBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_next_point));
        mapWidget.setFlightPathColor(getResources().getColor(R.color.teal_200));
        mapWidget.clearFlightPath();
        mapWidget.setDirectionToHomeVisible(false);
    }

    private void initView() {
        mapWidget = findViewById(R.id.map_widget);

        tvLiveUrl = findViewById(R.id.tv_live_url);
        tv_stream_fps = findViewById(R.id.tv_stream_fps);
        tv_stream_fps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLiveStreamManager();
            }
        });
        tv_stream_rate = findViewById(R.id.tv_stream_rate);
        mTextureView = findViewById(R.id.video_previewer_surface);
        mTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClick(mTextureView);
            }
        });
        parentView = (ViewGroup) findViewById(R.id.root_view);
        layoutPreviewerContainer = (RelativeLayout) findViewById(R.id.layout_previewer_container);
        btnUp = (LongTouchBtn) findViewById(R.id.btn_up);
        btnDown = (LongTouchBtn) findViewById(R.id.btn_down);
        btnLeft = (LongTouchBtn) findViewById(R.id.btn_left);
        btnRight = (LongTouchBtn) findViewById(R.id.btn_right);
        btnForward = (LongTouchBtn) findViewById(R.id.btn_forward);
        btnBackward = (LongTouchBtn) findViewById(R.id.btn_backward);
        layout_chose_mission = findViewById(R.id.layout_mission_chose);
        layout_change_preview = findViewById(R.id.layout_change_preview);
        layout_mission_start = findViewById(R.id.layout_start_mission);
        tv_sped = findViewById(R.id.tv_sped);
        tv_v_sped = findViewById(R.id.tv_v_sped);
        tv_alt = findViewById(R.id.tv_alt);
        tv_wind_sped = findViewById(R.id.tv_wind_sped);
        layout_change_preview.setOnClickListener(this);
        layout_mission_start.setOnClickListener(this);
        layout_chose_mission.setOnClickListener(this);

        btn_restart_live_show = findViewById(R.id.btn_restart_live_show);
        btn_restart_live_show.setOnClickListener(this);
        btn_stop_live = findViewById(R.id.btn_stop_live);
        btn_stop_live.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_mission_chose:
                canFlyOnResume = false;
                MissionListActivity.actionStartForResult(this);
                break;
            case R.id.layout_start_mission:
                startMission();
                break;
            case R.id.layout_change_preview:

//                changePreView();
                break;
            case R.id.btn_restart_live_show:
                reStartLiveShow();
                break;
            case R.id.btn_stop_live:
                stopLiveShow();
                break;
        }
    }

    //改变当前视角
    private void changePreView() {
        if (codecManager != null) {
            if (Movement.getInstance().getCurrentView() == 1) {//为FPV时切换云台
                codecManager.switchSource(CAMERA);
                Movement.getInstance().setCurrentView(0);

            } else {
                codecManager.switchSource(FPV);
                Movement.getInstance().setCurrentView(1);
            }
        }
    }

    private void startMission() {
        if (missionJson != null && missionJson.getWaypoints() != null && missionJson.getWaypoints().size() >= 2) {
            Logger.e("导入航线起飞：" + new Gson().toJson(missionJson));
            Toast.makeText(MainActivity.this, "航点数目:" + missionJson.getWaypoints().size(), Toast.LENGTH_SHORT).show();
            if (DJIApp.isM300Product()) {
                LocV2MissionManager.getInstance().createWayPointV2Mission(missionJson);
            } else {
                LocV1MissionManager.getInstance().createWayPointMission(missionJson);
            }
        } else {
            Toast.makeText(MainActivity.this, "未选择航线或航点过少", Toast.LENGTH_SHORT).show();
        }
    }

    VideoFeeder videoFeeder;
    DJICodecManager codecManager;
    FlightController flightController;
    AirLink airLink;
    List<Battery> batteries;
    Battery batteryA;
    Battery batteryB;
    Aircraft aircraft;
    LiveStreamManager liveStreamManager;
    OcuSyncLink ocuSyncLink;


    private void initUAVMovement() {
        aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null && aircraft.isConnected()) {
            //码率
            initPreviewer();
            //飞控参数、RTK开关、避障等
            initFlightController();
            //遥控器、图传等
            initAirLink();
            //图传链路
            initOcuSyncLink();
            //电池信息
            initBattery();
            //警告信息
            initDiagnosticsInfomation();
            //推流
//            initLiveStreamManager();

        } else {
            Toast.makeText(MainActivity.this, "aircraft disconnect", Toast.LENGTH_SHORT).show();
        }
    }


    //接收设备连接的广播
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //智能电池硬件连接具有延时,连接后再次初始化
            initVirtualStickMode();

            initUAVMovement();

        }
    };

    private void initVirtualStickMode() {

        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
//                        Toast.makeText(MainActivity.this, djiError == null ? "成功获取控制权" : djiError.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Logger.e("flightController is null 获取控制权失败");
            }
        } else {
            Logger.e("aircraft is null 飞行器未连接");

        }

    }

    private void initTimer() {
        Timer timer = new Timer(true);
        timer.schedule(timerTask, 0, 1000); //0ms后执行，1000ms执行一次
    }

    TimerTask timerTask = new TimerTask() {
        public void run() {
            DataInfo.Message message = DataInfo.Message.newBuilder()
                    .setFlyNum(PreferenceUtils.getInstance().getFlyNumber())
                    .setDataType(DataInfo.Message.DataType.MovementType)
                    .setMovement(Movement.getInstance().myMovement2DataInfoMovement())
                    .build();
            ChannelCache.send(message);

        }
    };

    private void initLiveStreamManager() {
        liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        if (liveStreamManager != null) {
            liveStreamManager.registerListener(new LiveShowStatusCallback());
            if (!liveStreamManager.isStreaming()) {
                String rtmpAddress = DataCache.getInstance().getRtmp_address();
                if (!TextUtils.isEmpty(rtmpAddress)) {
                    tvLiveUrl.setText(rtmpAddress);
                    startLiveShow(rtmpAddress);
                } else {
                    Toast.makeText(this, "no rtmp address!", Toast.LENGTH_SHORT).show();
                }
            } else {
                //开始推流
//                while(!Constant.H264FLAG){
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }

            }
        } else {
            Toast.makeText(this, "no live stream manager!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLiveShow(String url) {
        if (liveStreamManager != null) {
            liveStreamManager.setLiveUrl(url);
            liveStreamManager.setAudioStreamingEnabled(false);
            liveStreamManager.setAudioMuted(false);
            liveStreamManager.setVideoEncodingEnabled(true);
//            liveStreamManager.setLiveVideoBitRate(20);//码率越高，FPS就会越高
            liveStreamManager.setLiveVideoBitRateMode(AUTO);
            liveStreamManager.setLiveVideoResolution(VIDEO_RESOLUTION_1920_1080);//分辨率低，FPS越高
            liveStreamManager.setVideoSource(LiveStreamManager.LiveStreamVideoSource.Primary);
            int result = liveStreamManager.startStream();
            liveStreamManager.setStartTime();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "startLive:" + result, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
//            Toast.makeText(MainActivity.this, "liveStreamManager is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLiveShow() {
        if (liveStreamManager != null) {
            liveStreamManager.stopStream();
//            Toast.makeText(MainActivity.this, "stream stop", Toast.LENGTH_SHORT).show();
        }
    }

    private void initPreviewer() {
        videoFeeder = VideoFeeder.getInstance();
        if (videoFeeder != null) {
            mTextureView.registerLiveVideo(videoFeeder.getPrimaryVideoFeed());
            videoFeeder.setTranscodingDataRate(8f);
        }
    }

    private void initDiagnosticsInfomation() {
        aircraft.setDiagnosticsInformationCallback(new DiagnosticsInformationCallback());
    }

    private void initBattery() {
        batteries = aircraft.getBatteries();
        if (batteries != null && batteries.size() > 0) {
            batteryA = batteries.get(0);
            batteryA.setStateCallback(new BatteryAStateCallback());
            if (batteries.size() > 1) {
                batteryB = batteries.get(1);
                batteryB.setStateCallback(new BatteryBStateCallback());
            }
        } else {
            Logger.e("batteries is null");
        }
    }

    private void initAirLink() {
        airLink = aircraft.getAirLink();
        if (airLink != null) {
            airLink.setUplinkSignalQualityCallback(new UplinkSignalQualityCallback());
            airLink.setDownlinkSignalQualityCallback(new DownlinkSignalQualityCallback());
        }
    }

    private void initOcuSyncLink() {
        if (airLink != null) {
            if (airLink.isOcuSyncLinkSupported()) {
                ocuSyncLink = airLink.getOcuSyncLink();
                if (ocuSyncLink != null) {
                    ocuSyncLink.assignSourceToPrimaryChannel(PhysicalSource.LEFT_CAM, PhysicalSource.FPV_CAM, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                Logger.e("initOcuSyncLink:" + djiError.getDescription());
                            } else {
                                Movement.getInstance().setCurrentView(0);//初始化默认云台视角
                            }
                        }
                    });
                } else {
                    Logger.e("ocuSyncLink is null");
                }
            } else {
                Logger.e("aircraft not supported ocuSyncLink");
            }
        }
    }

    private void initFlightController() {
        flightController = aircraft.getFlightController();
        if (flightController != null) {
            flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
            flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);//相对于自己
            Movement.getInstance().setPlaneWing(flightController.getState().isFlying());
            flightController.setStateCallback(new FlightControllerStateCallBack(flightController, this));
            //测试虚拟飞行
            setVirtualStickFlightControl();

            RTK rtk = flightController.getRTK();
            if (rtk != null) {
                //RKT开关
                rtk.getRtkEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Movement.getInstance().setRtkSign(aBoolean);
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                    }
                });
            }
            //避障
            FlightAssistant flightAssistant = flightController.getFlightAssistant();
            if (flightAssistant != null) {
                //向上避障
                flightAssistant.getUpwardVisionObstacleAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
//                        Movement.getInstance().setPlaneMessage();
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                    }
                });
                //向下避障
                flightAssistant.getLandingProtectionEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                    }
                });

                //水平避障
                flightAssistant.getHorizontalVisionObstacleAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Movement.getInstance().setLevelObstacleAvoidance(aBoolean);
                    }

                    @Override
                    public void onFailure(DJIError djiError) {

                    }
                });
            }
        }
    }

    private void onViewClick(View view) {
        if (view == mTextureView && !isMapMini) {
            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 10, 0);
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, deviceWidth, deviceHeight, Utils.dip2px(this, 144), Utils.dip2px(this, 81), 10);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = true;
        } else if (view == mapWidget && isMapMini) {
            resizeFPVWidget(Utils.dip2px(this, 144), Utils.dip2px(this, 81), 10, 2);
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, Utils.dip2px(this, 144), Utils.dip2px(this, 81), deviceWidth, deviceHeight, 10);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = false;
        }
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) layoutPreviewerContainer.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvParams.leftMargin = margin;
        fpvParams.bottomMargin = margin;
        if (isMapMini) {
            fpvParams.addRule(RelativeLayout.CENTER_IN_PARENT, 10);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        } else {
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 10);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 10);
            fpvParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        }
        layoutPreviewerContainer.setLayoutParams(fpvParams);

        parentView.removeView(layoutPreviewerContainer);
        parentView.addView(layoutPreviewerContainer, fpvInsertPosition);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    private boolean canFlyOnResume = true;

    @Override
    public void onResume() {
        super.onResume();
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        mapWidget.onResume();
        if (canFlyOnResume) {
            String json = getIntent().getStringExtra(MISSION_JSON);
            if (!TextUtils.isEmpty(json)) {
                missionJson = new Gson().fromJson(json, MissionUploadData.class);
                Logger.e(json);
                fly();
            }
        }
    }

    private void fly() {
        Toast.makeText(MainActivity.this, "onResume:起飞", Toast.LENGTH_SHORT).show();
//        aMap.moveCamera(DJICameraUpdateFactory.newCameraPosition(DJICameraPosition.fromLatLngZoom(new DJILatLng(Double.valueOf(missionJson.getWaypoints().get(0).getLatitude()), Double.valueOf(missionJson.getWaypoints().get(0).getLongitude())), 15)));
        polylineOptions = new DJIPolylineOptions();//这里重新初始化,每次导入覆盖旧航线
        mapWidget.clearFlightPath();

        aMap.addPolyline(polylineOptions);
        aMap.clear();
        removeAllPolyLine();
        for (int i = 0; i < missionJson.getWaypoints().size(); i++) {
            DJILatLng latLng = new DJILatLng(Double.valueOf(missionJson.getWaypoints().get(i).getLatitude()),
                    Double.valueOf(missionJson.getWaypoints().get(i).getLongitude()));
            addMarkersToMap(latLng, null);
        }
        moveCameraCenter();
        if (DJIApp.isM300Product()) {
            LocV2MissionManager.getInstance().createWayPointV2Mission(missionJson);
        } else {
            LocV1MissionManager.getInstance().createWayPointMission(missionJson);
        }
    }

    public void addMarkersToMap(DJILatLng djiLatLng, FlightInfo.DataDTO.FlightPointListDTO flightPointListDTO) {

        latLngList.add(djiLatLng);
        DJIMarker marker = aMap.addMarker(new DJIMarkerOptions().position(djiLatLng)
                .icon(DJIBitmapDescriptorFactory.fromBitmap(convertViewToBitmap())).draggable(true));
        markerList.add(marker);

        polylineOptions.add(djiLatLng).width(5).setDashed(false).color(getResources().getColor(R.color.blue));
        djiPolyline = aMap.addPolyline(polylineOptions);
        djiPolylines.add(djiPolyline);
        if (flightPointListDTO != null) {
            //为初始化的航线Bean添加航点信息
            MissionUploadData.Waypoint waypoint = new MissionUploadData.Waypoint();
            waypoint.setLatitude(djiLatLng.latitude + "");
            waypoint.setLongitude(djiLatLng.longitude + "");
            waypoint.setAltitude(String.valueOf(flightPointListDTO.getAltitude()));
            waypoint.setSpeed(String.valueOf(flightPointListDTO.getSpeed()));
            waypoint.setAircraftYawAngle(String.valueOf(flightPointListDTO.getAircraftYawAngle()));
            waypoint.setFlightPointType(String.valueOf(flightPointListDTO.getFlightPointType()));
            waypoint.setTurnMode(String.valueOf(flightPointListDTO.getTurnMode()));
            waypoint.setFlightPointId(String.valueOf(flightPointListDTO.getFlightPointId()));
            uploadMissionWayoints.add(waypoint);
            missionJson.setWaypoints(uploadMissionWayoints);
        }
    }

    //Marker全部展示且取中心点坐标居地图中
    private void moveCameraCenter() {
        if (markerList != null) {
            DJILatLngBounds.Builder boundsBuilder = new DJILatLngBounds.Builder();//存放所有点的经纬度
            for (int i = 0; i < markerList.size(); i++) {
                boundsBuilder.include(markerList.get(i).getPosition());//把所有点都include进去（LatLng类型）
            }
            aMap.animateCamera(DJICameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 15, 20));//第二个参数为四周留空宽度
        }
    }

    private void removeAllPolyLine() {
        if (djiPolylines != null) {
            for (int i = 0; i < djiPolylines.size(); i++) {
                djiPolylines.get(i).remove();
            }
        }

    }

    /**
     * 自定义Marker样式转为Bitmap
     *
     * @return
     */
    public Bitmap convertViewToBitmap() {
        View view = View.inflate(this, R.layout.view_marker, null);
        LinearLayout layout_marker = view.findViewById(R.id.layout_marker);
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) layout_marker.getLayoutParams();
        linearParams.height = 50;
        linearParams.width = 50;
        layout_marker.setLayoutParams(linearParams);
        TextView textView = (TextView) view.findViewById(R.id.tv_marker_num);
        textView.setTextSize(8);
        textView.setText(latLngList.size() + "");
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    @Override
    public void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListener();
        mapWidget.onDestroy();
        if (socketClient != null) {
            socketClient.disconnect();
        }
        stopLiveShow();
    }

    private void removeListener() {
        if (videoFeeder != null) {
            videoFeeder.getPrimaryVideoFeed().addVideoDataListener(null);
        }
        if (codecManager != null) {
            codecManager.cleanSurface();
            codecManager = null;
        }
        if (flightController != null) {
            flightController.setStateCallback(null);
        }
        if (batteryA != null) {
            batteryA.setStateCallback(null);
        }
        if (batteryB != null) {
            batteryB.setStateCallback(null);
        }
        if (aircraft != null) {
            aircraft.setDiagnosticsInformationCallback(null);
        }
        if (airLink != null) {
            airLink.setUplinkSignalQualityCallback(null);
            airLink.setDownlinkSignalQualityCallback(null);
        }
        if (liveStreamManager != null) {
            liveStreamManager.registerListener(null);
        }
//        if (DJIApp.getWaypointMissionOperator() != null) {
//            DJIApp.getWaypointMissionOperator().addWaypointEventListener(null);
//            DJIApp.getWaypointMissionOperator().addActionListener(null);
//        }
        unregisterReceiver(mReceiver);
    }


    private void setVirtualStickFlightControl() {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                btnUp.setOnLongTouchListener(new LongTouchBtn.LongTouchListener() {
                    @Override
                    public void onLongTouch() {
                        flightController.sendVirtualStickFlightControlData(
                                new FlightControlData(
                                        0, 0, 0, 4
                                ), new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                }
                        );
                    }
                }, 200);
                btnDown.setOnLongTouchListener(new LongTouchBtn.LongTouchListener() {
                    @Override
                    public void onLongTouch() {
                        flightController.sendVirtualStickFlightControlData(
                                new FlightControlData(
                                        0, 0, 0, -4
                                ), new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                }
                        );
                    }
                }, 200);
                btnForward.setOnLongTouchListener(new LongTouchBtn.LongTouchListener() {
                    @Override
                    public void onLongTouch() {
                        flightController.sendVirtualStickFlightControlData(
                                new FlightControlData(
                                        0, 8, 0, 4
                                ), new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                }
                        );
                    }
                }, 200);
                btnBackward.setOnLongTouchListener(new LongTouchBtn.LongTouchListener() {
                    @Override
                    public void onLongTouch() {
                        flightController.sendVirtualStickFlightControlData(
                                new FlightControlData(
                                        0, 4 * -1, 0, 0
                                ), new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                }
                        );
                    }
                }, 200);

                btnLeft.setOnLongTouchListener(new LongTouchBtn.LongTouchListener() {
                    @Override
                    public void onLongTouch() {
                        flightController.sendVirtualStickFlightControlData(
                                new FlightControlData(
                                        -4, 0, 0, 0
                                ), new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                }
                        );
                    }
                }, 200);

                btnRight.setOnLongTouchListener(new LongTouchBtn.LongTouchListener() {
                    @Override
                    public void onLongTouch() {
                        flightController.sendVirtualStickFlightControlData(
                                new FlightControlData(
                                        4, 0, 0, 0
                                ), new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                }
                        );
                    }
                }, 200);

            } else {
                Toast.makeText(this, "flightController is null!", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(this, "aircraft disconnect!", Toast.LENGTH_SHORT).show();

        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onGetMessageAs(String message) {
        switch (message) {
            case RESTART_LIVE:
                Log.e("请求结果", "---002---");
                getLiveStreamStatus();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(String message) {
        switch (message) {
            case VISUAL_ANGLE_TYPE:
                changeFPVOrGimbalView();
                break;
            case CONFIRM_LANDING:
                if (flightController != null) {
                    flightController.confirmLanding(new ConfirmLandingCallBack());
                }
                break;
            case FLIGHT_CONTROLLER_STATE_CHANGE:
                upDataUIInfo();
                break;
            case START_LIVE:
//                initLiveStreamManager();
                break;
        }
    }

    String time;

    public void getLiveStreamStatus() {

        time = String.valueOf(System.currentTimeMillis());
        //restartLive
        reStartLiveShow();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(PreferenceUtils.getInstance().getHttpIp() + "/srs/getPublishStatus?flyNum=yLx3PLn6&time=" + time)
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.body() != null) {
                    StreamStatus streamStatus = new Gson().fromJson(response.body().string(), StreamStatus.class);
                    if (streamStatus.getCode() == 0) {
                        Logger.e("请求结果" + "已推流");
                        return;
                    } else {
                        Logger.e("请求结果不为0" + "推流失败");
                        EventBus.getDefault().post(RESTART_LIVE);
                        return;
                    }
                }
            }
        });
        Logger.e("参数" + PreferenceUtils.getInstance().getFlyNumber() + "----" + time);
        // http

//        try {
//            okhttp3.Response response = client.newCall(request).execute();
//            Logger.e("请求结果" + new Gson().toJson(response));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Call<StreamStatus> call = createRequest().getPublishStatus(PreferenceUtils.getInstance().getFlyNumber(), time);
//        try {
//            StreamStatus response = call.execute().body();
//            Logger.e("请求结果" + new Gson().toJson(response));
//            if (response != null && response.getCode() == 0) {
//                Logger.e("请求结果" + "已推流");
//                return;
//            } else {
//                Logger.e("请求结果不为0" + "推流失败");
//
//                getLiveStreamStatus();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Logger.e("请求结果异常" + e.toString());
//        }
    }


    public void reStartLiveShow() {
        stopLiveShow();
        //开始推流
        String rtmpAddress = DataCache.getInstance().getRtmp_address();
        if (!TextUtils.isEmpty(rtmpAddress)) {
//            tvLiveUrl.setText(rtmpAddress);
            startLiveShow(rtmpAddress);
        }
//        else {
//            Toast.makeText(this, "no rtmp address!", Toast.LENGTH_SHORT).show();
//        }
    }

    //更新飞控视图数据
    private void upDataUIInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_alt.setText(Movement.getInstance().getFlyingHeight() + "");
                tv_sped.setText(Movement.getInstance().getHorizontalSpeed() + "");
                tv_v_sped.setText(Movement.getInstance().getVerticalSpeed() + "");
                tv_wind_sped.setText(Movement.getInstance().getWindSpeed() + "");
            }
        });

    }

    //改变云台视角
    public void changeFPVOrGimbalView() {
        codecManager = mTextureView.getCodecManager();
        if (codecManager != null) {
            DJICodecManager.VideoSource source = CAMERA;
            switch (Movement.getInstance().getCurrentView()) {
                case 1: //FPV视角
                    source = FPV;
                    break;
                case 0: //云台视角
                    source = CAMERA;
                    break;
            }
            codecManager.switchSource(source);
        } else {
            Toast.makeText(this, "切换视角失败,解码器初始化失败", Toast.LENGTH_SHORT).show();

        }
    }

    //删除指定Marker
    private void clearMarkers() {
        //获取地图上所有Marker
        for (int i = 0; i < markerList.size(); i++) {
            DJIMarker marker = markerList.get(i);
            marker.remove();//移除当前Marker

        }
        mapWidget.invalidate();//刷新地图
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_CODE) {
                String flightPathId = data.getStringExtra("flightPathId");
                if (TextUtils.isEmpty(flightPathId)) {
                    Toast.makeText(this, "未选择航线", Toast.LENGTH_SHORT).show();
                    return;
                }
                createRequest().flightInfo(PreferenceUtils.getInstance().getUserToken(), flightPathId).enqueue(new Callback<FlightInfo>() {
                    @Override
                    public void onResponse(Call<FlightInfo> call, Response<FlightInfo> response) {
                        if (response.body().getCode() == 0) {
                            FlightInfo flightInfo = response.body();
                            if (flightInfo != null && flightInfo.getData().getFlightPointList() != null) {
                                clearMarkers();
                                if (markerList != null) {
                                    markerList.clear();
                                }
                                removeAllPolyLine();
                                if (latLngList != null) {
                                    latLngList.clear();
                                }
                                polylineOptions = new DJIPolylineOptions();//这里重新初始化,每次导入覆盖旧航线
                                //重新初始化这条待上传的航线数据，暂时除了航点，其它信息待赋值
                                if (uploadMissionWayoints != null) {
                                    uploadMissionWayoints.clear();
                                }
                                missionJson = new MissionUploadData();
                                for (int i = 0; i < flightInfo.getData().getFlightPointList().size(); i++) {
                                    DJILatLng latLng = new DJILatLng(Double.valueOf(flightInfo.getData().getFlightPointList().get(i).getLatitude()),
                                            Double.valueOf(flightInfo.getData().getFlightPointList().get(i).getLongitude()));
                                    addMarkersToMap(latLng, flightInfo.getData().getFlightPointList().get(i));
                                }
                                missionJson.setAltitude(String.valueOf(flightInfo.getData().getAltitude()));
                                missionJson.setName(flightInfo.getData().getName());
                                missionJson.setUavId(flightInfo.getData().getUavId());
                                missionJson.setSpeed(String.valueOf(flightInfo.getData().getSpeed()));
                                missionJson.setFinishedAction(String.valueOf(flightInfo.getData().getFinishedAction()));
                                missionJson.setFlightPathId(flightInfo.getData().getFlightPathId());
                                mapWidget.clearFlightPath();

//                                setMapWidgetInfo();
                                moveCameraCenter();


                            }
                        } else {
                            Toast.makeText(MainActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onFailure(Call<FlightInfo> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "网络异常:导入航线失败" + t.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
