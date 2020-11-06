package service;

import annotation.RpcServiceStartConfig;
import exception.NonRpcScanException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.zookeeper.KeeperException;
import zkHelper.ServiceZkHelper;


import java.io.IOException;

/**
 * @author mirror
 */
public class Server {
    public static void start(Class<?> startClass) throws IOException, NonRpcScanException, ClassNotFoundException, InterruptedException {
        ServiceContextManager.init(startClass);
        System.out.println("context init success.");
        startServer(startClass);
        System.out.println("start service success");
    }

    private static void startServer(Class<?> startClass) throws IOException, InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();


        ServiceZkHelper serviceZkHelper = new ServiceZkHelper(startClass);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                                      @Override
                                      protected void initChannel(SocketChannel ch) throws Exception {
                                          ChannelPipeline pipeline = ch.pipeline();
                                          pipeline.addLast(new StringDecoder());
                                          pipeline.addLast(new StringEncoder());
                                          pipeline.addLast(new ServerHandler());
                                      }
                                  }

                    );
            RpcServiceStartConfig rpcStartConfig = startClass.getAnnotation(RpcServiceStartConfig.class);
            ChannelFuture channelFuture = serverBootstrap.bind(rpcStartConfig.nettyHost(), rpcStartConfig.nettyPort()).sync();

            registerService(serviceZkHelper);
            System.out.println("服务端启动成功");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            // 关闭注册到zookeeper的服务
            serviceZkHelper.close();
        }
    }

    /**
     * 注册服务到zookeeper
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static void registerService(ServiceZkHelper serviceZkHelper) throws KeeperException, InterruptedException {
        if (!serviceZkHelper.existsRoot()) {
            serviceZkHelper.createRoot();
        }
        if (!serviceZkHelper.existsServicePath()) {
            serviceZkHelper.createService();
        }
        serviceZkHelper.addInstance();
    }
}