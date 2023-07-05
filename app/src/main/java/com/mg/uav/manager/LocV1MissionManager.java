package com.mg.uav.manager;

import android.os.Handler;

import com.mg.uav.base.BaseManager;
import com.mg.uav.entity.MissionUploadData;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.util.CommonCallbacks;

import static com.mg.uav.app.DJIApp.getWaypointMissionV1Operator;

/**
 * 航线飞行任务V1(Mobile客户端控制)
 */
public class LocV1MissionManager{

    private List<Waypoint> waypointList = new ArrayList<>();

    public static WaypointMission.Builder waypointMissionBuilder;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
//    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;

    private LocV1MissionManager() {
    }

    private static class WayPointMissionManagerHolder {
        private static final LocV1MissionManager INSTANCE = new LocV1MissionManager();
    }

    public static LocV1MissionManager getInstance() {
        return LocV1MissionManager.WayPointMissionManagerHolder.INSTANCE;
    }

    public void createWayPointMission(MissionUploadData mission) {
        getWaypointMissionV1Operator().clearMission();
        if (waypointList!=null){
            waypointList.clear();
        }
        for (int i = 0; i < mission.getWaypoints().size(); i++) {
            Waypoint mWaypoint = new Waypoint(Double.valueOf(mission.getWaypoints().get(i).getLatitude()), Double.valueOf(mission.getWaypoints().get(i).getLongitude()), Float.parseFloat(mission.getWaypoints().get(i).getAltitude()));
            waypointList.add(mWaypoint);
        }
        waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                .headingMode(mHeadingMode)
                .autoFlightSpeed(Float.parseFloat(mission.getSpeed()))
                .maxFlightSpeed(Float.parseFloat(mission.getSpeed()))
                .finishedAction(WaypointMissionFinishedAction.GO_HOME)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        DJIError error = getWaypointMissionV1Operator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            Logger.e("loadWaypoint succeeded");
            uploadWayPointMission();
        } else {
            Logger.e("loadWaypoint failed " + error.getDescription());
        }

    }

    public void uploadWayPointMission() {

        getWaypointMissionV1Operator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startWaypointMission();
                        }
                    },2000);
                } else {
                    getWaypointMissionV1Operator().retryUploadMission(null);
                }
            }
        });

    }

    public void startWaypointMission() {
        getWaypointMissionV1Operator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error==null){
                    Logger.e("startMission success");
                }else{
                    Logger.e("startMission fail"+error.getDescription());
                }
            }
        });
    }

    public void stopWaypointMission() {
        getWaypointMissionV1Operator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
//                Toast.makeText(this,"aircraft disconnect!",Toast.LENGTH_SHORT).show();
//
//                ToastUtil.showToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });
    }
}
