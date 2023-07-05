package com.mg.uav.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mg.uav.R;
import com.mg.uav.base.BaseActivity;
import com.mg.uav.entity.LoginValues;
import com.mg.uav.entity.User;
import com.mg.uav.tools.PreferenceUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 登录
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText et_account, et_password;

    private Button btn_login,btn_setting;


    public static void actionStart(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (TextUtils.isEmpty(PreferenceUtils.getInstance().getHttpIp())){
            ConfigActivity.actionStart(this);
            Toast.makeText(this,"请先配置运行环境",Toast.LENGTH_SHORT).show();
            finish();
        }
        initView();
    }

    private void initView() {
        et_account = findViewById(R.id.et_account);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        btn_setting=findViewById(R.id.btn_config);
        btn_setting.setOnClickListener(this);

    }


    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:

                String account = et_account.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(account)) {
                    Toast.makeText(this,"请输入账号",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this,"请输入密码",Toast.LENGTH_SHORT).show();
                    return;
                }
                LoginValues loginValues = new LoginValues();
                loginValues.setPassword(password);
                loginValues.setUsername(account);
                toLogin(loginValues);
                break;
            case R.id.btn_config:
                ConfigActivity.actionStart(this);
                break;
        }
    }

    private void toLogin(LoginValues loginValues) {
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
                            HomeActivity.actionStart(LoginActivity.this);
                            finish();
                            break;

                    }
                }else{
                    Toast.makeText(LoginActivity.this,"网络异常:登陆失败",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this,"网络异常:登陆失败",Toast.LENGTH_SHORT).show();
                Log.e("网络异常:登陆失败", t.toString());
            }
        });

    }

}
