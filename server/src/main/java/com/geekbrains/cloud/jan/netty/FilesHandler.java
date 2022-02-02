package com.geekbrains.cloud.jan.netty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.geekbrains.cloud.jan.model.AbstractMessage;
import com.geekbrains.cloud.jan.model.FileMessage;
import com.geekbrains.cloud.jan.model.FileRequest;
import com.geekbrains.cloud.jan.model.FilesList;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FilesHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    // require userName

    private Path currentDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        currentDir = Paths.get("serverDir");
        sendList(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
        switch (message.getType()) {
            case FILE_REQUEST:
                FileRequest fileRequest = (FileRequest) message;
                ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getFileName())));
                break;
            case FILE_MESSAGE:
                FileMessage fileMessage = (FileMessage) message;
                Files.write(currentDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
                sendList(ctx);
        }
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new FilesList(currentDir));
    }
}
