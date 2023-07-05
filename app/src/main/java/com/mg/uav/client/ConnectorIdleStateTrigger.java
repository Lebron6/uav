package com.mg.uav.client;


import com.mg.uav.entity.DataInfo;
import com.orhanobut.logger.Logger;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author chuangzhang
 */
@Sharable
public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter {

  private static final DataInfo.Message HEARTBEAT =DataInfo.Message.newBuilder().build();/*= DataInfo.Message.newBuilder()
      .setMethod("Heartbeat").build();
*/
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleState state = ((IdleStateEvent) evt).state();
      if (state == IdleState.WRITER_IDLE) {
        // write heartbeat to server
        ctx.writeAndFlush(HEARTBEAT);
      } else if (state == IdleState.READER_IDLE) {
        ctx.writeAndFlush(HEARTBEAT);
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }
}
