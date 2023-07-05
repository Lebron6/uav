package com.mg.uav.base;

import com.google.gson.Gson;

import com.mg.uav.client.ChannelCache;
import com.mg.uav.constant.SocketConfig;
import com.mg.uav.entity.DataInfo;
import com.mg.uav.tools.PreferenceUtils;
import com.orhanobut.logger.Logger;

public abstract class BaseManager {
    public void sendErrorMsg2Server(DataInfo.Message message, String result) {

        DataInfo.Message msg = DataInfo.Message.newBuilder()
                .setDataType(DataInfo.Message.DataType.RequestResponseType)
                .setFlyNum(PreferenceUtils.getInstance().getFlyNumber())
                .setId(message.getId())
                .setRequestResponse(
                        DataInfo.RequestResponse.newBuilder()
                                .setStatus(-1)
                                .setResponseTime(System.currentTimeMillis())
                                .setResponseType(DataInfo.RequestResponse.ResponseType.OtherResponseType)
                                .setOther(
                                        DataInfo.OtherResponse.newBuilder()
                                                .setMsg(result).build()
                                ).build()

                ).build();
        ChannelCache.send(msg);
    }

    public void sendCorrectMsg2Server(DataInfo.Message message, String result) {
        DataInfo.Message msg = DataInfo.Message.newBuilder()
                .setDataType(DataInfo.Message.DataType.RequestResponseType)
                .setFlyNum(PreferenceUtils.getInstance().getFlyNumber())
                .setId(message.getId())
                .setRequestResponse(
                        DataInfo.RequestResponse.newBuilder()
                                .setStatus(200)
                                .setResponseTime(System.currentTimeMillis())
                                .setResponseType(DataInfo.RequestResponse.ResponseType.OtherResponseType)
                                .setOther(
                                        DataInfo.OtherResponse.newBuilder()
                                                .setMsg(result).build()
                                ).build()

                ).build();
        ChannelCache.send(msg);
    }

}
