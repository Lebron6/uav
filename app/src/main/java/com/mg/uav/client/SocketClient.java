package com.mg.uav.client;


import android.util.Log;

import com.mg.uav.client.handle.EventHandle;
import com.mg.uav.entity.DataInfo;
import com.orhanobut.logger.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketClient {


    String nettyServerIp;

    int nettyServerPort;

    String uavId;

    ThreadPoolExecutor executorService = ExecutorService.mCachedSerialExecutor;

    public SocketClient() {
    }

    public void setClientConfig(String nettyServerIp, int nettyServerPort, String uavId) {
        this.nettyServerIp = nettyServerIp;
        this.nettyServerPort = nettyServerPort;
        this.uavId = uavId;
    }

    protected final HashedWheelTimer timer = new HashedWheelTimer();

    private Bootstrap boot;
    EventLoopGroup group;
    private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();


    public void disconnect() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public void connect() throws Exception {
        if (group == null) {
            group = new NioEventLoopGroup();
        }
        group = new NioEventLoopGroup();
        boot = new Bootstrap();
        boot.group(group).channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO));
        final BaseConnectionWatchdog watchdog = new BaseConnectionWatchdog(boot, timer, nettyServerPort,
                nettyServerIp, true) {

            @Override
            public ChannelHandler[] handlers() {

                return new ChannelHandler[]{
                        //解码用
                        new ProtobufVarint32FrameDecoder(),
                        //构造函数传递要解码成的类型
                        new ProtobufDecoder(DataInfo.Message.getDefaultInstance()),
                        //编码用
                        new ProtobufVarint32LengthFieldPrepender(),
                        new ProtobufEncoder(),
                        this,
                        new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS),
                        idleStateTrigger,
                        new HeartBeatClientHandler(uavId, executorService),
                        new EventHandle(executorService)
                };
            }
        };

        ChannelFuture future;
        //进行连接
        try {
            synchronized (boot) {
                boot.handler(new ChannelInitializer<Channel>() {
                    //初始化channel
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(watchdog.handlers());
                    }
                });

                future = boot.connect(nettyServerIp, nettyServerPort);
            }
            // 以下代码在synchronized同步块外面是安全的
            future.sync();
        } catch (Throwable t) {
//      System.exit(1);
            Logger.e("connects to  fails", t);
            throw new Exception("connects to  fails", t);
        }
    }

}