package com.mg.uav.ui.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mg.uav.R;
import com.mg.uav.app.DJIApp;
import com.mg.uav.base.BaseActivity;
import com.mg.uav.entity.LoginValues;
import com.mg.uav.entity.User;
import com.mg.uav.tools.AppManager;
import com.mg.uav.tools.PreferenceUtils;
import com.orhanobut.logger.Logger;
import com.yanzhenjie.permission.AndPermission;

import dji.common.error.DJIError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.useraccount.UserAccountManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static dji.common.useraccount.UserAccountState.AUTHORIZED;

public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private RelativeLayout layout_start_fly, layout_mission_plan, layout_gallay, layout_record, layout_exit,layout_config;
    private ImageView iv_plane_name;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            // 添加这俩权限就不会延迟了
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        initData();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIApp.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController!=null){
                flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError==null){
                            Logger.e("成功获取控制权");
                        }else{
                            Logger.e("获取控制权失败:"+djiError.getDescription());
                        }
                        Logger.e("");
                    }
                });
            }

            if (UserAccountManager.getInstance().getUserAccountState()==AUTHORIZED){
                return;
            }else{
                UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                        new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                            @Override
                            public void onSuccess(final UserAccountState userAccountState) {
                                Logger.e("Login Success!");

                            }
                            @Override
                            public void onFailure(DJIError error) {
                                Log.e(TAG, "Login Fail"+error);

                            }
                        });
            }

        }

    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    private void initData() {
        AndPermission.with(this)
                .runtime()
                .permission(REQUIRED_PERMISSION_LIST)
                .onGranted(permissions -> {
                    // Storage permission are allowed.
                    if (PreferenceUtils.getInstance().getLoginStatus() == true) {
                        LoginValues loginValues = new LoginValues();
                        loginValues.setUsername(PreferenceUtils.getInstance().getUserName());
                        loginValues.setPassword(PreferenceUtils.getInstance().getUserPassword());
                        toLogin(loginValues);
                    }else{
                        LoginActivity.actionStart(this);
                        finish();
                    }
                })
                .onDenied(permissions -> {
                    // Storage permission are not allowed.
                    Toast.makeText(this,"请给予APP运行所需权限",Toast.LENGTH_SHORT).show();
                    finish();
                })
                .start();
    }
    private void toLogin(LoginValues loginValues) {
        if (TextUtils.isEmpty(PreferenceUtils.getInstance().getHttpIp())){
            ConfigActivity.actionStart(this);
            finish();
        }else{
            createRequest().userLogin(loginValues).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.body()!=null){
                        switch (response.body().getCode()) {
                            case 0:
                                PreferenceUtils.getInstance().setLoginStatus(true);
                                PreferenceUtils.getInstance().setUserToken(response.body().getToken());
                                PreferenceUtils.getInstance().setUserRole(response.body().getRole());
                                PreferenceUtils.getInstance().setUserPassword(loginValues.getPassword());
                                PreferenceUtils.getInstance().setUserName(loginValues.getUsername());
                                break;
                        }
                    }else{
                        Toast.makeText(HomeActivity.this,"网络异常:登陆失败",Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(HomeActivity.this,"网络异常:登录失败",Toast.LENGTH_SHORT).show();
                    Log.e("网络异常:登陆失败", t.toString());
                }
            });
        }
    }
    //判断是哪款机型
    private void initView() {
        layout_start_fly = findViewById(R.id.layout_start_fly);
        layout_mission_plan = findViewById(R.id.layout_mission_plan);
        layout_gallay = findViewById(R.id.layout_gallay);
        layout_record = findViewById(R.id.layout_record);
        layout_exit = findViewById(R.id.layout_exit);
        layout_config = findViewById(R.id.layout_config);
        layout_start_fly.setOnClickListener(this);
        layout_mission_plan.setOnClickListener(this);
        layout_gallay.setOnClickListener(this);
        layout_record.setOnClickListener(this);
        layout_exit.setOnClickListener(this);
        layout_config.setOnClickListener(this);
        iv_plane_name = findViewById(R.id.iv_plane_name);
        if (DJIApp.getProductInstance() == null) {
            Toast.makeText(this,"Product disconnected",Toast.LENGTH_SHORT).show();
            return;
        } else {
            BaseProduct product = DJIApp.getProductInstance();
//            if (product!=null){
//                switch (product.getModel()) {
//                    case A3:
//
//                        break;
//                }
//            }

        }
    }



    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_gallay:
                GalleryActivity.actionStart(this);
                break;
            case R.id.layout_record:
                Toast.makeText(this,"记录",Toast.LENGTH_SHORT).show();
                break;
            case R.id.layout_start_fly:
                MainActivity.actionStart(this, null);
                break;
            case R.id.layout_mission_plan:
                MissionActivity.actionStart(this);
                break;
            case R.id.layout_exit:
                exit();
                break;
                case R.id.layout_config:
ConfigActivity.actionStart(this);
                break;
        }
    }

    private void exit() {
        PreferenceUtils.getInstance().setLoginStatus(false);
        AppManager.getAppManager().finishAllActivity();
        LoginActivity.actionStart(this);
    }
}
