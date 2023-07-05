package com.mg.uav.callback;

import com.mg.uav.constant.Constant;
import com.mg.uav.entity.Movement;

import org.greenrobot.eventbus.EventBus;

import dji.common.airlink.SignalQualityCallback;

/**
 * 遥控器信号
 */
public class UplinkSignalQualityCallback implements SignalQualityCallback {
    @Override
    public void onUpdate(int i) {
        if (i <= 0) {
            Movement.getInstance().setRemoteControlSignal(0);
        } else if (i > 0 && i <= 20) {
            Movement.getInstance().setRemoteControlSignal(1);
        } else if (i > 20 && i <= 40) {
            Movement.getInstance().setRemoteControlSignal(2);
        } else if (i > 40 && i <= 60) {
            Movement.getInstance().setRemoteControlSignal(3);
        } else if (i > 60 && i <= 80) {
            Movement.getInstance().setRemoteControlSignal(4);
        } else if (i > 80 && i <= 100) {
            Movement.getInstance().setRemoteControlSignal(5);
        }
//        EventBus.getDefault().post(Constant.UPLINK_SIGNAL_QUALITY_STATE_CHANGE);

    }
}
