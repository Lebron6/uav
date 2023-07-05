package com.mg.uav.callback;

import com.google.gson.Gson;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.mg.uav.client.ChannelCache;
import com.mg.uav.constant.Constant;
import com.mg.uav.constant.SocketConfig;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.entity.Movement;
import com.mg.uav.tools.LocationUtils;
import com.mg.uav.tools.MapConvertUtils;
import com.mg.uav.tools.PreferenceUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.keysdk.DiagnosticsKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.GetCallback;
import dji.sdk.flightcontroller.FlightController;

import static com.mg.uav.constant.Constant.LOG_TAG;
import static dji.keysdk.FlightControllerKey.WIND_SPEED;

/**
 * 飞控
 */
public class FlightControllerStateCallBack implements FlightControllerState.Callback {

    private Context mContext;
    private FlightController flightController;
    DiagnosticsKey diagnosticsKey = DiagnosticsKey.create(DiagnosticsKey.SYSTEM_STATUS);
    private boolean isFly;

    public FlightControllerStateCallBack(FlightController flightController, Context context) {
        this.mContext = context;
        this.flightController = flightController;
    }

    @Override
    public void onUpdate(@NonNull FlightControllerState state) {
//        state.getGoHomeExecutionState()
        //待测试：下避障开启时，飞机在距离地面0.3米时会触发悬停，需要执行isLandingConfirmationNeeded();确认着陆
//        if (state.isLandingConfirmationNeeded()) {
//            EventBus.getDefault().post(Constant.CONFIRM_LANDING);
//        }
//        Logger.e(LOG_TAG+"state:"+new Gson().toJson(state));
        //距返航点
        double distance = LocationUtils.getDistance(state.getHomeLocation().getLongitude() + "",
                state.getHomeLocation().getLatitude() + ""
                , state.getAircraftLocation().getLongitude() + "",
                state.getAircraftLocation().getLatitude() + "");
        Movement.getInstance().setDistance((int) distance);
        //水平速度
        float horizontalSpeed = Math.abs(state.getVelocityX());
        int horizontalSpeed1 = (int) Math.sqrt((double) ((Math.abs(state.getVelocityY()) * Math.abs(state.getVelocityY())) + (Math.abs(state.getVelocityX()) * Math.abs(state.getVelocityX()))));
        Movement.getInstance().setHorizontalSpeed(Math.round(horizontalSpeed1));
//        Logger.e("水平速度:" + horizontalSpeed1);

        //垂直速度
        float verticalSpeed = Math.abs(state.getVelocityZ());
        Movement.getInstance().setVerticalSpeed(Math.round(verticalSpeed));
//        Logger.e("垂直速度" + verticalSpeed);

        //风速
        Object windSpeed = KeyManager.getInstance().getValue((FlightControllerKey.create(WIND_SPEED)));
        if (windSpeed != null && windSpeed instanceof Integer) {

            Movement.getInstance().setWindSpeed((int) windSpeed);
        }
//        Logger.e("风速" + windSpeed);

        //飞行高度
        float flyingHeight = state.getAircraftLocation().getAltitude();
        Movement.getInstance().setFlyingHeight(Math.round(flyingHeight));
//        Logger.e("飞行高度" + flyingHeight);

        //卫星数
        int satelliteCount = state.getSatelliteCount();
        Movement.getInstance().setSatelliteNumber(satelliteCount);
//        Logger.e("卫星数" + satelliteCount);

        //飞机当前坐标
        if ((state.getAircraftLocation().getLatitude() + "").equals("NaN")) {
            Movement.getInstance().setCurrentLatitude("0.0");
        } else {
            LatLng latLng = MapConvertUtils.getGDLatLng(state.getAircraftLocation().getLatitude(),
                    state.getAircraftLocation().getLongitude(), mContext);
            Movement.getInstance().setCurrentLatitude(latLng.latitude + "");
        }
        if ((state.getAircraftLocation().getLongitude() + "").equals("NaN")) {
            Movement.getInstance().setCurrentLongitude("0.0");
        } else {
            LatLng latLng = MapConvertUtils.getGDLatLng(state.getAircraftLocation().getLatitude(),
                    state.getAircraftLocation().getLongitude(), mContext);
            Movement.getInstance().setCurrentLongitude(latLng.longitude + "");
        }

        //飞机飞行状态
        KeyManager.getInstance().getValue(diagnosticsKey, new GetCallback() {
            @Override
            public void onSuccess(@NonNull Object o) {
                Movement.getInstance().setPlaneMessage(o.toString());
            }

            @Override
            public void onFailure(@NonNull DJIError djiError) {

            }
        });
        //当前飞机桨叶是否转动
        Movement.getInstance().setPlaneWing(state.isFlying());

        if (flightController != null) {
            //飞机机头角度
            float heading = flightController.getCompass().getHeading();
            Movement.getInstance().setAngleYaw(Math.round(heading));
        }

        //飞行模式
        Movement.getInstance().setPlaneMode(state.getFlightMode().name());
        //架次
//        if (isFly != state.isFlying()) {
//            DataInfo.Message message = DataInfo.Message.newBuilder()
//                    .setFlyNum(PreferenceUtils.getInstance().getFlyNumber())
//                    .setDataType(DataInfo.Message.DataType.SortiesType)
//                    .setSorties(DataInfo.Sorties.newBuilder().setType(state.isFlying() ? 0 : 1).build())
//                    .build();
//            ChannelCache.send(message);
//            isFly=state.isFlying();
//        }
        EventBus.getDefault().post(Constant.FLIGHT_CONTROLLER_STATE_CHANGE);


        o=n;n= state.isFlying()?1:0;
        if(o==0&&n==1){
            //起飞
            DataInfo.Message message=DataInfo.Message.newBuilder().setFlyNum(PreferenceUtils.getInstance().getFlyNumber())
                    .setDataType(DataInfo.Message.DataType.SortiesType)
                    .setSorties(DataInfo.Sorties.newBuilder().setType(0).build())
                    .build();
            ChannelCache.send(message);
        }else if(o==1&&n==0){
            //降落
            DataInfo.Message message=DataInfo.Message.newBuilder().setFlyNum(PreferenceUtils.getInstance().getFlyNumber())
                    .setDataType(DataInfo.Message.DataType.SortiesType)
                    .setSorties(DataInfo.Sorties.newBuilder().setType(1).build())
                    .build();
            ChannelCache.send(message);
        }
    }

    int o=-1,n=-1;
}
