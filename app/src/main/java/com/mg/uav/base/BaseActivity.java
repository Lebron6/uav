package com.mg.uav.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.gyf.barlibrary.ImmersionBar;
import com.mg.uav.api.BaseUrl;
import com.mg.uav.api.HttpLogger;
import com.mg.uav.api.LoggingInterceptor;
import com.mg.uav.api.UavApi;
import com.mg.uav.app.UAVApp;
import com.mg.uav.client.SocketClient;
import com.mg.uav.constant.SocketConfig;
import com.mg.uav.tools.AppManager;
import com.mg.uav.tools.PreferenceUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

public abstract class BaseActivity extends AppCompatActivity {
    public static int REQUEST_CODE = 1;
    public static int RESULT_CODE = 2;
    public static int RESULT_SAVE_CODE = 3;
    public static int RESULT_UPDATA_CODE = 4;
    /**
     * activity堆栈管理
     */
    protected AppManager appManager = AppManager.getAppManager();

    protected String TAG;
    protected boolean useEventBus = false;
    OkHttpClient mOkHttpClient;
    Retrofit retrofit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loggerSimpleName();
        if (useEventBus()) {
            EventBus.getDefault().register(this);
        }

        appManager.addActivity(this);
        initImmersionBar();
    }

    public void initImmersionBar() {
        //在BaseActivity里初始化
        ImmersionBar mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.transparentBar().fullScreen(true).init();
    }


    public abstract boolean useEventBus();

    public void loggerSimpleName() {
        TAG = getClass().getSimpleName();
        Logger.e("当前界面 ："+ TAG);
    }

    /**
     * 初始化okhttpclient.
     * @return okhttpClient
     */
    public OkHttpClient okhttpclient() {
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger(getClass().getSimpleName()));
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
         mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .build();
        return mOkHttpClient;
    }

    public UavApi createRequest() {
        if (!TextUtils.isEmpty(PreferenceUtils.getInstance().getHttpIp())){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(PreferenceUtils.getInstance().getHttpIp())
                    .client(okhttpclient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            UavApi vamApi = retrofit.create(UavApi.class);
            return vamApi;
        }else{
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 从栈中移除activity
        appManager.finishActivity(this);
        if (useEventBus == true) {
            EventBus.getDefault().unregister(this);
        }
    }

}
