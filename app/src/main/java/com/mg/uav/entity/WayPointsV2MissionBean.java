package com.mg.uav.entity;

import java.util.List;

public class WayPointsV2MissionBean {

    private String speed;
    private String finishedAction;
    private List<WayPointsBean> wayPoints;

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

    public List<WayPointsBean> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(List<WayPointsBean> wayPoints) {
        this.wayPoints = wayPoints;
    }


    public static class WayPointsBean {
        /**
         * speed : 5
         * altitude : 120
         * latitude : 31.32584
         * turnMode : -1
         * longitude : 120.547518
         * flightPathMode :
         * headingMode :
         * hasWaitingTime;
         * hasVoice
         * wayPointAction : [{"actionType":"","yawAngle":"","direction":"","pitch":"","yaw":"","focalLength":"","waitingTime":""}]
         */
        private String hasWaitingTime;
        private String speed;
        private String altitude;
        private String latitude;
        private String turnMode;
        private String longitude;
        private String flightPathMode;
        private String headingMode;

        private List<WayPointActionBean> wayPointAction;

        public String getHasWaitingTime() {
            return hasWaitingTime;
        }

        public void setHasWaitingTime(String hasWaitingTime) {
            this.hasWaitingTime = hasWaitingTime;
        }


        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public String getAltitude() {
            return altitude;
        }

        public void setAltitude(String altitude) {
            this.altitude = altitude;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getTurnMode() {
            return turnMode;
        }

        public void setTurnMode(String turnMode) {
            this.turnMode = turnMode;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getFlightPathMode() {
            return flightPathMode;
        }

        public void setFlightPathMode(String flightPathMode) {
            this.flightPathMode = flightPathMode;
        }

        public String getHeadingMode() {
            return headingMode;
        }

        public void setHeadingMode(String headingMode) {
            this.headingMode = headingMode;
        }

        public List<WayPointActionBean> getWayPointAction() {
            return wayPointAction;
        }

        public void setWayPointAction(List<WayPointActionBean> wayPointAction) {
            this.wayPointAction = wayPointAction;
        }

        public static class WayPointActionBean {
            /**
             * actionType :
             * yawAngle :
             * direction :
             * pitch :
             * yaw :
             * focalLength :
             * waitingTime :
             */

            private String actionType;
            private String yawAngle;
            private String direction;
            private String pitch;
            private String yaw;
            private String focalLength;
            private String waitingTime;


            public String getActionType() {
                return actionType;
            }

            public void setActionType(String actionType) {
                this.actionType = actionType;
            }

            public String getYawAngle() {
                return yawAngle;
            }

            public void setYawAngle(String yawAngle) {
                this.yawAngle = yawAngle;
            }

            public String getDirection() {
                return direction;
            }

            public void setDirection(String direction) {
                this.direction = direction;
            }

            public String getPitch() {
                return pitch;
            }

            public void setPitch(String pitch) {
                this.pitch = pitch;
            }

            public String getYaw() {
                return yaw;
            }

            public void setYaw(String yaw) {
                this.yaw = yaw;
            }

            public String getFocalLength() {
                return focalLength;
            }

            public void setFocalLength(String focalLength) {
                this.focalLength = focalLength;
            }

            public String getWaitingTime() {
                return waitingTime;
            }

            public void setWaitingTime(String waitingTime) {
                this.waitingTime = waitingTime;
            }



        }

    }


}
