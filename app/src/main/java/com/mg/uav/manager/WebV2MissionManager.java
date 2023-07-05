package com.mg.uav.manager;

import com.mg.uav.app.DJIApp;
import com.mg.uav.base.BaseManager;
import com.mg.uav.callback.WaypointV2ActionListener;
import com.mg.uav.callback.WaypointV2MissionOperatorListener;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.entity.Movement;
import com.mg.uav.tools.MapConvertUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import dji.common.error.DJIWaypointV2Error;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.mission.waypointv2.Action.ActionState;
import dji.common.mission.waypointv2.Action.ActionTypes;
import dji.common.mission.waypointv2.Action.InterruptRecoverActionType;
import dji.common.mission.waypointv2.Action.WaypointActuator;
import dji.common.mission.waypointv2.Action.WaypointAircraftControlParam;
import dji.common.mission.waypointv2.Action.WaypointAircraftControlRotateYawParam;
import dji.common.mission.waypointv2.Action.WaypointAircraftControlStartStopFlyParam;
import dji.common.mission.waypointv2.Action.WaypointCameraActuatorParam;
import dji.common.mission.waypointv2.Action.WaypointCameraZoomParam;
import dji.common.mission.waypointv2.Action.WaypointGimbalActuatorParam;
import dji.common.mission.waypointv2.Action.WaypointReachPointTriggerParam;
import dji.common.mission.waypointv2.Action.WaypointTrigger;
import dji.common.mission.waypointv2.Action.WaypointV2Action;
import dji.common.mission.waypointv2.Action.WaypointV2AssociateTriggerParam;
import dji.common.mission.waypointv2.WaypointV2;
import dji.common.mission.waypointv2.WaypointV2Mission;
import dji.common.mission.waypointv2.WaypointV2MissionState;
import dji.common.mission.waypointv2.WaypointV2MissionTypes;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.mission.waypoint.WaypointV2MissionOperator;

/**
 * 航线飞行任务
 */
public class WebV2MissionManager extends BaseManager {

    private WebV2MissionManager() {
    }

    private static class WayPointMissionManagerHolder {
        private static final WebV2MissionManager INSTANCE = new WebV2MissionManager();
    }

    public static WebV2MissionManager getInstance() {
        return WebV2MissionManager.WayPointMissionManagerHolder.INSTANCE;
    }

    //开始航线任务
    public void startType(DataInfo.Message message) {
        Logger.e("开始任务执行");
        if (waypointMissionOperator==null){
            waypointMissionOperator=DJIApp.getWaypointMissionOperator();
        }
        createWayPointV2Mission(message);
    }

