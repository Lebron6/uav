package com.mg.uav.entity;

import java.util.List;

/**
 * 航线详情
 */
public class FlightInfo {

    private String msg;
    private Integer code;
    private DataDTO data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
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
        private Integer altitude;
        private Integer speed;
        private Integer finishedAction;
        private String imageUrl;
        private List<FlightPointListDTO> flightPointList;
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

        public Integer getAltitude() {
            return altitude;
        }

        public void setAltitude(Integer altitude) {
            this.altitude = altitude;
        }

        public Integer getSpeed() {
            return speed;
        }

        public void setSpeed(Integer speed) {
            this.speed = speed;
        }

        public Integer getFinishedAction() {
            return finishedAction;
        }

        public void setFinishedAction(Integer finishedAction) {
            this.finishedAction = finishedAction;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public List<FlightPointListDTO> getFlightPointList() {
            return flightPointList;
        }

        public void setFlightPointList(List<FlightPointListDTO> flightPointList) {
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

        public static class FlightPointListDTO {
            private String flightPointId;
            private String longitude;
            private String latitude;
            private Integer altitude;
            private Integer speed;
            private Integer turnMode;
            private Integer aircraftYawAngle;
            private Integer flightPointType;
            private String flightPathId;
            private List<FlightActionListDTO> flightActionList;
            private Integer pointOrder;
            private Boolean heightFollow;
            private Boolean speedFollow;

            public String getFlightPointId() {
                return flightPointId;
            }

            public void setFlightPointId(String flightPointId) {
                this.flightPointId = flightPointId;
            }

            public String getLongitude() {
                return longitude;
            }

            public void setLongitude(String longitude) {
                this.longitude = longitude;
            }

            public String getLatitude() {
                return latitude;
            }

            public void setLatitude(String latitude) {
                this.latitude = latitude;
            }

            public Integer getAltitude() {
                return altitude;
            }

            public void setAltitude(Integer altitude) {
                this.altitude = altitude;
            }

            public Integer getSpeed() {
                return speed;
            }

            public void setSpeed(Integer speed) {
                this.speed = speed;
            }

            public Integer getTurnMode() {
                return turnMode;
            }

            public void setTurnMode(Integer turnMode) {
                this.turnMode = turnMode;
            }

            public Integer getAircraftYawAngle() {
                return aircraftYawAngle;
            }

            public void setAircraftYawAngle(Integer aircraftYawAngle) {
                this.aircraftYawAngle = aircraftYawAngle;
            }

            public Integer getFlightPointType() {
                return flightPointType;
            }

            public void setFlightPointType(Integer flightPointType) {
                this.flightPointType = flightPointType;
            }

            public String getFlightPathId() {
                return flightPathId;
            }

            public void setFlightPathId(String flightPathId) {
                this.flightPathId = flightPathId;
            }

            public List<FlightActionListDTO> getFlightActionList() {
                return flightActionList;
            }

            public void setFlightActionList(List<FlightActionListDTO> flightActionList) {
                this.flightActionList = flightActionList;
            }

            public Integer getPointOrder() {
                return pointOrder;
            }

            public void setPointOrder(Integer pointOrder) {
                this.pointOrder = pointOrder;
            }

            public Boolean getHeightFollow() {
                return heightFollow;
            }

            public void setHeightFollow(Boolean heightFollow) {
                this.heightFollow = heightFollow;
            }

            public Boolean getSpeedFollow() {
                return speedFollow;
            }

            public void setSpeedFollow(Boolean speedFollow) {
                this.speedFollow = speedFollow;
            }

            public static class FlightActionListDTO {
                private String pkid;
                private Integer actionType;
                private Integer rotationAngle;
                private Integer waitingTime;
                private Object rotationDirection;
                private String flightPointId;

                public String getPkid() {
                    return pkid;
                }

                public void setPkid(String pkid) {
                    this.pkid = pkid;
                }

                public Integer getActionType() {
                    return actionType;
                }

                public void setActionType(Integer actionType) {
                    this.actionType = actionType;
                }

                public Integer getRotationAngle() {
                    return rotationAngle;
                }

                public void setRotationAngle(Integer rotationAngle) {
                    this.rotationAngle = rotationAngle;
                }

                public Integer getWaitingTime() {
                    return waitingTime;
                }

                public void setWaitingTime(Integer waitingTime) {
                    this.waitingTime = waitingTime;
                }

                public Object getRotationDirection() {
                    return rotationDirection;
                }

                public void setRotationDirection(Object rotationDirection) {
                    this.rotationDirection = rotationDirection;
                }

                public String getFlightPointId() {
                    return flightPointId;
                }

                public void setFlightPointId(String flightPointId) {
                    this.flightPointId = flightPointId;
                }
            }
        }
    }
}
