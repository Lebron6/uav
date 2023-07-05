package com.mg.uav.entity;

import java.util.List;

public class MissionUploadData {
   private String name="";
    private String uavId="";
    private String speed="";
    private String altitude="";
    private String finishedAction="";
    private String flightPathId="";

    public String getFlightPathId() {
        return flightPathId;
    }

    public void setFlightPathId(String flightPathId) {
        this.flightPathId = flightPathId;
    }

    private String flightPointJson="";
    private List<Waypoint> waypoints;

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUavId() {
        return uavId;
    }

    public void setUavId(String uavId) {
        this.uavId = uavId;
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

    public String getFinishedAction() {
        return finishedAction;
    }

    public void setFinishedAction(String finishedAction) {
        this.finishedAction = finishedAction;
    }

    public String getFlightPointJson() {
        return flightPointJson;
    }

    public void setFlightPointJson(String flightPointJson) {
        this.flightPointJson = flightPointJson;
    }
    public static class Waypoint{
        private String speed="5";
        private String altitude="120";
        private boolean speedFollow;
        private boolean heightFollow;
        private String turnMode="1";
        private String aircraftYawAngle="0";
        private String flightPointType="0";
        private String longitude;
        private String latitude;
        private String flightPointId;

        public String getFlightPointId() {
            return flightPointId;
        }

        public void setFlightPointId(String flightPointId) {
            this.flightPointId = flightPointId;
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

        public boolean isSpeedFollow() {
            return speedFollow;
        }

        public void setSpeedFollow(boolean speedFollow) {
            this.speedFollow = speedFollow;
        }

        public boolean isHeightFollow() {
            return heightFollow;
        }

        public void setHeightFollow(boolean heightFollow) {
            this.heightFollow = heightFollow;
        }

        public String getTurnMode() {
            return turnMode;
        }

        public void setTurnMode(String turnMode) {
            this.turnMode = turnMode;
        }

        public String getAircraftYawAngle() {
            return aircraftYawAngle;
        }

        public void setAircraftYawAngle(String aircraftYawAngle) {
            this.aircraftYawAngle = aircraftYawAngle;
        }

        public String getFlightPointType() {
            return flightPointType;
        }

        public void setFlightPointType(String flightPointType) {
            this.flightPointType = flightPointType;
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
    }
}
