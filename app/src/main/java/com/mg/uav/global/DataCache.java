package com.mg.uav.global;

import com.mg.uav.entity.DataInfo;
import com.mg.uav.entity.Movement;

public class DataCache {

    private static class DataCacheHolder {
        private static final DataCache INSTANCE = new DataCache();
    }

    private DataCache (){}

    public static final DataCache getInstance() {
        return DataCache.DataCacheHolder.INSTANCE;
    }

    //上升/下降速度
    private int rising_falling;

    //向前/向后速度
    private int forward_backward;

    //向左/向右速度
    private int left_right;

    //左旋/右旋速度
    private int left_right_handed;

    //默认上升/下降速度
    private  int default_rising_falling;

    //默认向前/向后速度
    private  int default_forward_backward;

    //默认向左/向右速度
    private  int default_left_right;

    //默认左旋/右旋速度
    private  int default_left_right_handed;

    //rtmp地址
    private  String rtmp_address;

    //云台转速
    private int holderSpeedType;

    public int getHolderSpeedType() {
        return holderSpeedType;
    }

    public void setHolderSpeedType(int holderSpeedType) {
        this.holderSpeedType = holderSpeedType;
    }

    public int getDefault_rising_falling() {
        return default_rising_falling;
    }

    public void setDefault_rising_falling(int default_rising_falling) {
        this.default_rising_falling = default_rising_falling;
    }

    public int getDefault_forward_backward() {
        return default_forward_backward;
    }

    public void setDefault_forward_backward(int default_forward_backward) {
        this.default_forward_backward = default_forward_backward;
    }

    public int getDefault_left_right() {
        return default_left_right;
    }

    public void setDefault_left_right(int default_left_right) {
        this.default_left_right = default_left_right;
    }

    public int getDefault_left_right_handed() {
        return default_left_right_handed;
    }

    public void setDefault_left_right_handed(int default_left_right_handed) {
        this.default_left_right_handed = default_left_right_handed;
    }

    public String getRtmp_address() {
        return rtmp_address;
    }

    public void setRtmp_address(String rtmp_address) {
        this.rtmp_address = rtmp_address;
    }

    public int getRising_falling() {
        return rising_falling;
    }

    public void setRising_falling(int rising_falling) {
        this.rising_falling = rising_falling;
    }

    public int getForward_backward() {
        return forward_backward;
    }

    public void setForward_backward(int forward_backward) {
        this.forward_backward = forward_backward;
    }

    public int getLeft_right() {
        return left_right;
    }

    public void setLeft_right(int left_right) {
        this.left_right = left_right;
    }

    public int getLeft_right_handed() {
        return left_right_handed;
    }

    public void setLeft_right_handed(int left_right_handed) {
        this.left_right_handed = left_right_handed;
    }

//    public static DataInfo.Movement movement = DataInfo.Movement.newBuilder().build();
//
//    public static DataInfo.Message message = DataInfo.Message.newBuilder().setDataTypeValue(0).setMovement(movement).build();

}
