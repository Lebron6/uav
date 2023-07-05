package com.mg.uav.callback;

import com.google.gson.Gson;

import com.mg.uav.constant.Constant;
import com.mg.uav.entity.Movement;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import dji.common.battery.BatteryState;

/**
 * 电池A
 */
public class BatteryAStateCallback implements BatteryState.Callback{
    @Override
    public void onUpdate(BatteryState state) {
        Logger.e("BatteryStateA"+new Gson().toJson(state));
        Movement.getInstance().setVoltageInfoA(state.getVoltage());
        Movement.getInstance().setElectricityInfoA(state.getChargeRemainingInPercent());
//        EventBus.getDefault().post(Constant.BATTERY_A_STATE_CHANGE);
    }
}