    //继续航线任务(只有航线任务状态为INTERRUPTED时可调用)
    public void continueType(DataInfo.Message message) {
        if (waypointMissionOperator==null){
            waypointMissionOperator=DJIApp.getWaypointMissionOperator();
        }
        if (waypointMissionOperator.getCurrentState() == WaypointV2MissionState.INTERRUPTED) {
            waypointMissionOperator.recoverMission(InterruptRecoverActionType.find(1), new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                    if (djiWaypointV2Error == null) {
                        sendCorrectMsg2Server(message, "recoverMission success：" + "The mission has been recovered");
                    } else {
                        sendErrorMsg2Server(message, "recoverMission fail：" + djiWaypointV2Error.getDescription());
                    }
                }
            });
        } else {
            sendErrorMsg2Server(message, "recoverMission fail：" + waypointMissionOperator.getCurrentState());
        }
    }

    //暂停航线任务(只有航线任务状态为EXECUTING时可调用)
    public void suspendType(DataInfo.Message message) {
        Logger.e("暂停任务执行");
        if (waypointMissionOperator==null){
            waypointMissionOperator=DJIApp.getWaypointMissionOperator();
        }
        if (waypointMissionOperator.getCurrentState() == WaypointV2MissionState.EXECUTING) {
            waypointMissionOperator.interruptMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                    if (djiWaypointV2Error == null) {
                        sendCorrectMsg2Server(message, "interruptMission success：" + "The mission has been interrupted");
                    } else {
                        sendErrorMsg2Server(message, "interruptMission fail：" + djiWaypointV2Error.getDescription());
                    }
                }
            });
        } else {
            sendErrorMsg2Server(message, "interruptMission fail：" + waypointMissionOperator.getCurrentState());
        }
    }

    //结束航线任务(只有航线任务状态为EXECUTING或者INTERRUPTED时可调用)
    public void endType(DataInfo.Message message) {
        Logger.e("结束任务执行");
        if (waypointMissionOperator==null){
            waypointMissionOperator=DJIApp.getWaypointMissionOperator();
        }
        if (waypointMissionOperator.getCurrentState() == WaypointV2MissionState.EXECUTING || waypointMissionOperator.getCurrentState() == WaypointV2MissionState.INTERRUPTED) {
            waypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                    if (djiWaypointV2Error == null) {
                        sendCorrectMsg2Server(message, "stopMission success：" + "The mission has been stopped");
                    } else {
                        sendErrorMsg2Server(message, "stopMission fail：" + djiWaypointV2Error.getDescription());
                    }
                }
            });
        } else {
            sendErrorMsg2Server(message, "stopMission fail：" + waypointMissionOperator.getCurrentState());
        }
    }

    WaypointV2MissionOperator waypointMissionOperator;
    public void createWayPointV2Mission(DataInfo.Message message)  {
        setWayV2UpListener(message);
        DataInfo.FlightPath flightPath = message.getFlightPathControl().getFlightPath();
//        DataInfo.FlightPath flightPath = serializeMissionData(message);
        List<WaypointV2> waypointV2List = new ArrayList<>();
        if (flightPath != null && flightPath.getPointList() != null && flightPath.getPointList().size() > 0) {
            for (int i = 0; i < flightPath.getPointList().size(); i++) {
                double[] latLng = MapConvertUtils.getDJILatLng(Double.parseDouble(flightPath.getPointList().get(i).getLatitude()),
                        Double.parseDouble(flightPath.getPointList().get(i).getLongitude()));
                WaypointV2 waypoint = new WaypointV2.Builder()
                        .setDampingDistance(20f)
                        .setCoordinate(new LocationCoordinate2D(latLng[0], latLng[1]))
                        .setAltitude(Double.valueOf(flightPath.getPointList().get(i).getAltitude()))
                        .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.find(flightPath.getPointList().get(i).getFlightPointType()))
                        .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.find(flightPath.getPointList().get(i).getAircraftYawAngle()))
                        .setTurnMode(WaypointV2MissionTypes.WaypointV2TurnMode.find(flightPath.getPointList().get(i).getTurnMode()))
                        .setAutoFlightSpeed(flightPath.getPointList().get(i).getSpeed())
                        .build();
                waypointV2List.add(waypoint);
            }
            WaypointV2Mission.Builder waypointV2MissionBuilder = new WaypointV2Mission.Builder();
            waypointV2MissionBuilder.setMissionID(new Random().nextInt(65535))
                    .setMaxFlightSpeed(flightPath.getSpeed())
                    .setAutoFlightSpeed(flightPath.getSpeed())
                    .setFinishedAction(WaypointV2MissionTypes.MissionFinishedAction.find(flightPath.getFinishedAction()))
                    .setGotoFirstWaypointMode(WaypointV2MissionTypes.MissionGotoWaypointMode.SAFELY)
                    .setExitMissionOnRCSignalLostEnabled(true)
                    .setRepeatTimes(1)
                    .addwaypoints(waypointV2List);
            WaypointV2Mission waypointV2Mission = waypointV2MissionBuilder.build();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (waypointMissionOperator.getCurrentState().equals(WaypointV2MissionState.READY_TO_UPLOAD) || waypointMissionOperator.getCurrentState().equals(WaypointV2MissionState.READY_TO_EXECUTE)) {
                //加载
                waypointMissionOperator.loadMission(waypointV2Mission, new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                    @Override
                    public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                        if (djiWaypointV2Error == null) {
//                            sendCorrectMsg2Server(message, "loadMission success：uploading...");
                            uploadWayPointV2Mission(message);
                        } else {
                            Logger.e("loadMission fail：" + djiWaypointV2Error.getDescription());
                            sendErrorMsg2Server(message, "loadMission fail：" + djiWaypointV2Error.getDescription());
                        }
                    }
                });
            } else {
                Logger.e("loadMission fail：" + waypointMissionOperator.getCurrentState() + "：The mission can be loaded only when the operator state is READY_TO_UPLOAD or READY_TO_EXECUTE");
                sendErrorMsg2Server(message, "loadMission fail：" +waypointMissionOperator.getCurrentState() + "The mission can be loaded only when the operator state is READY_TO_UPLOAD or READY_TO_EXECUTE");
            }
        } else {
            Logger.e("loadMission fail：" + "Wrong parameter");
            sendErrorMsg2Server(message, "loadMission fail：" + "Wrong parameter");
        }
    }

    //上传航点任务
    private void uploadWayPointV2Mission(DataInfo.Message message) {
        waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
            @Override
            public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                if (djiWaypointV2Error == null) {
                    uploadWayPointV2Action(message);
                } else {
                    Logger.e("uploadMission fail：" + djiWaypointV2Error.getDescription());
                    sendErrorMsg2Server(message, "uploadMission fail：" + djiWaypointV2Error.getDescription());
                }
            }
        });
    }

    private WaypointTrigger waypointActionTrigger;
    private WaypointActuator waypointActionActuator;
    private int actionID;

    //上传航点动作
    private void uploadWayPointV2Action(DataInfo.Message message) {
        List<WaypointV2Action> waypointV2ActionList = new ArrayList<>();
        DataInfo.FlightPath flightPath = message.getFlightPathControl().getFlightPath();
        for (int i = 0; i < flightPath.getPointList().size(); i++) {
            DataInfo.FlightPoint flightPoint = flightPath.getPointList().get(i);
            for (int j = 0; j < flightPoint.getPointActionList().size(); j++) {
                actionID += 1;
                DataInfo.FlightPointAction pointAction = flightPoint.getPointActionList().get(j);
                if (j == 0) {
                    waypointActionTrigger = new WaypointTrigger.Builder()
                            .setTriggerType(ActionTypes.ActionTriggerType.REACH_POINT)
                            .setReachPointParam(new WaypointReachPointTriggerParam.Builder()
                                    .setStartIndex(i)
                                    .setAutoTerminateCount(i)
                                    .build())
                            .build();
                } else {
                    waypointActionTrigger = new WaypointTrigger.Builder()
                            .setTriggerType(ActionTypes.ActionTriggerType.ASSOCIATE)
                            .setAssociateParam(new WaypointV2AssociateTriggerParam.Builder()
                                    .setAssociateActionID(actionID - 1)
                                    .setAssociateType(ActionTypes.AssociatedTimingType.AFTER_FINISHED)
                                    .setWaitingTime(pointAction.getWaitingTime())
                                    .build())
                            .build();
                }
                switch (pointAction.getActionType()) {
                    case 0://云台角度
                        waypointActionActuator = new WaypointActuator.Builder()
                                .setActuatorType(ActionTypes.ActionActuatorType.GIMBAL)
                                .setGimbalActuatorParam(new WaypointGimbalActuatorParam.Builder()
                                        .operationType(ActionTypes.GimbalOperationType.ROTATE_GIMBAL)
                                        .rotation(new Rotation.Builder()
                                                .mode(RotationMode.ABSOLUTE_ANGLE)
                                                .pitch(pointAction.getRotationAngle())
                                                .roll(0)
                                                .yaw(0)
                                                .time(2)
                                                .build())
                                        .build())
                                .build();
                        break;
                    case 1://云台变焦
                        waypointActionActuator = new WaypointActuator.Builder()
                                .setActuatorType(ActionTypes.ActionActuatorType.CAMERA)
                                .setCameraActuatorParam(new WaypointCameraActuatorParam.Builder()
                                        .setCameraOperationType(ActionTypes.CameraOperationType.ZOOM)
                                        .setZoomParam(new WaypointCameraZoomParam.Builder()
                                                .setFocalLength(pointAction.getRotationAngle())
                                                .build())
                                        .build())
                                .build();
                        break;
                    case 2://拍照
                        waypointActionActuator = new WaypointActuator.Builder()
                                .setActuatorType(ActionTypes.ActionActuatorType.CAMERA)
                                .setCameraActuatorParam(new WaypointCameraActuatorParam.Builder()
                                        .setCameraOperationType(ActionTypes.CameraOperationType.SHOOT_SINGLE_PHOTO)
                                        .build())
                                .build();
                        break;
                    case 3://悬停
                        waypointActionActuator = new WaypointActuator.Builder()
                                .setActuatorType(ActionTypes.ActionActuatorType.AIRCRAFT_CONTROL)
                                .setAircraftControlActuatorParam(new WaypointAircraftControlParam.Builder()
                                        .setAircraftControlType(ActionTypes.AircraftControlType.START_STOP_FLY)
                                        .setFlyControlParam(new WaypointAircraftControlStartStopFlyParam.Builder()
                                                .setStartFly(false)
                                                .build())
                                        .build())
                                .build();
                        break;
                    case 4://旋转
                        waypointActionActuator = new WaypointActuator.Builder()
                                .setActuatorType(ActionTypes.ActionActuatorType.AIRCRAFT_CONTROL)
                                .setAircraftControlActuatorParam(new WaypointAircraftControlParam.Builder()
                                        .setAircraftControlType(ActionTypes.AircraftControlType.ROTATE_YAW)
                                        .setRotateYawParam(new WaypointAircraftControlRotateYawParam.Builder()
                                                .setYawAngle(pointAction.getRotationAngle())
                                                .setDirection(WaypointV2MissionTypes.WaypointV2TurnMode.find(pointAction.getRotationDirection()))
//                                            .setRelative()
                                                .build())
                                        .build())
                                .build();
                        break;
                    case 5://继续飞行
                        waypointActionActuator = new WaypointActuator.Builder()
                                .setActuatorType(ActionTypes.ActionActuatorType.AIRCRAFT_CONTROL)
                                .setAircraftControlActuatorParam(new WaypointAircraftControlParam.Builder()
                                        .setAircraftControlType(ActionTypes.AircraftControlType.START_STOP_FLY)
                                        .setFlyControlParam(new WaypointAircraftControlStartStopFlyParam.Builder()
                                                .setStartFly(true)
                                                .build())
                                        .build())
                                .build();
                        break;
                }
                WaypointV2Action waypointAction = new WaypointV2Action.Builder()
                        .setActionID(actionID)
                        .setTrigger(waypointActionTrigger)
                        .setActuator(waypointActionActuator)
                        .build();
                waypointV2ActionList.add(waypointAction);
            }
        }
//        if (waypointMissionOperator.getCurrentActionState().equals(ActionState.READY_TO_UPLOAD)) {
        Logger.e("uploadAction fail：准备上传航点动作，当前状态是" + waypointMissionOperator.getCurrentState());
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Logger.e("uploadAction fail：准备上传航点动作，休眠后当前状态是" + waypointMissionOperator.getCurrentState());
        Logger.e("uploadAction fail：准备上传航点动作，航点动作 list size ：" + waypointV2ActionList.size());
        if(waypointV2ActionList.size() == 0){
            Logger.e("uploadAction fail：准备上传航点动作,准备调用起飞");
            startWaypointsV2Mission(message);
            return;
        }
        waypointMissionOperator.uploadWaypointActions(waypointV2ActionList, new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                    if (djiWaypointV2Error == null) {
                        if (waypointMissionOperator.getCurrentState().equals(ActionState.READY_TO_EXECUTE)) {
//                            sendCorrectMsg2Server(message, "uploadAction success：" + "Can start mission");
                            Logger.e("uploadAction success："+ "Can start mission");
                            startWaypointsV2Mission(message);
                        } else {
                            int count=10;
                            Logger.e("uploadAction fail: while 循环前状态："+waypointMissionOperator.getCurrentState().name());

                            while((!waypointMissionOperator.getCurrentState().name().equals("READY_TO_EXECUTE"))&&(--count)>0){
                                Logger.e("uploadAction fail：waypointMissionOperator.getCurrentState()=" + waypointMissionOperator.getCurrentState());
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Logger.e("uploadAction fail：当前状态是" + waypointMissionOperator.getCurrentState()+",不满足条件，继续等待 READY_TO_EXECUTE ,count="+count);
                            }
                            if(count>0){
                                Logger.e("uploadAction fail: while 循环后状态"+waypointMissionOperator.getCurrentState().name()+" count="+count+" 休眠5秒");
                                try {
                                    TimeUnit.SECONDS.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Logger.e("uploadAction fail: 休眠5秒成功，调用起飞");

                                startWaypointsV2Mission(message);
                            }else {
                                Logger.e("uploadAction fail：" + waypointMissionOperator.getCurrentState());
                                sendErrorMsg2Server(message, "uploadAction fail：" + waypointMissionOperator.getCurrentState());
                            }
                        }
                    } else {
                        Logger.e("uploadAction fail：" + djiWaypointV2Error.getDescription());
                        sendErrorMsg2Server(message, "uploadAction fail：" + djiWaypointV2Error.getDescription());
                    }
                }
            });
