package com.mg.uav.ui.activity;

import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mg.uav.R;
import com.mg.uav.base.BaseActivity;
import com.mg.uav.entity.MissionUploadData;
import com.mg.uav.tools.PreferenceUtils;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

/**
 * 编辑航线信息/在此处应将编辑的航线信息返回至航线规划(MissionActivity)
 */
public class MissionEditActivity extends BaseActivity implements View.OnClickListener {

    private static String FLIGHT_INFO = "flight_info";
    private EditText et_line_name, et_speed, et_altitude, et_go_home_altitude, et_point_speed, et_point_altitude;
    private IndicatorSeekBar sb_altitude, sb_speed, sb_go_home_altitude, sb_point_speed, sb_point_altitude;
    private RadioGroup rg_point_line;
    private RadioButton rb_line, rb_waypoint;
    private LinearLayout layout_line, layout_waypoint, layout_last_point, layout_next_point;
    private TextView tv_point_num, tv_last_point, tv_next_point;
    private Button btn_save;
    MissionUploadData missionUploadData;
    int currentWaypointLocation = 0;//当前选中的航点位置


    public static void actionStartForResult(Activity context, String flightInfo) {
        Intent intent = new Intent(context, MissionEditActivity.class);
        intent.putExtra(FLIGHT_INFO, flightInfo);
        context.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mission);
        initView();
        initData();
    }

    private void initData() {
        missionUploadData = new Gson().fromJson(getIntent().getStringExtra(FLIGHT_INFO), MissionUploadData.class);
        if (missionUploadData != null) {

            Log.e("编辑航线信息", new Gson().toJson(missionUploadData));
            if (!TextUtils.isEmpty(missionUploadData.getName())) {
                et_line_name.setText(missionUploadData.getName());
            }
            if (!TextUtils.isEmpty(missionUploadData.getAltitude())) {
                sb_altitude.setProgress(Float.parseFloat(missionUploadData.getAltitude()));
                et_altitude.setText(missionUploadData.getAltitude());
            }
            if (!TextUtils.isEmpty(missionUploadData.getSpeed())) {
                sb_speed.setProgress(Float.parseFloat(missionUploadData.getSpeed()));
                et_altitude.setText(missionUploadData.getSpeed());

            }
            if (!TextUtils.isEmpty(missionUploadData.getAltitude())) {
                sb_go_home_altitude.setProgress(Float.parseFloat(missionUploadData.getAltitude()));
                et_go_home_altitude.setText(missionUploadData.getAltitude());
            }
            if (missionUploadData.getWaypoints()!=null&&missionUploadData.getWaypoints().size()>0){
                et_point_speed.setText(missionUploadData.getWaypoints().get(0).getSpeed());
                et_point_altitude.setText(missionUploadData.getWaypoints().get(0).getAltitude());
            }
        }
    }

    private void initView() {
        tv_point_num = findViewById(R.id.tv_point_num);
        btn_save = findViewById(R.id.btn_save);
        tv_last_point = findViewById(R.id.tv_last_point);
        tv_last_point.setTextColor(getResources().getColor(R.color.blue));
        tv_next_point = findViewById(R.id.tv_next_point);
        et_line_name = findViewById(R.id.et_line_name);
        sb_altitude = findViewById(R.id.sb_altitude);
        sb_speed = findViewById(R.id.sb_speed);
        sb_go_home_altitude = findViewById(R.id.sb_go_home_altitude);
        sb_point_speed = findViewById(R.id.sb_point_speed);
        sb_point_altitude = findViewById(R.id.sb_point_altitude);
        rg_point_line = findViewById(R.id.rg_point_line);
        layout_line = findViewById(R.id.layout_line);
        layout_waypoint = findViewById(R.id.layout_point);
        layout_last_point = findViewById(R.id.layout_last_point);
        layout_next_point = findViewById(R.id.layout_next_point);
        et_speed = findViewById(R.id.et_speed);
        et_altitude = findViewById(R.id.et_altitude);
        et_go_home_altitude = findViewById(R.id.et_go_home_altitude);
        et_point_speed = findViewById(R.id.et_point_speed);
        et_point_altitude = findViewById(R.id.et_point_altitude);
        rg_point_line.setOnCheckedChangeListener(onCheckedChangeListener);
        layout_last_point.setOnClickListener(this);
        layout_next_point.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        sb_speed.setOnSeekChangeListener(onSeekChangeListener);
        sb_altitude.setOnSeekChangeListener(onSeekChangeListener);
        sb_go_home_altitude.setOnSeekChangeListener(onSeekChangeListener);
        sb_point_altitude.setOnSeekChangeListener(onSeekChangeListener);
        sb_point_speed.setOnSeekChangeListener(onSeekChangeListener);
    }

    OnSeekChangeListener onSeekChangeListener = new OnSeekChangeListener() {
        @Override
        public void onSeeking(SeekParams seekParams) {

            switch (seekParams.seekBar.getId()) {
                case R.id.sb_speed://航线速度
                    et_speed.setText(seekParams.seekBar.getProgress() + "");
                    break;
                case R.id.sb_altitude://航线高度
                    et_altitude.setText(seekParams.seekBar.getProgress() + "");
                    break;
                case R.id.sb_go_home_altitude://航线返航高度
                    et_go_home_altitude.setText(seekParams.seekBar.getProgress() + "");
                    break;
                case R.id.sb_point_speed://航点速度
                    et_point_speed.setText(seekParams.seekBar.getProgress() + "");
                    break;
                case R.id.sb_point_altitude://航点高度
                    et_point_altitude.setText(seekParams.seekBar.getProgress() + "");
                    break;
            }

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            switch (seekBar.getId()) {

            }
        }
    };

    RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rb_line:
                    layout_line.setVisibility(View.VISIBLE);
                    layout_waypoint.setVisibility(View.GONE);
                    break;
                case R.id.rb_waypoint:
                    layout_waypoint.setVisibility(View.VISIBLE);
                    layout_line.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_last_point:
                if (missionUploadData.getWaypoints() != null) {
                    if (currentWaypointLocation == 0) {
                        Toast.makeText(MissionEditActivity.this,"已到底起始点",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //这里先将当前View的航点信息保存,再更新之后的航点数据
                    saveCurrentData();
                    currentWaypointLocation = currentWaypointLocation - 1;
                    updataPointData();

                    tv_point_num.setText("航点" + (currentWaypointLocation + 1));
                }
                break;
            case R.id.layout_next_point:
                if (missionUploadData.getWaypoints() != null) {
                    if (currentWaypointLocation == missionUploadData.getWaypoints().size() - 1) {
                        Toast.makeText(MissionEditActivity.this,"暂无更多航点",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //这里先将当前View的航点信息保存,再更新之后的航点数据
                    saveCurrentData();
                    currentWaypointLocation = currentWaypointLocation + 1;
                    updataPointData();
                    tv_point_num.setText("航点" + (currentWaypointLocation + 1));
                }
                break;
            case R.id.btn_save:
                saveMission();
                break;
        }
    }


    private void saveMission() {
        if (TextUtils.isEmpty(et_line_name.getText())) {
            Toast.makeText(MissionEditActivity.this,"请填写航线名称",Toast.LENGTH_SHORT).show();
            return;
        }
        missionUploadData.setName(et_line_name.getText().toString());
        missionUploadData.setAltitude(String.valueOf(sb_altitude.getProgress()));
        missionUploadData.setSpeed(String.valueOf(sb_speed.getProgress()));
        missionUploadData.setFinishedAction("1");
        missionUploadData.setUavId(PreferenceUtils.getInstance().getFlyNumber());
        Intent intent = new Intent();
        intent.putExtra("missionInfo", new Gson().toJson(missionUploadData));
        if (TextUtils.isEmpty(missionUploadData.getFlightPathId())) {
            setResult(RESULT_SAVE_CODE, intent);
        } else {
            setResult(RESULT_UPDATA_CODE, intent);
        }
        finish();

    }

    /**
     * 更新航点数据到View
     */
    private void updataPointData() {
        sb_point_speed.setProgress(Integer.valueOf(missionUploadData.getWaypoints().get(currentWaypointLocation).getSpeed()));
        sb_point_altitude.setProgress(Integer.valueOf(missionUploadData.getWaypoints().get(currentWaypointLocation).getAltitude()));
        et_point_altitude.setText(missionUploadData.getWaypoints().get(currentWaypointLocation).getAltitude());
        et_point_speed.setText(missionUploadData.getWaypoints().get(currentWaypointLocation).getSpeed());
    }

    /**
     * 切换航点时缓存当前航点速度/高度
     */
    public void saveCurrentData() {
        MissionUploadData.Waypoint waypoint = missionUploadData.getWaypoints().get(currentWaypointLocation);
        waypoint.setSpeed(sb_point_speed.getProgress() + "");
        waypoint.setAltitude(sb_point_altitude.getProgress() + "");
        missionUploadData.getWaypoints().set(currentWaypointLocation, waypoint);

    }

    @Override
    public boolean useEventBus() {
        return false;
    }


}
