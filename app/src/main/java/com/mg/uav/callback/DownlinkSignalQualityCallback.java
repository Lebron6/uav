package com.mg.uav.callback;

import com.mg.uav.constant.Constant;
import com.mg.uav.entity.Movement;

import org.greenrobot.eventbus.EventBus;

import dji.common.airlink.SignalQualityCallback;

public class DownlinkSignalQualityCallback implements SignalQualityCallback {
    @Override
    public void onUpdate(int i) {
        if (i <= 0) {
            Movement.getInstance().setPictureBiographySignal(0);
        } else if (i > 0 && i <= 20) {
            Movement.getInstance().setPictureBiographySignal(1);
        } else if (i > 20 && i <= 40) {
            Movement.getInstance().setPictureBiographySignal(2);
        } else if (i > 40 && i <= 60) {
            Movement.getInstance().setPictureBiographySignal(3);
        } else if (i > 60 && i <= 80) {
            Movement.getInstance().setPictureBiographySignal(4);
        } else if (i > 80 && i <= 100) {
            Movement.getInstance().setPictureBiographySignal(5);
        }
//        EventBus.getDefault().post(Constant.DOWNLINK_SIGNAL_QUALITY_STATE_CHANGE);
    }
}
