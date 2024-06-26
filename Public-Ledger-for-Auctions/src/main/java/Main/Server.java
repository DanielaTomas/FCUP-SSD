package Main;

import Kademlia.Node;
import Kademlia.ServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** Class Server: Represents a Netty server.*/
public class Server implements Runnable {
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

    /** Starts the server. */
    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        try {
            this.start(bossGroup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * Configures and starts the Netty server.
     *
     * @param bossGroup The event loop group for handling server events.
     * @throws Exception If an error occurs while starting the server.
     */
    public void start(EventLoopGroup bossGroup) throws Exception{
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(bossGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(NioDatagramChannel ch) throws Exception {
                        ch.pipeline().addLast(new ServerHandler(myNode));
                    }
                })
                .option(ChannelOption.SO_REUSEADDR, true);

        int maxAttempts = 3;
        int currentAttempt = 0;
        int retryDelaySeconds = 5;

        while (currentAttempt < maxAttempts) {
            try {
                ChannelFuture channelFuture = bootstrap.bind(port).sync();
                logger.info("Server started and listening on port " + port);
                channelFuture.channel().closeFuture().sync();
                break;
            } catch (Exception e) {
                currentAttempt++;
                logger.warning("Failed to bind to port " + port + ". Retrying in " + retryDelaySeconds + " seconds.");
                TimeUnit.SECONDS.sleep(retryDelaySeconds);
            }
        }

        if (currentAttempt >= maxAttempts) {
            logger.severe("Could not start server after " + maxAttempts + " attempts. Exiting.");
        }
    }
}
