package com.mg.uav.callback;

import android.util.Log;

import com.mg.uav.base.BaseManager;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.entity.Movement;
import com.orhanobut.logger.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dji.common.error.DJIWaypointV2Error;
import dji.common.mission.waypointv2.WaypointV2MissionDownloadEvent;
import dji.common.mission.waypointv2.WaypointV2MissionExecuteState;
import dji.common.mission.waypointv2.WaypointV2MissionExecutionEvent;
import dji.common.mission.waypointv2.WaypointV2MissionUploadEvent;

public class WaypointV2MissionOperatorListener extends BaseManager implements dji.sdk.mission.waypoint.WaypointV2MissionOperatorListener {

    DataInfo.Message message;

    public WaypointV2MissionOperatorListener() {
    }

    public WaypointV2MissionOperatorListener(DataInfo.Message message) {
        this.message = message;
    }

    @Override
    public void onDownloadUpdate(@NonNull WaypointV2MissionDownloadEvent waypointV2MissionDownloadEvent) {

    }

    @Override
    public void onUploadUpdate(@NonNull WaypointV2MissionUploadEvent waypointV2MissionUploadEvent) {
        if (waypointV2MissionUploadEvent.getError() != null) {
            sendErrorMsg2Server(message, waypointV2MissionUploadEvent.getError().getDescription());
            Logger.e("upload mission fail：" + waypointV2MissionUploadEvent.getError().getDescription());
        }
    }

    @Override
    public void onExecutionUpdate(@NonNull WaypointV2MissionExecutionEvent event) {
        // 当前航点任务状态
//        WaypointV2MissionState state = event.getCurrentState();
        //当前航点任务执行状态
        if (event.getProgress()!=null){
            Logger.e("任务状态："+event.getProgress().getExecuteState().name());
            waypointV2MissionExecuteStateToString(event.getProgress().getExecuteState());
        }
        // 目标航点序号
//        int index = event.getProgress().getTargetWaypointIndex();
        // 是否已经到达航点
//        final String reached = event.getProgress().isWaypointReached() ? "已到达" : "未到达";

    }

    @Override
    public void onExecutionStart() {
        Movement.getInstance().setAirlineFlight(true);
    }

    @Override
    public void onExecutionFinish(@Nullable DJIWaypointV2Error djiWaypointV2Error) {
        Movement.getInstance().setAirlineFlight(false);
    }

    @Override
    public void onExecutionStopped() {
        Movement.getInstance().setAirlineFlight(false);
        Logger.e("onExecutionStopped", "onExecutionStopped");
    }

    // 航点任务执行枚举值转字符串
    private String waypointV2MissionExecuteStateToString(WaypointV2MissionExecuteState state) {
        switch (state) {
            case INITIALIZING:
                return "INITIALIZING 初始化";
            case MOVING:
                Movement.getInstance().setFlightPathStatus(0);
                return "MOVING 移动中";
            case GO_TO_FIRST_WAYPOINT:
                Movement.getInstance().setFlightPathStatus(0);
                return "GO_TO_FIRST_WAYPOINT 飞向第一个航点";
            case INTERRUPTED:
                Movement.getInstance().setFlightPathStatus(1);
                return "INTERRUPTED 飞行器当前被用户打断";
            case FINISHED:
                Movement.getInstance().setFlightPathStatus(1);
                return "FINISHED 完成";
            case GO_HOME:
                Movement.getInstance().setFlightPathStatus(0);
                return "GO_HOME 返航";
            case LANDING:
                Movement.getInstance().setFlightPathStatus(0);
                return "LANDING 着陆";
            case RETURN_TO_FIRST_WAYPOINT:
                Movement.getInstance().setFlightPathStatus(0);
                return "RETURN_TO_FIRST_WAYPOINT 返回到第一个航点";
            case PAUSED:
                Movement.getInstance().setFlightPathStatus(1);
                return "PAUSED 暂停中";
            default:
                return "N/A";
        }
    }
}
