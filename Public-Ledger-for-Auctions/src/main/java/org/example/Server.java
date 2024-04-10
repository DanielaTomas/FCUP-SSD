package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.logging.Logger;

/** Class Server: Represents a Netty server.*/
public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final int port;
    private Node myNode;

    /**
     * Constructs a new Server instance.
     *
     * @param port The port on which the server listens.
     * @param node The local node.
     */
    public Server(int port, Node node) {
        this.port = port;
        this.myNode = node;
    }

    /**
     * Starts the server.
     *
     * @throws Exception If an error occurs while starting the server.
     */
    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(bossGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(new ServerHandler(myNode));
                        }
                    })
                    .option(ChannelOption.SO_REUSEADDR, true);

            ChannelFuture cf = b.bind(port).sync();
            logger.info("Server started and listening on port " + port);
            cf.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }
}