//        } else {
//            Logger.e("uploadAction fail：" +  waypointMissionOperator.getCurrentState());
//            sendErrorMsg2Server(message, "uploadAction fail：" + waypointMissionOperator.getCurrentState());
//        }

    }

    private void setWayV2UpListener(DataInfo.Message message) {
        waypointMissionOperator.addActionListener(new WaypointV2ActionListener(message));
        waypointMissionOperator.addWaypointEventListener(new WaypointV2MissionOperatorListener(message));
    }

    //将悬停设置为每个航点的第一个航点动作
    private DataInfo.FlightPath serializeMissionData(DataInfo.Message message) {
        DataInfo.FlightPath flightPath = message.getFlightPathControl().getFlightPath();
        List<DataInfo.FlightPoint> wayPoints = flightPath.getPointList();
        for (int i = 0; i < wayPoints.size(); i++) {
            DataInfo.FlightPoint flightPoint = wayPoints.get(i);
            List<DataInfo.FlightPointAction> pointActionList = flightPoint.getPointActionList();
            for (int j = 0; j < pointActionList.size(); j++) {
                DataInfo.FlightPointAction action = pointActionList.get(j);
                if ("0".equals(action.getActionType())) {
                    flightPoint.getPointActionList().remove(j);
                    flightPoint.getPointActionList().add(0, action);
                    flightPath.getPointList().set(i, flightPoint);
                    break;
                }
            }
        }
        return flightPath;
    }

    /*******************************************************************************************************************************/

    private void startWaypointsV2Mission(DataInfo.Message message) {
        if (waypointMissionOperator.getCurrentState() == WaypointV2MissionState.READY_TO_EXECUTE) {
            waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
                @Override
                public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                    if (djiWaypointV2Error == null) {
                        Logger.e("uploadAction fail：调用起飞成功");
                        Movement.getInstance().setFlightPathName(message.getFlightPathControl().getFlightPath().getName());
                        sendCorrectMsg2Server(message, "startMission success");
                    } else {
                        Logger.e("uploadAction fail：调用起飞失败："+djiWaypointV2Error.getDescription());
                        sendErrorMsg2Server(message, "startMission fail：" + djiWaypointV2Error.getDescription());
                    }
                }
            });
        } else {
            sendErrorMsg2Server(message, "startMission fail：" + waypointMissionOperator.getCurrentState());
        }
    }
}
