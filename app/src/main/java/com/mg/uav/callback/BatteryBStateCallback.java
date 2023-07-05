package com.mg.uav.callback;

import com.mg.uav.entity.Movement;
import dji.common.battery.BatteryState;

/**
 * 电池B
 */
public class BatteryBStateCallback implements BatteryState.Callback{
    @Override
    public void onUpdate(BatteryState state) {
        Movement.getInstance().setVoltageInfoB(state.getVoltage());
        Movement.getInstance().setElectricityInfoB(state.getChargeRemainingInPercent());
//        EventBus.getDefault().post(Constant.BATTERY_B_STATE_CHANGE);
    }
}
