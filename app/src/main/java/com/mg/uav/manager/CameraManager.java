package com.mg.uav.manager;

import com.mg.uav.app.DJIApp;
import com.mg.uav.base.BaseManager;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.entity.Movement;

import dji.common.camera.CameraVideoStreamSource;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.products.Aircraft;

import static dji.common.camera.CameraVideoStreamSource.DEFAULT;
import static dji.common.camera.CameraVideoStreamSource.INFRARED_THERMAL;
import static dji.common.camera.CameraVideoStreamSource.WIDE;
import static dji.common.camera.CameraVideoStreamSource.ZOOM;

/**
 * 摄像机
 */
public class CameraManager extends BaseManager {
    private CameraManager() {
    }

    private static class CameraManagerHolder {
        private static final CameraManager INSTANCE = new CameraManager();
    }

    public static CameraManager getInstance() {
        return CameraManager.CameraManagerHolder.INSTANCE;
    }

    //控制切换 1变焦 2广角 3红外
    public void controlSwitchType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Camera camera = aircraft.getCamera();
            if (camera != null) {
                if (camera.isMultiLensCameraSupported()) {
                    CameraVideoStreamSource source = DEFAULT;
                    switch (message.getCameraControl().getData()) {
                        case 1:
                            source = ZOOM;
                            break;
                        case 2:
                            source = WIDE;
                            break;
                        case 3:
                            source = INFRARED_THERMAL;
                            break;
                    }
                    camera.setCameraVideoStreamSource(source, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                sendCorrectMsg2Server(message, "切换成功");
                            } else {
                                sendErrorMsg2Server(message, djiError.getDescription());
                            }
                        }
                    });

                } else {
                    sendErrorMsg2Server(message, "product not supported");
                }
            } else {
                sendErrorMsg2Server(message, "camera is null");
            }
        } else {
            sendErrorMsg2Server(message, "aircraft disconnect");
        }
    }

    //模式切换 1拍照模式 2录像模式
    public void modeSwitchType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Camera camera = aircraft.getCamera();
            if (camera != null) {
                SettingsDefinitions.CameraMode cameraMode = SettingsDefinitions.CameraMode.SHOOT_PHOTO;
                switch (message.getCameraControl().getData()) {
                    case 1:
                        cameraMode = SettingsDefinitions.CameraMode.SHOOT_PHOTO;
                        break;
                    case 2:
                        cameraMode = SettingsDefinitions.CameraMode.RECORD_VIDEO;
                        break;
                }
                camera.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            Movement.getInstance().setCameraMode(message.getCameraControl().getData());
                            sendCorrectMsg2Server(message, "切换成功");
                        } else {
                            sendErrorMsg2Server(message, djiError.getDescription());
                        }
                    }
                });
            } else {
                sendErrorMsg2Server(message, "camera is null");
            }
        } else {
            sendErrorMsg2Server(message, "aircraft disconnect");
        }
    }

    //拍照
    public void takePhotosVideosType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Camera camera = aircraft.getCamera();
            if (camera != null) {
                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            sendCorrectMsg2Server(message, "拍摄成功");
                        } else {
                            sendErrorMsg2Server(message, djiError.getDescription());
                        }
                    }
                });
            } else {
                sendErrorMsg2Server(message, "camera is null");
            }
        } else {
            sendErrorMsg2Server(message, "aircraft disconnect");
        }
    }

    //对焦模式 1 手动 2 自动 3 AFC
    public void focusModeType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Camera camera = aircraft.getCamera();
            if (camera != null) {
                if (camera.isMultiLensCameraSupported()) {
                    SettingsDefinitions.FocusMode focusMode = SettingsDefinitions.FocusMode.AUTO;
                    switch (message.getCameraControl().getData()) {
                        case 3:
                            focusMode = SettingsDefinitions.FocusMode.MANUAL;
                            break;
                        case 1:
                            focusMode = SettingsDefinitions.FocusMode.AUTO;
                            break;
                        case 2:
                            focusMode = SettingsDefinitions.FocusMode.AFC;
                            break;
                    }
                    camera.getLens(0).setFocusMode(focusMode, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                sendCorrectMsg2Server(message, "对焦模式已切换");
                            } else {
                                sendErrorMsg2Server(message, djiError.getDescription());
                            }
                        }
                    });
                } else {
                    sendErrorMsg2Server(message, "product not supported");
                }
            } else {
                sendErrorMsg2Server(message, "camera is null");
            }
        } else {
            sendErrorMsg2Server(message, "aircraft disconnect");
        }
    }

    //锁定曝光 1锁定
    public void lockExposureType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Camera camera = aircraft.getCamera();
            if (camera != null) {
                if (camera.isMultiLensCameraSupported()) {
                    boolean exposure = false;
                    switch (message.getCameraControl().getData()) {
                        case 1:
                            exposure = true;
                            break;
                        case 0:
                            exposure = false;
                            break;
                    }
                    camera.getLens(0).setAELock(exposure, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                sendCorrectMsg2Server(message, message.getCameraControl().getData()==1 ?"锁定曝光":"解锁曝光");
                            } else {
                                sendErrorMsg2Server(message, djiError.getDescription());
                            }
                        }
                    });
                } else {
                    sendErrorMsg2Server(message, "product not supported");
                }
            } else {
                sendErrorMsg2Server(message, "camera is null");
            }
        } else {
            sendErrorMsg2Server(message, "aircraft disconnect");
        }
    }

    //变焦
    public void focalLengthType(DataInfo.Message message) {
        Aircraft aircraft = DJIApp.getAircraftInstance();
        if (aircraft != null) {
            Camera camera = aircraft.getCamera();
            if (camera != null) {
               if (camera.isMultiLensCameraSupported()){//是否支持多摄像头
                   camera.getLens(0).setHybridZoomFocalLength(getbigZoomValue(message.getCameraControl().getData()/10), new CommonCallbacks.CompletionCallback() {
                       @Override
                       public void onResult(DJIError djiError) {
                           if (djiError == null) {
                               sendCorrectMsg2Server(message, "变焦成功");
                           } else {
                               sendErrorMsg2Server(message, djiError.getDescription());
                           }
                       }
                   });
               }else if(camera.isHybridZoomSupported()){//是否支持混合变焦(光学/数码)
                   camera.setHybridZoomFocalLength(message.getCameraControl().getData()/10, new CommonCallbacks.CompletionCallback() {
                       @Override
                       public void onResult(DJIError djiError) {
                           if (djiError == null) {
                               sendCorrectMsg2Server(message, "变焦成功");
                           } else {
                               sendErrorMsg2Server(message, djiError.getDescription());
                           }
                       }
                   });
               }else if (camera.isTapZoomSupported()){//是否支持TapZoom,目前只有Z30支持
                   camera.setTapZoomMultiplier(message.getCameraControl().getData()/10, new CommonCallbacks.CompletionCallback() {
                       @Override
                       public void onResult(DJIError djiError) {
                           if (djiError == null) {
                               sendCorrectMsg2Server(message, "变焦成功");
                           } else {
                               sendErrorMsg2Server(message, djiError.getDescription());
                           }
                       }
                   });
               }
            } else {
                sendErrorMsg2Server(message, "camera is null");
            }
        } else {
            sendErrorMsg2Server(message, "aircraft disconnect");
        }
    }

    /**
     * //修改变焦数据为从前端拿2-200自己计算然后放入官方的sdk
     * @param smallZoomFromWeb
     * @return
     */
    public int getbigZoomValue(int smallZoomFromWeb) {
        int bigZoom = (47549 - 317) / 199 * (smallZoomFromWeb - 2) + 317;
        return bigZoom;
    }
}
