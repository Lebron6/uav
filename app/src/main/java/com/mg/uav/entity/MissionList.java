package com.mg.uav.entity;

import java.util.List;

public class MissionList {

    private String msg;
    private int code;
    private List<DataDTO> data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<DataDTO> getData() {
        return data;
    }

    public void setData(List<DataDTO> data) {
        this.data = data;
    }

    public static class DataDTO {
        private String createTime;
        private String updateTime;
        private String createBy;
        private String updateBy;
        private String flightPathId;
        private String uavId;
        private String name;
        private String altitude;
        private String speed;
        private String finishedAction;
        private String imageUrl;
        private String flightPointList;
        private String uavName;
        private String flightPointJson;

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public String getUpdateBy() {
            return updateBy;
        }

        public void setUpdateBy(String updateBy) {
            this.updateBy = updateBy;
        }

        public String getFlightPathId() {
            return flightPathId;
        }

        public void setFlightPathId(String flightPathId) {
            this.flightPathId = flightPathId;
        }

        public String getUavId() {
            return uavId;
        }

        public void setUavId(String uavId) {
            this.uavId = uavId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAltitude() {
            return altitude;
        }

        public void setAltitude(String altitude) {
            this.altitude = altitude;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public String getFinishedAction() {
            return finishedAction;
        }

        public void setFinishedAction(String finishedAction) {
            this.finishedAction = finishedAction;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getFlightPointList() {
            return flightPointList;
        }

        public void setFlightPointList(String flightPointList) {
            this.flightPointList = flightPointList;
        }

        public String getUavName() {
            return uavName;
        }

        public void setUavName(String uavName) {
            this.uavName = uavName;
        }

        public String getFlightPointJson() {
            return flightPointJson;
        }

        public void setFlightPointJson(String flightPointJson) {
            this.flightPointJson = flightPointJson;
        }
    }
}
