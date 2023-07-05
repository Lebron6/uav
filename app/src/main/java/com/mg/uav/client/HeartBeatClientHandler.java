package com.mg.uav.client;

import com.mg.uav.entity.DataInfo;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.concurrent.ExecutorService;


/**
 * @author chuangzhang
 */
@Sharable
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {

  String uavId;

  ExecutorService executorService;

  public HeartBeatClientHandler(String uavId, ExecutorService executorService) {
    this.uavId = uavId;
    this.executorService = executorService;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {

    //log.info("激活时间是：{}", DateUtil.formatDateTime(new Date()));
    DataInfo.Message message= DataInfo.Message.newBuilder()
            .setDataType(DataInfo.Message.DataType.RegisterRequestType)
            .setFlyNum(uavId)
            .setRegisterRequest(
                    DataInfo.RegisterRequest.newBuilder().build()
            ).build();

    ctx.writeAndFlush(message).addListener(future -> {
      if (future.isSuccess()) {
        ChannelCache.setCtx(ctx);
        //log.info("发送指令 : {} 消息发送成功", communication);
      } else {
       // log.info("消息发送失败 {}", future.toString(), future.cause());
      }
    });

  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    //log.info("停止时间是：" + DateUtil.formatDateTime(new Date()));
  }
}
