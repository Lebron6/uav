package com.mg.uav.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.mg.uav.constant.Constant;
import com.mg.uav.entity.Movement;
import com.mg.uav.tools.PreferenceUtils;
import com.orhanobut.logger.Logger;

import androidx.core.content.ContextCompat;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.product.Model;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointV2MissionOperator;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;


public class DJIApp extends Application {

    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    public static final String TAG = "DJIApp";

    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback;
    private static BaseProduct mProduct;
    public Handler mHandler;

    private Application instance;

    public void setContext(Application application) {
        instance = application;
    }

    @Override
    public Context getApplicationContext() {
        return instance;
    }



    public DJIApp() {

    }

    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {

            @Override
            public void onRegister(DJIError djiError) {
                if(djiError == DJISDKError.REGISTRATION_SUCCESS) {
                    Toast.makeText(getApplicationContext(), "Register sdk success", Toast.LENGTH_LONG).show();
                    Logger.e("Register sdk success");
                    PreferenceUtils.getInstance().setRegisterStatus(true);
                    DJISDKManager.getInstance().startConnectionToProduct();
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Register sdk fails, check network is available", Toast.LENGTH_LONG).show();
                        }
                    });

                }
                Log.e(TAG, djiError.toString());
            }

            @Override
            public void onProductDisconnect() {
                Toast.makeText(getApplicationContext(), "设备已失联", Toast.LENGTH_LONG).show();
                Log.d(TAG, "onProductDisconnect");
                notifyStatusChange();
                Movement.getInstance().setCurrentLongitude(Constant.ZERO);
                Movement.getInstance().setCurrentLatitude(Constant.ZERO);
            }

            @Override
            public void onProductConnect(BaseProduct baseProduct) {
                Toast.makeText(getApplicationContext(), "设备已连接", Toast.LENGTH_LONG).show();
                Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                notifyStatusChange();
            }

            @Override
            public void onProductChanged(BaseProduct baseProduct) {
                Log.e("onProductChanged","onProductChanged");

            }

            @Override
            public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                          BaseComponent newComponent) {
                Logger.e("onComponentChange:"+String.format("组件变化 键:%s,旧组件:%s,"
                        +"新组件:%s",componentKey,oldComponent,newComponent
                ));
                if (newComponent != null) {
                    newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                        @Override
                        public void onConnectivityChange(boolean isConnected) {
                            notifyStatusChange();
                        }
                    });
                }
                notifyStatusChange();
            }

            @Override
            public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {
                Logger.e("DJIMSDK init process:"+i);
            }

            @Override
            public void onDatabaseDownloadProgress(long l, long l1) {

            }
        };
        //Check the permissions before registering the application for android system 6.0 above.
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (permissionCheck == 0 && permissionCheck2 == 0)) {
            //This is used to start SDK services and initiate SDK.
            DJISDKManager.getInstance().registerApp(getApplicationContext(), mDJISDKManagerCallback);
            Toast.makeText(getApplicationContext(), "registering, pls wait...", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Please check if the permission is granted.", Toast.LENGTH_LONG).show();
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            getApplicationContext().sendBroadcast(intent);
        }
    };

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) return null;
        return (Aircraft) getProductInstance();
    }

    public static boolean isM300Product() {
        if (DJISDKManager.getInstance().getProduct() == null) {
            return false;
        }
        Model model = DJISDKManager.getInstance().getProduct().getModel();
        return model == Model.MATRICE_300_RTK;
    }

    // 获取航点任务操作器
    public static WaypointV2MissionOperator getWaypointMissionOperator() {
        return MissionControl.getInstance().getWaypointMissionV2Operator();
    }
    // 获取航点任务操作器V1
    public static WaypointMissionOperator getWaypointMissionV1Operator() {
        return MissionControl.getInstance().getWaypointMissionOperator();
    }

    public static synchronized Camera getCameraInstance() {

        if (getProductInstance() == null) return null;

        Camera camera = null;

        if (getProductInstance() instanceof Aircraft){
            camera = ((Aircraft) getProductInstance()).getCamera();

        } else if (getProductInstance() instanceof HandHeld) {
            camera = ((HandHeld) getProductInstance()).getCamera();
        }

        return camera;
    }
}


