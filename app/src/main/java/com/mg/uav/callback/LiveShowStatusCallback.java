package com.mg.uav.callback;

import com.mg.uav.entity.Movement;
import com.orhanobut.logger.Logger;

import dji.sdk.sdkmanager.LiveStreamManager;

public class LiveShowStatusCallback implements LiveStreamManager.OnLiveErrorStatusListener,LiveStreamManager.OnLiveChangeListener{
    @Override
    public void onStatusChanged(int i) {
        Movement.getInstance().setLiveStatus(i==0?1:0);
        Logger.e("LiveShowStatusCallback:"+i);
//        Movement.getInstance().setLiveStatus(i);
    }

    @Override
    public void onError(int i, String s) {
        Movement.getInstance().setLiveStatus(i==0?1:0);
        Logger.e("OnLiveErrorStatusListener:"+i+s);
    }
}
