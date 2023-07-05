package com.mg.uav.callback;

import com.google.gson.Gson;

import com.mg.uav.entity.Movement;
import com.orhanobut.logger.Logger;

import androidx.annotation.NonNull;
import dji.common.error.DJIError;
import dji.keysdk.callback.GetCallback;

public class LinkPlanStateCallBack implements GetCallback {
    @Override
    public void onSuccess(@NonNull Object o) {
        Logger.e("LinkPlanStateCallBack："+o.toString());

        Movement.getInstance().setPlaneMessage(o.toString());
    }

    @Override
    public void onFailure(@NonNull DJIError djiError) {

    }
}
