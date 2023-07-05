package com.mg.uav.manager;

import com.mg.uav.app.DJIApp;
import com.mg.uav.base.BaseManager;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.global.DataCache;
import com.orhanobut.logger.Logger;

import dji.common.error.DJIError;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;

/**
 * 云台
 */
public class GimbalManager extends BaseManager {
    private GimbalManager() {
    }

    private static class GimbalControlHolder {
        private static final GimbalManager INSTANCE = new GimbalManager();
    }

    public static GimbalManager getInstance() {
        return GimbalControlHolder.INSTANCE;
    }


    //向上
    public void upwardType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.RELATIVE_ANGLE).time(2);
                builder.pitch(DataCache.getInstance().getHolderSpeedType());
                builder.build();
                gimbal.rotate(builder.build(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台向上...");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            } else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //向下
    public void downType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.RELATIVE_ANGLE).time(2);
                builder.pitch(( DataCache.getInstance().getHolderSpeedType()*-1));
                builder.build();
                gimbal.rotate(builder.build(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台向下...");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            }else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //向左
    public void toLeftType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.RELATIVE_ANGLE).time(2);
                builder.yaw(  DataCache.getInstance().getHolderSpeedType()*-1);
                builder.build();
                gimbal.rotate(builder.build(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台向左...");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            }else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //向右
    public void toRightType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.RELATIVE_ANGLE).time(2);
                builder.yaw(DataCache.getInstance().getHolderSpeedType());
                builder.build();
                gimbal.rotate(builder.build(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台向右...");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            } else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //水平归中
    public void levelToCenterType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                gimbal.resetYaw(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台已水平归中");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            } else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }


    //归中
    public void centerType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                gimbal.reset(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台已归中");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            }else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //垂直归中
    public void verticalCenterType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(2);
                builder.pitch(Float.parseFloat("0"));
                builder.build();
                gimbal.rotate(builder.build(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台已垂直归中");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            }else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //偏航朝下 ABSOLUTE_ANGLE：云台旋转时相对于0度（飞机航向）的角度值。  RELATIVE_ANGLE:云台旋转时相对于当前角度的角度值
    public void yawDownType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                Logger.e("云台偏航朝下"+Float.parseFloat("-90"));

                Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(2);
                builder.pitch(Float.parseFloat("-90"));
                builder.build();
                gimbal.rotate(builder.build(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台已朝下");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            } else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //云台向下// 暂时理解为水平且垂直朝下
    public void holderDownType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Gimbal gimbal = aircraft.getGimbal();
            if (gimbal != null) {
                Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(2);
                builder.pitch(Float.parseFloat("-90"));
                builder.yaw(Float.parseFloat("0"));
                builder.build();
                gimbal.rotate(builder.build(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"云台已向下");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            } else {
                sendErrorMsg2Server(message,"gimbal is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //云台转速
    public void holderSpeedType(DataInfo.Message message) {
        DataCache.getInstance().setHolderSpeedType(message.getHolderControl().getSpeed());
        sendCorrectMsg2Server(message,"云台转速已更新");
    }

}
