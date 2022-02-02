package com.geekbrains.cloud.jan.client;

import com.geekbrains.cloud.jan.model.AbstractMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Net {

    private Callback callback;
    private SocketChannel channel;

    private static Net INSTANCE;

    public static Net getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Net();
        }
        return INSTANCE;
    }

    private Net() {
        this.callback = m -> {};
        Thread thread = new Thread(this::start);
        thread.setDaemon(true);
        thread.start();
    }

    public void setCallback(Callback callback) throws InterruptedException {
        Thread.sleep(500);
        channel.pipeline()
                .get(ClientHandler.class)
                .setCallback(callback);
    }

    public void write(AbstractMessage msg) {
        channel.writeAndFlush(msg);
    }

    private void start() {
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(worker)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            channel = socketChannel;
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ClientHandler(callback)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.connect("localhost", 8189)
                    .sync();
            log.info("client connected...");
            future.channel().closeFuture().sync(); // wait events
        } catch (Exception e) {
            log.error("e=", e);
        } finally {
            worker.shutdownGracefully();
        }
    }

}
