package com.mg.uav.ui.activity;

import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.services.core.LatLonPoint;
import com.dji.mapkit.core.callback.MapScreenShotListener;
import com.dji.mapkit.core.camera.DJICameraUpdateFactory;
import com.dji.mapkit.core.maps.DJIMap;
import com.dji.mapkit.core.models.DJIBitmapDescriptorFactory;
import com.dji.mapkit.core.models.DJICameraPosition;
import com.dji.mapkit.core.models.DJILatLng;
import com.dji.mapkit.core.models.DJILatLngBounds;
import com.dji.mapkit.core.models.annotations.DJIMarker;
import com.dji.mapkit.core.models.annotations.DJIMarkerOptions;
import com.dji.mapkit.core.models.annotations.DJIPolyline;
import com.dji.mapkit.core.models.annotations.DJIPolylineOptions;
import com.mg.uav.R;
import com.mg.uav.api.BaseUrl;
import com.mg.uav.base.BaseActivity;
import com.mg.uav.entity.FlightInfo;
import com.mg.uav.entity.MissionUploadData;
import com.mg.uav.tools.PreferenceUtils;
import com.mg.uav.tools.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import dji.ux.widget.MapWidget;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MissionActivity extends BaseActivity implements View.OnClickListener {

    private MapWidget mapWidget;
    DJIMap aMap;
    DJIPolylineOptions polylineOptions;
    DJIPolyline djiPolyline;
    List<LatLonPoint> latLonPoints = new ArrayList<LatLonPoint>();    //用于计算航线长度
    private List<DJIMarker> markerList = new ArrayList<>();//记录每一个Marker
    private List<DJIPolyline> djiPolylines = new ArrayList<>();//记录每一条Line
    ArrayList<DJILatLng> latLngList = new ArrayList<DJILatLng>();//记录每一个Marker的经纬度
    //将要上传的航线数据
    MissionUploadData missionUploadData;
    List<MissionUploadData.Waypoint> uploadMissionWayoints = new ArrayList<>();
    private RelativeLayout layout_import, layout_clear, layout_exit, layout_start_fly;
    private TextView tv_mission_length, tv_mission_time, tv_mission_point_num;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MissionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        initMap(savedInstanceState);
        initView();
        initMissionUploadData();
    }


    /**
     * 进入航线规划界面后初始化一条不包含航点列表的航线信息，用于航线/航点的编辑、添加、上传
     */
    private void initMissionUploadData() {
        missionUploadData = new MissionUploadData();
    }

    private void initView() {
        tv_mission_length = findViewById(R.id.tv_mission_length);
        tv_mission_time = findViewById(R.id.tv_mission_time);
        tv_mission_point_num = findViewById(R.id.tv_mission_point_num);

        layout_import = findViewById(R.id.layout_import);
        layout_exit = findViewById(R.id.layout_exit);
        layout_start_fly = findViewById(R.id.layout_fly);
        layout_clear = findViewById(R.id.layout_clear_map);

        layout_import.setOnClickListener(this);
        layout_exit.setOnClickListener(this);
        layout_start_fly.setOnClickListener(this);
        layout_clear.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_import:
                MissionListActivity.actionStartForResult(this);
                break;
            case R.id.layout_fly:
                saveAndFly();
                break;
            case R.id.layout_clear_map:
                if (uploadMissionWayoints != null) {
                    uploadMissionWayoints.clear();
                }
                clearMarkers();
                if (markerList != null) {
                    markerList.clear();
                }
                if (latLngList != null) {
                    latLngList.clear();
                }
                if (latLonPoints != null) {
                    latLonPoints.clear();
                }
                removeAllPolyLine();
                polylineOptions = new DJIPolylineOptions();//这里重新初始化,每次导入覆盖旧航线
                missionUploadData = new MissionUploadData();
                aMap.addPolyline(polylineOptions);
//                aMap.clear();
                tv_mission_time.setText("N/A");
                tv_mission_length.setText("N/A");
                tv_mission_point_num.setText("N/A");
                break;
            case R.id.layout_exit:
                finish();
                break;
        }
    }

    /**
     * 保存并起飞
     */
    private void saveAndFly() {
        if (uploadMissionWayoints == null || uploadMissionWayoints.size() < 2
        ) {
            Toast.makeText(this, "航点数目不得小于2", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(missionUploadData.getAltitude()) ||
                TextUtils.isEmpty(missionUploadData.getName()) ||
                TextUtils.isEmpty(missionUploadData.getSpeed())) {
            Toast.makeText(this, "请完善航线信息", Toast.LENGTH_SHORT).show();
            MissionEditActivity.actionStartForResult(this, new Gson().toJson(missionUploadData));
        } else {
            Toast.makeText(MissionActivity.this,"起飞",Toast.LENGTH_SHORT).show();
            MainActivity.actionStart(this, new Gson().toJson(missionUploadData));
        }
    }

    private void initMap(Bundle savedInstanceState) {
        mapWidget = findViewById(R.id.map_widget);
        polylineOptions = new DJIPolylineOptions();
        mapWidget.initAMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {
                aMap = map;
                map.setOnMapClickListener(onMapClickListener);
                map.setOnMarkerDragListener(onMarkerDragListener);
                map.setOnMarkerClickListener(onMarkerClickListener);
                map.getUiSettings().setZoomControlsEnabled(false);//禁用右下角地图视角放大缩小
            }
        });
        mapWidget.onCreate(savedInstanceState);
        setMapWidgetInfo();
    }

    private void setMapWidgetInfo() {
        mapWidget.setFlightPathVisible(true);
        mapWidget.setHomeBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mapWidget.setAircraftBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_next_point));
        mapWidget.setFlightPathColor(getResources().getColor(R.color.teal_200));
        mapWidget.setDirectionToHomeVisible(false);
    }

    float missionLength = 0;

    private void getMissionDistance() {
        missionLength = 0;
        if (latLngList != null && latLngList.size() > 1) {
            for (int i = 0; i < latLngList.size(); i++) {
                if (i == 0) {
                    missionLength = 0;
                } else {
                    DPoint dPoint = new DPoint(latLngList.get(i - 1).getLatitude(), latLngList.get(i - 1).getLongitude());
                    DPoint dPoint1 = new DPoint(latLngList.get(i).getLatitude(), latLngList.get(i).getLongitude());
                    missionLength = missionLength + (CoordinateConverter.calculateLineDistance(dPoint, dPoint1));

                }
            }
        }

        tv_mission_length.setText(missionLength + "米");
        tv_mission_time.setText((Utils.secToTime((int) missionLength / 5)));
        tv_mission_point_num.setText(missionUploadData.getWaypoints().size() + "个");


    }

    //Marker全部展示
    private void moveCameraCenter() {
        if (markerList != null) {
            DJILatLngBounds.Builder boundsBuilder = new DJILatLngBounds.Builder();//存放所有点的经纬度

            for (int i = 0; i < markerList.size(); i++) {
                boundsBuilder.include(markerList.get(i).getPosition());//把所有点都include进去（LatLng类型）
            }
            aMap.animateCamera(DJICameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 15, 100));//第二个参数为四周留空宽度
        }
    }

    DJIMap.OnMarkerClickListener onMarkerClickListener = new DJIMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(DJIMarker djiMarker) {
            if (uploadMissionWayoints == null || uploadMissionWayoints.size() < 2
            ) {
                Toast.makeText(MissionActivity.this,"航点数目必须>=2",Toast.LENGTH_SHORT).show();

            } else {
                MissionEditActivity.actionStartForResult(MissionActivity.this, new Gson().toJson(missionUploadData));
            }
            return false;
        }
    };

    DJIMap.OnMapClickListener onMapClickListener = new DJIMap.OnMapClickListener() {
        @Override
        public void onMapClick(DJILatLng djiLatLng) {
            addMarkersToMap(djiLatLng, null);
            moveCameraCenter();

        }
    };

    DJIMap.OnMarkerDragListener onMarkerDragListener = new DJIMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(DJIMarker djiMarker) {

        }

        @Override
        public void onMarkerDrag(DJIMarker djiMarker) {
        }

        @Override
        public void onMarkerDragEnd(DJIMarker djiMarker) {
            if (markerList.contains(djiMarker)) {
                //移动Marker后更新本地数据
                markerList.set(markerList.indexOf(djiMarker), djiMarker);
                latLngList.set(markerList.indexOf(djiMarker), djiMarker.getPosition());
                MissionUploadData.Waypoint waypoint = new MissionUploadData.Waypoint();
                waypoint.setLatitude(djiMarker.getPosition().latitude + "");
                waypoint.setLongitude(djiMarker.getPosition().longitude + "");
                uploadMissionWayoints.set(markerList.indexOf(djiMarker), waypoint);
                missionUploadData.setWaypoints(uploadMissionWayoints);
                LatLonPoint latLonPoint = new LatLonPoint(djiMarker.getPosition().latitude, djiMarker.getPosition().longitude);
                latLonPoints.set(markerList.indexOf(djiMarker), latLonPoint);

                removeAllPolyLine();
                if (polylineOptions != null) {
                    polylineOptions.addAll(latLngList).width(5).setDashed(false).color(getResources().getColor(R.color.blue));
                    djiPolyline = aMap.addPolyline(polylineOptions);
                    djiPolylines.add(djiPolyline);
                }
                getMissionDistance();
                moveCameraCenter();

            }
        }

    };

    /**
     * 往地图上添加marker/第二个参数表示如果是导入航线，为需要上传的航线数据赋值(航点的信息)
     *
     * @param djiLatLng
     * @param flightPointListDTO
     */
    public void addMarkersToMap(DJILatLng djiLatLng, FlightInfo.DataDTO.FlightPointListDTO flightPointListDTO) {
        latLngList.add(djiLatLng);
        latLonPoints.add(new LatLonPoint(djiLatLng.getLatitude(), djiLatLng.getLongitude()));
        DJIMarker marker = aMap.addMarker(new DJIMarkerOptions().position(djiLatLng)
                .icon(DJIBitmapDescriptorFactory.fromBitmap(convertViewToBitmap())).draggable(true));
        markerList.add(marker);

        polylineOptions.add(djiLatLng).width(5).setDashed(false).color(getResources().getColor(R.color.blue));
        djiPolyline = aMap.addPolyline(polylineOptions);
        djiPolylines.add(djiPolyline);
        //为初始化的航线Bean添加航点信息
        MissionUploadData.Waypoint waypoint = new MissionUploadData.Waypoint();
        waypoint.setLatitude(djiLatLng.latitude + "");
        waypoint.setLongitude(djiLatLng.longitude + "");
        if (flightPointListDTO != null) {
            waypoint.setAltitude(String.valueOf(flightPointListDTO.getAltitude()));
            waypoint.setSpeed(String.valueOf(flightPointListDTO.getSpeed()));
            waypoint.setAircraftYawAngle(String.valueOf(flightPointListDTO.getAircraftYawAngle()));
            waypoint.setFlightPointType(String.valueOf(flightPointListDTO.getFlightPointType()));
            waypoint.setTurnMode(String.valueOf(flightPointListDTO.getTurnMode()));
            waypoint.setFlightPointId(String.valueOf(flightPointListDTO.getFlightPointId()));
//            waypoint.setSpeedFollow(flightPointListDTO.getSpeedFollow());
//            waypoint.setHeightFollow(flightPointListDTO.getHeightFollow());
        }
        uploadMissionWayoints.add(waypoint);
        missionUploadData.setWaypoints(uploadMissionWayoints);
        getMissionDistance();
    }

    /**
     * 自定义Marker样式转为Bitmap
     *
     * @return
     */
    public Bitmap convertViewToBitmap() {
        View view = View.inflate(this, R.layout.view_marker, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_marker_num);
        textView.setText(latLngList.size() + "");
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    private void removeAllPolyLine() {
        if (djiPolylines != null) {
            for (int i = 0; i < djiPolylines.size(); i++) {
                djiPolylines.get(i).remove();
            }
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
                    Toast.makeText(MissionActivity.this,"未选择航线",Toast.LENGTH_SHORT).show();
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
                                latLonPoints = new ArrayList<LatLonPoint>();
                                //重新初始化这条待上传的航线数据，暂时除了航点，其它信息待赋值
                                if (uploadMissionWayoints != null) {
                                    uploadMissionWayoints.clear();
                                }
                                aMap.moveCamera(DJICameraUpdateFactory.newCameraPosition(
                                        DJICameraPosition.fromLatLngZoom(
                                                new DJILatLng(
                                                        Double.valueOf(flightInfo.getData().getFlightPointList().get(0).getLatitude()),
                                                        Double.valueOf(flightInfo.getData().getFlightPointList().get(0).getLongitude())),
                                                16)));
                                for (int i = 0; i < flightInfo.getData().getFlightPointList().size(); i++) {
                                    DJILatLng latLng = new DJILatLng(Double.valueOf(flightInfo.getData().getFlightPointList().get(i).getLatitude()),
                                            Double.valueOf(flightInfo.getData().getFlightPointList().get(i).getLongitude()));
                                    addMarkersToMap(latLng, flightInfo.getData().getFlightPointList().get(i));
                                }
                                missionUploadData.setAltitude(String.valueOf(flightInfo.getData().getAltitude()));
                                missionUploadData.setName(flightInfo.getData().getName());
                                missionUploadData.setUavId(flightInfo.getData().getUavId());
                                missionUploadData.setSpeed(String.valueOf(flightInfo.getData().getSpeed()));
                                missionUploadData.setFinishedAction(String.valueOf(flightInfo.getData().getFinishedAction()));
                                missionUploadData.setFlightPathId(flightInfo.getData().getFlightPathId());
                                getMissionDistance();
                                moveCameraCenter();
                                setMapWidgetInfo();
                            }
                        } else {
                            Toast.makeText(MissionActivity.this,response.body().getMsg(),Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<FlightInfo> call, Throwable t) {
                        Toast.makeText(MissionActivity.this,"网络异常:导入航线失败:"+t.toString(),Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (resultCode == RESULT_SAVE_CODE) {
                missionUploadData = new Gson().fromJson(data.getStringExtra("missionInfo"), MissionUploadData.class);
                tv_mission_point_num.setText(missionUploadData.getWaypoints().size() + "个");

                mapScreenShot(RESULT_SAVE_CODE);

            } else if (resultCode == RESULT_UPDATA_CODE) {
                missionUploadData = new Gson().fromJson(data.getStringExtra("missionInfo"), MissionUploadData.class);
                tv_mission_point_num.setText(missionUploadData.getWaypoints().size() + "个");

                mapScreenShot(RESULT_UPDATA_CODE);
            }
        }
    }

    private void mapScreenShot(int type) {
        aMap.snapshot(new MapScreenShotListener() {
                          @Override
                          public void onMapScreenShot(Bitmap bitmap) {
                              if (null == bitmap) {
                                  Log.e("上传", "截屏失败");
                                  return;
                              }
                              try {
                                  FileOutputStream fos = new FileOutputStream(
                                          Environment.getExternalStorageDirectory() + "/" + missionUploadData.getName() + ".png");
                                  boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                  try {
                                      fos.flush();
                                  } catch (IOException e) {
                                      e.printStackTrace();
                                  }
                                  try {
                                      fos.close();
                                  } catch (IOException e) {
                                      e.printStackTrace();
                                  }
                                  StringBuffer buffer = new StringBuffer();
                                  if (b) {
                                      buffer.append("截屏成功 ");
                                      saveOrUpdate(new File(Environment.getExternalStorageDirectory() + "/" + missionUploadData.getName() + ".png"), type);

                                  } else {
                                      buffer.append("截屏失败 ");
                                  }
                                  Log.e("上传截屏", buffer.toString());
                                  Toast.makeText(MissionActivity.this,buffer.toString(),Toast.LENGTH_SHORT).show();
                              } catch (FileNotFoundException e) {
                                  e.printStackTrace();
                                  Log.e("异常了？", e.toString());
                              }
                          }
                      }
        );
    }

    private void saveOrUpdate(File file, int type) {
        Log.e("上传航线信息", new Gson().toJson(missionUploadData));
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
//        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                file))
                .addFormDataPart("name", missionUploadData.getName())
                .addFormDataPart("uavId", missionUploadData.getUavId())
                .addFormDataPart("speed", missionUploadData.getSpeed())
                .addFormDataPart("flightPathId", missionUploadData.getFlightPathId() + "")
                .addFormDataPart("altitude", missionUploadData.getAltitude())
                .addFormDataPart("finishedAction", missionUploadData.getFinishedAction())
                .addFormDataPart("flightPointJson", new Gson().toJson(missionUploadData.getWaypoints()))
                .build();
        Request request;
        if (type == RESULT_SAVE_CODE) {
            request = new Request.Builder()
                    .url(PreferenceUtils.getInstance().getHttpIp() + "flight/save")
                    .method("POST", body)
                    .addHeader("token", PreferenceUtils.getInstance().getUserToken())
                    .build();
        } else {
            request = new Request.Builder()
                    .url(PreferenceUtils.getInstance().getHttpIp()  + "flight/update")
                    .method("POST", body)
                    .addHeader("token", PreferenceUtils.getInstance().getUserToken())
                    .build();
        }

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("请求结果失败", e.toString());

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.body() != null) {
                    Log.e("请求结果", response.body().string());
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        mapWidget.onResume();
    }

    @Override
    protected void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }


}
