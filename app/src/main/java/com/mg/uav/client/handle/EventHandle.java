package com.mg.uav.client.handle;

import com.google.gson.Gson;

import android.util.Log;

import com.mg.uav.client.ChannelCache;
import com.mg.uav.constant.SocketConfig;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.global.DataCache;
import com.mg.uav.manager.CameraManager;
import com.mg.uav.manager.FlightManager;
import com.mg.uav.manager.GimbalManager;
import com.mg.uav.manager.WebV2MissionManager;
import com.mg.uav.tools.PreferenceUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

import static com.mg.uav.constant.Constant.RESTART_LIVE;

public class EventHandle extends SimpleChannelInboundHandler<DataInfo.Message> {


    ThreadPoolExecutor eventHandleThreadPoolExecutor;

    public EventHandle(ThreadPoolExecutor eventHandleThreadPoolExecutor) {
        this.eventHandleThreadPoolExecutor = eventHandleThreadPoolExecutor;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataInfo.Message msg) {
        eventHandleThreadPoolExecutor.execute(() -> {
            try {
                handle(ctx, msg);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private void handle(ChannelHandlerContext ctx, DataInfo.Message message) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Logger.e("Netty接收："+new Gson().toJson(message));
        if (message.hasRequestResponse()) {
            DataInfo.RequestResponse requestResponse = message.getRequestResponse();
            if (requestResponse.hasRegister()) {
                DataInfo.RegisterResponse register = requestResponse.getRegister();
                DataCache.getInstance().setDefault_rising_falling(register.getDefaultRisingFalling());
                DataCache.getInstance().setDefault_forward_backward(register.getDefaultForwardBackward());
                DataCache.getInstance().setDefault_left_right_handed(register.getDefaultLeftRightHanded());
                DataCache.getInstance().setDefault_left_right(register.getDefaultLeftRight());
                DataCache.getInstance().setHolderSpeedType(register.getDefaultHolderSpeed());
                DataCache.getInstance().setRtmp_address(register.getRtmpAddress());
                Logger.e("获取注册飞控速度"+new Gson().toJson(DataCache.getInstance()));
                sendCorrectMsg2Server(message,"速度已初始化");
            }
        } else if (message.hasUavControl()) {
            DataInfo.UavControl uavControl = message.getUavControl();
            String method = uavControl.getUavType().name();
            method = method.substring(0, 1).toLowerCase() + method.substring(1);

            FlightManager flightManager = FlightManager.getInstance();
            Class<?> clazz = flightManager.getClass();
            Method method1 = clazz.getDeclaredMethod(method, DataInfo.Message.class);
            method1.invoke(flightManager, message);
        } else if (message.hasSpeedControl()) {
            DataInfo.SpeedControl speedControl = message.getSpeedControl();
            DataCache.getInstance().setDefault_rising_falling(speedControl.getRisingFalling());
            DataCache.getInstance().setDefault_forward_backward(speedControl.getForwardBackward());
            DataCache.getInstance().setDefault_left_right(speedControl.getLeftRight());
            DataCache.getInstance().setDefault_left_right_handed(speedControl.getLeftRightHanded());
            Logger.e("修改飞控速度",new Gson().toJson(DataCache.getInstance()));
            sendCorrectMsg2Server(message,"飞机速度已更新");
        } else if (message.hasHolderControl()) {
            DataInfo.HolderControl holderControl = message.getHolderControl();
            String method = holderControl.getHoldType().name();
            method = method.substring(0, 1).toLowerCase() + method.substring(1);

            GimbalManager gimbalManager = GimbalManager.getInstance();
            Class<?> clazz = gimbalManager.getClass();
            Method method1 = clazz.getDeclaredMethod(method, DataInfo.Message.class);
            method1.invoke(gimbalManager, message);
        } else if (message.hasCameraControl()) {
            DataInfo.CameraControl cameraControl = message.getCameraControl();
            String method = cameraControl.getCameraType().name();
            method = method.substring(0, 1).toLowerCase() + method.substring(1);

            CameraManager cameraManager = CameraManager.getInstance();
            Class<?> clazz = cameraManager.getClass();
            Method method1 = clazz.getDeclaredMethod(method, DataInfo.Message.class);
            method1.invoke(cameraManager, message);
        } else if (message.hasFlightPathControl()) {
            DataInfo.FlightPathControl flightPathControl = message.getFlightPathControl();
            String method = flightPathControl.getControlType().name();
            method = method.substring(0, 1).toLowerCase() + method.substring(1);

            WebV2MissionManager webV2MissionManager = WebV2MissionManager.getInstance();
            Class<?> clazz = webV2MissionManager.getClass();
            Method method1 = clazz.getDeclaredMethod(method, DataInfo.Message.class);
            method1.invoke(webV2MissionManager, message);
        } else if (message.getDataType()== DataInfo.Message.DataType.RestartLiveType){
            Log.e("请求结果", "---001---");
                        EventBus.getDefault().post(RESTART_LIVE);
        }

    }

    public void sendCorrectMsg2Server(DataInfo.Message message, String result) {
        DataInfo.Message msg = DataInfo.Message.newBuilder()
                .setDataType(DataInfo.Message.DataType.RequestResponseType)
                .setFlyNum(PreferenceUtils.getInstance().getFlyNumber())
                .setId(message.getId())
                .setRequestResponse(
                        DataInfo.RequestResponse.newBuilder()
                                .setStatus(200)
                                .setResponseTime(System.currentTimeMillis())
                                .setResponseType(DataInfo.RequestResponse.ResponseType.OtherResponseType)
                                .setOther(
                                        DataInfo.OtherResponse.newBuilder()
                                                .setMsg(result).build()
                                ).build()

                ).build();
        ChannelCache.send(msg);
    }
}
