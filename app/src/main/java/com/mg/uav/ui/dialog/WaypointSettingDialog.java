package com.mg.uav.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mg.uav.R;

import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;

public class WaypointSettingDialog extends Dialog {

    String mSpeed, mFinishedAction, mHeadingMode;

    public WaypointSettingDialog(Context context) {
        super(context);
    }

    public WaypointSettingDialog(Context context, int theme, DialogClickListener dialogClickListener) {
        super(context, theme);
        this.dialogClickListener = dialogClickListener;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_waypoint_setting);
        initView();
    }

    private void initView() {
        TextView wpAltitude_TV = (TextView) findViewById(R.id.altitude);
        TextView wpname_TV = (TextView) findViewById(R.id.name);
        TextView tv_sure = (TextView) findViewById(R.id.tv_sure);
        TextView tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        RadioGroup speed_RG = (RadioGroup) findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) findViewById(R.id.heading);

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeed) {
                    mSpeed = "5";
                } else if (checkedId == R.id.MidSpeed) {
                    mSpeed = "8";
                } else if (checkedId == R.id.HighSpeed) {
                    mSpeed = "12";
                }
            }
        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.finishNone) {
                    mFinishedAction = WaypointMissionFinishedAction.NO_ACTION.value()+"";
                } else if (checkedId == R.id.finishGoHome) {
                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME.value()+"";
                } else if (checkedId == R.id.finishAutoLanding) {
                    mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND.value()+"";
                } else if (checkedId == R.id.finishToFirst) {
                    mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT.value()+"";
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.headingNext) {
                    mHeadingMode = WaypointMissionHeadingMode.AUTO.value()+"";
                } else if (checkedId == R.id.headingInitDirec) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION .value()+"";
                } else if (checkedId == R.id.headingRC) {
                    mHeadingMode = WaypointMissionHeadingMode.CONTROL_BY_REMOTE_CONTROLLER .value()+"";
                } else if (checkedId == R.id.headingWP) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING .value()+"";
                }
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tv_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = wpname_TV.getText().toString().trim();
                String altitude = wpAltitude_TV.getText().toString().trim();
                if (TextUtils.isEmpty(name)||TextUtils.isEmpty(altitude)){
                    Toast.makeText(getContext(),"请检查航线设置",Toast.LENGTH_SHORT).show();
                    return;
                }
                dialogClickListener.sure(altitude, name, mSpeed,mFinishedAction, mFinishedAction);
                dismiss();
            }
        });
    }


    public DialogClickListener dialogClickListener;

    public interface DialogClickListener {
        void sure(String altitude, String name, String speed,String finishAction, String headMode);
    }
}
