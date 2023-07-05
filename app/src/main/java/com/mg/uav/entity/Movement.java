package com.mg.uav.entity;


public class Movement {

    private static class MovementHolder {
        private static final Movement INSTANCE = new Movement();
    }

    private Movement (){}

    public static final Movement getInstance() {
        return MovementHolder.INSTANCE;
    }


    private int distance;//距离航点
    private int horizontalSpeed;//水平速度
    private int verticalSpeed;//垂直速度
    private int windSpeed;//风速
    private int flyingHeight;//飞行高度
    private boolean rtkSign;//rtk标志
    private int satelliteNumber;//卫星数量
    private boolean levelObstacleAvoidance;//水平避障
    private int remoteControlSignal;//遥控器信号
    private int pictureBiographySignal;//图传信号
    private int electricityInfoA;//A电量信息
    private int voltageInfoA;//A电压信息
    private int electricityInfoB;//B电量信息
    private int voltageInfoB;//B电压信息
    private int currentView=0;//当前视角 1FPV 0云台
    private String currentLongitude="";//当前经度
    private String currentLatitude="";//当前纬度
    private boolean planeWing;//当前飞机的桨叶是否转动
    private String planeMessage="";//飞机提示信息
    private boolean airlineFlight;//是否航线飞行
    private String warningMessage="";//飞机警告信息
    private int flightPathStatus;//航线状态
    private int angleYaw;//飞机飞行机头角度
    private int cameraMode;//相机模式 拍照/录像
    private int cameraVideoStreamSource;//相机模式 红外/广角/变焦
    private String flightPathName="";//航线名称

    public int getCameraVideoStreamSource() {
        return cameraVideoStreamSource;
    }

    public void setCameraVideoStreamSource(int cameraVideoStreamSource) {
        this.cameraVideoStreamSource = cameraVideoStreamSource;
    }

    private String planeMode="";//飞机模式

    public String getPlaneMode() {
        return planeMode;
    }

    public void setPlaneMode(String planeMode) {
        this.planeMode = planeMode;
    }


    public String getFlightPathName() {
        return flightPathName;
    }

    public void setFlightPathName(String flightPathName) {
        this.flightPathName = flightPathName;
    }

    public int getCameraMode() {
        return cameraMode;
    }

    public void setCameraMode(int cameraMode) {
        this.cameraMode = cameraMode;
    }

    public int getAngleYaw() {
        return angleYaw;
    }

    public void setAngleYaw(int angleYaw) {
        this.angleYaw = angleYaw;
    }

    public int getFlightPathStatus() {
        return flightPathStatus;
    }

    public void setFlightPathStatus(int flightPathStatus) {
        this.flightPathStatus = flightPathStatus;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public int getLiveStatus() {
        return liveStatus;
    }

    public void setLiveStatus(int liveStatus) {
        this.liveStatus = liveStatus;
    }

    private int liveStatus;//推流状态 0失败  1成功

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getHorizontalSpeed() {
        return horizontalSpeed;
    }

    public void setHorizontalSpeed(int horizontalSpeed) {
        this.horizontalSpeed = horizontalSpeed;
    }

    public int getVerticalSpeed() {
        return verticalSpeed;
    }

    public void setVerticalSpeed(int verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getFlyingHeight() {
        return flyingHeight;
    }

    public void setFlyingHeight(int flyingHeight) {
        this.flyingHeight = flyingHeight;
    }

    public boolean isRtkSign() {
        return rtkSign;
    }

    public void setRtkSign(boolean rtkSign) {
        this.rtkSign = rtkSign;
    }

    public int getSatelliteNumber() {
        return satelliteNumber;
    }

    public void setSatelliteNumber(int satelliteNumber) {
        this.satelliteNumber = satelliteNumber;
    }


    public int getRemoteControlSignal() {
        return remoteControlSignal;
    }

    public void setRemoteControlSignal(int remoteControlSignal) {
        this.remoteControlSignal = remoteControlSignal;
    }

    public int getPictureBiographySignal() {
        return pictureBiographySignal;
    }

    public void setPictureBiographySignal(int pictureBiographySignal) {
        this.pictureBiographySignal = pictureBiographySignal;
    }

    public int getElectricityInfoA() {
        return electricityInfoA;
    }

    public void setElectricityInfoA(int electricityInfoA) {
        this.electricityInfoA = electricityInfoA;
    }

    public int getVoltageInfoA() {
        return voltageInfoA;
    }

    public void setVoltageInfoA(int voltageInfoA) {
        this.voltageInfoA = voltageInfoA;
    }

    public int getElectricityInfoB() {
        return electricityInfoB;
    }

    public void setElectricityInfoB(int electricityInfoB) {
        this.electricityInfoB = electricityInfoB;
    }

    public int getVoltageInfoB() {
        return voltageInfoB;
    }

    public void setVoltageInfoB(int voltageInfoB) {
        this.voltageInfoB = voltageInfoB;
    }

    public int getCurrentView() {
        return currentView;
    }

    public void setCurrentView(int currentView) {
        this.currentView = currentView;
    }

    public String getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(String currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public String getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(String currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public boolean isPlaneWing() {
        return planeWing;
    }

    public void setPlaneWing(boolean planeWing) {
        this.planeWing = planeWing;
    }

    public String getPlaneMessage() {
        return planeMessage;
    }

    public void setPlaneMessage(String planeMessage) {
        this.planeMessage = planeMessage;
    }

    public boolean isAirlineFlight() {
        return airlineFlight;
    }

    public void setAirlineFlight(boolean airlineFlight) {
        this.airlineFlight = airlineFlight;
    }

    public boolean isLevelObstacleAvoidance() {
        return levelObstacleAvoidance;
    }

    public void setLevelObstacleAvoidance(boolean levelObstacleAvoidance) {
        this.levelObstacleAvoidance = levelObstacleAvoidance;
    }


    public DataInfo.Movement myMovement2DataInfoMovement() {

        return DataInfo.Movement.newBuilder()
                .setLiveStatus(liveStatus)
                .setFlightPathStatus(flightPathStatus)
                .setWarningMessage(warningMessage)
                .setAirlineFlight(airlineFlight)
                .setPlaneMessage(planeMessage)
                .setPlaneWing(planeWing)
                .setCurrentLatitude(currentLatitude)
                .setCurrentLongitude(currentLongitude)
                .setCurrentView(currentView)
                .setVoltageInfoB(voltageInfoB)
                .setElectricityInfoB(electricityInfoB)
                .setVoltageInfoA(voltageInfoA)
                .setElectricityInfoA(electricityInfoA)
                .setPictureBiographySignal(pictureBiographySignal)
                .setRemoteControlSignal(remoteControlSignal)
                .setLevelObstacleAvoidance(levelObstacleAvoidance)
                .setSatelliteNumber(satelliteNumber)
                .setRtkSign(rtkSign)
                .setFlyingHeight(flyingHeight)
                .setWindSpeed(windSpeed)
                .setVerticalSpeed(verticalSpeed)
                .setHorizontalSpeed(horizontalSpeed)
                .setDistance(distance)
                .setAngleYaw(angleYaw)
                .setCameraMode(cameraMode)
                .setFlightPathName(flightPathName)
                .setPlaneMode(planeMode)
                .build();
    }

}
