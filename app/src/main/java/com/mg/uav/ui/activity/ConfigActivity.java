package com.mg.uav.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mg.uav.R;
import com.mg.uav.app.DJIApp;
import com.mg.uav.base.BaseActivity;
import com.mg.uav.tools.AppManager;
import com.mg.uav.tools.PreferenceUtils;
import com.mg.uav.tools.RecyclerViewHelper;
import com.mg.uav.ui.adapter.FileListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseProduct;
import dji.sdk.media.FetchMediaTask;
import dji.sdk.media.FetchMediaTaskContent;
import dji.sdk.media.FetchMediaTaskScheduler;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

/**
 * 配置
 */
public class ConfigActivity extends BaseActivity implements View.OnClickListener {

    private EditText et_socket_ip;
    private EditText et_port;
    private EditText et_fly_num;
    private EditText et_interface_ip;
    private Button btn_setting;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ConfigActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        initView();
        initData();
    }

    private void initData() {
        String socketIp = PreferenceUtils.getInstance().getSocketHost();
        int socketPort = PreferenceUtils.getInstance().getSocketPort();
        String httpIp = PreferenceUtils.getInstance().getHttpIp();
        String flyNumber = PreferenceUtils.getInstance().getFlyNumber();
        if (!TextUtils.isEmpty(socketIp)) {
            et_socket_ip.setText(socketIp);
        }
        if (socketPort!=0) {
            et_port.setText(socketPort+"");
        }
        if (!TextUtils.isEmpty(httpIp)) {
            et_interface_ip.setText(httpIp);
        }
        if (!TextUtils.isEmpty(flyNumber)) {
            et_fly_num.setText(flyNumber);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        et_socket_ip = findViewById(R.id.et_socket_ip);
        et_port = findViewById(R.id.et_port);
        et_fly_num = findViewById(R.id.et_fly_num);
        et_interface_ip = findViewById(R.id.et_interface_ip);
        btn_setting = findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(this);
    }


    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting:
                if (TextUtils.isEmpty(et_socket_ip.getText())) {
                    Toast.makeText(this, "请输入socket ip地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et_port.getText())) {
                    Toast.makeText(this, "请输入socket端口", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et_interface_ip.getText())) {
                    Toast.makeText(this, "请输入接口ip地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et_fly_num.getText())) {
                    Toast.makeText(this, "请输入飞行器编号", Toast.LENGTH_SHORT).show();
                    return;
                }
                setting();
                break;
        }
    }

    private void setting() {
        PreferenceUtils.getInstance().loginOut();
        PreferenceUtils.getInstance().setSocketHost(et_socket_ip.getText().toString());
        PreferenceUtils.getInstance().setSocketPort(Integer.valueOf(et_port.getText().toString()));
        PreferenceUtils.getInstance().setFlyNumber(et_fly_num.getText().toString());
        PreferenceUtils.getInstance().setHttpIp(et_interface_ip.getText().toString());
        AppManager.getAppManager().finishAllActivity();
        LoginActivity.actionStart(this);
    }
}
