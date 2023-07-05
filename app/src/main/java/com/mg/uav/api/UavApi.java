package com.mg.uav.api;


import com.mg.uav.entity.FlightInfo;
import com.mg.uav.entity.LoginValues;
import com.mg.uav.entity.MissionList;
import com.mg.uav.entity.FlightSaveResult;
import com.mg.uav.entity.StreamStatus;
import com.mg.uav.entity.StreamValues;
import com.mg.uav.entity.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by James on 2021/10/15.
 */

public interface UavApi {

    /**
     * 登录
     *
     * @return
     */
    @Headers("Content-Type:application/json")
    @POST("sys/login")
    Call<User> userLogin(@Body LoginValues loginValues);

    /**
     * 获取推流状态
     * @return
     */
    @GET("srs/getPublishStatus")
    Call<StreamStatus> getPublishStatus(@Query("flyNum")String flyNum,@Query("time")String time);

    /**
     * 航线列表
     * @return
     */
    @POST("flight/selected")
    @FormUrlEncoded
    Call<MissionList> flightSelected(@Header("token") String token, @Field("flyNum") String flyNum);

    /**
     * 航线详情
     * @return
     */
    @POST("flight/info")
    @FormUrlEncoded
    Call<FlightInfo> flightInfo(@Header("token") String token, @Field("flightPathId") String flightPathId);

    /**
     * 上传航线
     * @return
     */
    @Headers("Content-Type:multipart/form-data;boundary=876589")
    @Multipart
    @POST("flight/save")
    Call<FlightSaveResult> flightSave(@Header("token") String token, @Part("uavId") RequestBody uavId, @Part MultipartBody.Part mapRes,
                                      @Part("name") RequestBody name, @Part("speed") RequestBody speed,
                                      @Part("altitude") RequestBody altitude, @Part("finishedAction") RequestBody finishedAction,
                                      @Part("flightPointJson") RequestBody flightPointJson);

    /**
     * 更新航线
     * @return
     */
    @Multipart
    @POST("flight/update")
    Call<FlightSaveResult> flightUpdate(@Header("token") String token, @Part RequestBody uavId, @Part MultipartBody.Part mapRes,
                                      @Part("name") RequestBody name, @Part("flightPathId") RequestBody flightPathId, @Part("speed") RequestBody speed,
                                      @Part("altitude") RequestBody altitude, @Part("finishedAction") RequestBody finishedAction,
                                      @Part("flightPointJson") RequestBody flightPointJson);

}