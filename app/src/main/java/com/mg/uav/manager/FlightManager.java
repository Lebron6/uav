package com.mg.uav.manager;

import com.mg.uav.app.DJIApp;
import com.mg.uav.base.BaseManager;
import com.mg.uav.constant.Constant;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.entity.Movement;
import com.mg.uav.global.DataCache;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

/**
 * 飞控
 */
public class FlightManager extends BaseManager {

    private FlightManager() {
    }

    private static class FlightControlHolder {
        private static final FlightManager INSTANCE = new FlightManager();
    }

    public static FlightManager getInstance() {
        return FlightControlHolder.INSTANCE;
    }

    //起飞
    public void takeOffType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.startTakeoff(djiError -> {
                    if (djiError == null) {
                       sendCorrectMsg2Server(message,"起飞成功");
                    } else {
                        sendErrorMsg2Server(message,djiError.getDescription());
                    }
                });
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //上升  (float) message.getSpeedControl().getRisingFalling()
    public void risingType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                0, 0, 0,
                               Math.abs(DataCache.getInstance().getDefault_rising_falling())
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    sendCorrectMsg2Server(message,"上升中...");
                                } else {
                                    sendErrorMsg2Server(message,djiError.getDescription());
                                }
                            }
                        }
                );
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }

    }

    //下降
    public void fallingType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                0, 0, 0,
                                Math.abs(DataCache.getInstance().getDefault_rising_falling())*-1
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    sendCorrectMsg2Server(message,"下降中...");
                                } else {
                                    sendErrorMsg2Server(message,djiError.getDescription());
                                }
                            }
                        }
                );
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //左旋
    public void leftHanded(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                0, 0,
                                Math.abs(DataCache.getInstance().getDefault_left_right_handed())*-1
                                , 0
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    sendCorrectMsg2Server(message,"左旋中...");
                                } else {
                                    sendErrorMsg2Server(message,djiError.getDescription());
                                }
                            }
                        }
                );
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //右旋
    public void rightHandedType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                0, 0,
                              Math.abs(DataCache.getInstance().getDefault_left_right_handed())
                                , 0
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    sendCorrectMsg2Server(message,"右旋中...");
                                } else {
                                    sendErrorMsg2Server(message,djiError.getDescription());
                                }
                            }
                        }
                );
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //向前
    public void forwardType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                0,
                               Math.abs(DataCache.getInstance().getDefault_forward_backward())
                                , 0, 0
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    sendCorrectMsg2Server(message,"向前中...");
                                } else {
                                    sendErrorMsg2Server(message,djiError.getDescription());
                                }
                            }
                        }
                );
            }else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //向后
    public void backwardType(DataInfo.Message message) {
        Logger.e("向后:"+DataCache.getInstance().getDefault_forward_backward()*-1);
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                0,
                                Math.abs(DataCache.getInstance().getDefault_forward_backward())*-1
                                , 0, 0
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    sendCorrectMsg2Server(message,"向后中...");
                                } else {
                                    sendErrorMsg2Server(message,djiError.getDescription());
                                }
                            }
                        }
                );
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //向左
    public void leftType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                Math.abs(DataCache.getInstance().getDefault_left_right())*-1
                                , 0, 0, 0
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    sendCorrectMsg2Server(message,"向左中...");
                                } else {
                                    sendErrorMsg2Server(message,djiError.getDescription());
                                }
                            }
                        }
                );
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //向右
    public void rightType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                Math.abs( DataCache.getInstance().getDefault_left_right())
                                , 0, 0, 0
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    sendCorrectMsg2Server(message,"向右中...");
                                } else {
                                    sendErrorMsg2Server(message,djiError.getDescription());
                                }
                            }
                        }
                );
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //自动返回
    public void autoReturnType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                flightController.startGoHome(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message,"开始返航");
                        } else {
                            sendErrorMsg2Server(message,djiError.getDescription());
                        }
                    }
                });
            } else {
                sendErrorMsg2Server(message,"flightController is null");
            }
        } else {
            sendErrorMsg2Server(message,"aircraft disconnect");
        }
    }

    //切换云台/FPV
    public void visualAngleType(DataInfo.Message message) {
        Movement.getInstance().setCurrentView(message.getUavControl().getData());
        EventBus.getDefault().post(Constant.VISUAL_ANGLE_TYPE);
        sendCorrectMsg2Server(message,"视角已切换");
    }

}
