package org.kubo.netty.zerocopy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * 用 DefaultFileRegion 触发内核 sendfile 的零拷贝文件服务器。
 * 注意：当 pipeline 中存在 SslHandler 时无法使用 sendfile，将回退到 ChunkedNioFile。
 *
 * 运行：
 *  - 准备目录 src/main/resources/static，并放置 index.html
 *  - 运行 main 后访问 http://127.0.0.1:8080
 */
public class ZeroCopyFileServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpServerCodec());
                        p.addLast(new HttpObjectAggregator(1 << 20));
                        p.addLast(new LoggingHandler(LogLevel.DEBUG));
                        p.addLast(new ChunkedWriteHandler());
                        p.addLast(new StaticFileHandler(new File("src/main/resources/static")));
                    }
                });

            Channel ch = bootstrap.bind(8080).sync().channel();
            System.out.println("[server] started at http://127.0.0.1:8080");
            ch.closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    static final class StaticFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final File rootDir;
        StaticFileHandler(File rootDir) { this.rootDir = rootDir; }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
            if (!HttpMethod.GET.equals(req.method())) {
                sendText(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, "Only GET");
                return;
            }

            String uri = req.uri();
            if (uri.equals("/")) uri = "/index.html";
            File file = new File(rootDir, uri);

            if (!file.exists() || !file.isFile()) {
                sendText(ctx, HttpResponseStatus.NOT_FOUND, "Not Found: " + uri);
                return;
            }

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long length = raf.length();

            HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpUtil.setContentLength(resp, length);
            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType(file.getName()));
            ctx.write(resp);

            if (ctx.pipeline().get(SslHandler.class) == null) {
                FileChannel fc = raf.getChannel();
                DefaultFileRegion region = new DefaultFileRegion(fc, 0, length);
                System.out.println("[file] zero-copy sendfile: " + file.getAbsolutePath() + " bytes=" + length);
                ctx.write(region).addListener(f -> closeQuietly(raf));
            } else {
                System.out.println("[file] SSL enabled → fallback to ChunkedNioFile: " + file.getAbsolutePath());
                ctx.write(new HttpChunkedInput(new ChunkedNioFile(raf.getChannel()))).addListener(f -> closeQuietly(raf));
            }

            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
        }

        private static void closeQuietly(RandomAccessFile raf) {
            try { raf.close(); } catch (Exception ignore) {}
        }

        private static void sendText(ChannelHandlerContext ctx, HttpResponseStatus status, String text) {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(bytes));
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=utf-8");
            HttpUtil.setContentLength(res, bytes.length);
            ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
        }

        private static String contentType(String name) {
            String lower = name.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=utf-8";
            if (lower.endsWith(".txt")) return "text/plain; charset=utf-8";
            if (lower.endsWith(".css")) return "text/css; charset=utf-8";
            if (lower.endsWith(".js")) return "application/javascript";
            if (lower.endsWith(".json")) return "application/json";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            return "application/octet-stream";
        }
    }
}


