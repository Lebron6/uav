package com.mg.uav.client;

import android.util.Log;

import com.mg.uav.entity.DataInfo;
import com.orhanobut.logger.Logger;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author zhangchuang
 * @description
 * @date 2020/9/8 11:13 上午
 **/
public class ChannelCache {

    private static ChannelHandlerContext channelHandlerContext;

    public synchronized static void setCtx(ChannelHandlerContext ctx) {
        if (ctx == null || !ctx.channel().isActive()) {
            channelHandlerContext = null;
            return;
        }
        ChannelCache.channelHandlerContext = ctx;
    }



    public static void send(DataInfo.Message message) {
        if (ChannelCache.channelHandlerContext == null) {
            //log.error("channelHandlerContext 为null，请检查连接！");
            return;
        }
        channelHandlerContext.writeAndFlush(message)
                .addListener(future -> {
                    if (future.isSuccess()) {
//                        Logger.e("Netty 发送成功：",message.toString());
                    } else {
                        Logger.e("Netty 发送失败："+future.toString());
                    }
                });
    }
    public static void e(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int max_str_length = 2001 - tag.length();
        //大于4000时
        while (msg.length() > max_str_length) {
            Log.e(tag,msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);
        }
        //剩余部分
        Log.e(tag, msg);
    }

}
