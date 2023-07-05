package com.mg.uav.callback;

import android.text.TextUtils;

import com.mg.uav.constant.Constant;
import com.mg.uav.entity.Movement;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import dji.sdk.base.DJIDiagnostics;

public class DiagnosticsInformationCallback implements DJIDiagnostics.DiagnosticsInformationCallback {
    @Override
    public void onUpdate(List<DJIDiagnostics> list) {
        if (list != null && !list.isEmpty()) {
            String errorMsg = "";
            for (int i = 0; i < list.size(); i++) {
                if (!TextUtils.isEmpty(list.get(i).getSolution().trim())) {
                    errorMsg += list.get(i).getSolution() + ",";
                }
            }
            if (!TextUtils.isEmpty(errorMsg)) {
                errorMsg = errorMsg.substring(0, errorMsg.length() - 1);
                Movement.getInstance().setWarningMessage(errorMsg);
//                EventBus.getDefault().post(Constant.DIAGNOSTICS_INFO_STATE_CHANGE);
            }
        }
    }
}
