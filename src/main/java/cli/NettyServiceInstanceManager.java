//package cli;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.string.StringDecoder;
//import io.netty.handler.codec.string.StringEncoder;
//import org.apache.zookeeper.KeeperException;
//import org.apache.zookeeper.WatchedEvent;
//import org.apache.zookeeper.Watcher;
//import org.apache.zookeeper.ZooKeeper;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Create by mirror on 2020/11/5
// */
//public class NettyServiceInstanceManager {
//    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//    private static Map<String, Map<String, InstanceContext>> serviceInstanceContexts = new ConcurrentHashMap<>();
//    private final ZooKeeper zk;
//
//    public NettyServiceInstanceManager(String zkHost) throws IOException, InterruptedException {
//        final CountDownLatch connectedSignal = new CountDownLatch(1);
//        zk = new ZooKeeper(zkHost, 6000, new Watcher() {
//            @Override
//            public void process(WatchedEvent we) {
//                if (we.getState() == Event.KeeperState.SyncConnected) {
//                    connectedSignal.countDown();
//                }
//            }
//        });
//        connectedSignal.await();
//    }
//
//
//    public void subscribe(String serviceName) throws KeeperException, InterruptedException {
//        while (true) {
//            final CountDownLatch instanceChangeSignal = new CountDownLatch(1);
//            List<String> zkInstances = zk.getChildren("", new Watcher() {
//                @Override
//                public void process(WatchedEvent event) {
//                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
//                        instanceChangeSignal.countDown();
//                    }
//                }
//            });
//            Map<String, InstanceContext> instanceContexts = serviceInstanceContexts.get(serviceName);
//            Set<String> instances = instanceContexts.keySet();
//
//            Map<String, InstanceContext> newInstanceContexts = new HashMap<>();
//
//            for (String zkInstance : zkInstances) {
//                // 已经被初始化
//                if (instances.contains(zkInstance)) {
//                    newInstanceContexts.put(zkInstance, instanceContexts.get(zkInstance));
//                } else if (!instances.contains(zkInstance)) {
//                    //新的服务
//                    InstanceContext instanceContext = instanceContext(zkInstance);
//                    newInstanceContexts.put(zkInstance, instanceContext);
//                }
//            }
//            instances.removeAll(zkInstances);
//            serviceInstanceContexts.remove(serviceName);
//            serviceInstanceContexts.put(serviceName, newInstanceContexts);
//            instanceChangeSignal.await();
//        }
//    }
//
//
//    public Map<String, InstanceContext> getInstanceContext(String serviceName) {
//        return serviceInstanceContexts.get(serviceName);
//    }
//
//
//    private static InstanceContext instanceContext(String instance) {
//        String[] instanceSplit = instance.split(":");
//
//        ClientNettyHandler client = new ClientNettyHandler();
//        //创建EventLoopGroup
//        NioEventLoopGroup group = new NioEventLoopGroup();
//        Bootstrap bootstrap = new Bootstrap();
//        bootstrap.group(group)
//                .channel(NioSocketChannel.class)
//                .option(ChannelOption.TCP_NODELAY, true)
//                .handler(
//                        new ChannelInitializer<SocketChannel>() {
//                            @Override
//                            protected void initChannel(SocketChannel ch) {
//                                ChannelPipeline pipeline = ch.pipeline();
//                                pipeline.addLast(new StringDecoder());
//                                pipeline.addLast(new StringEncoder());
//                                pipeline.addLast(client);
//                            }
//                        }
//                );
//        try {
//            bootstrap.connect(instanceSplit[0], Integer.valueOf(instanceSplit[1])).sync();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        InstanceContext instanceContext = new InstanceContext();
//        instanceContext.setClineBootsrap(bootstrap);
//        instanceContext.setClientNettyHandler(client);
//        return instanceContext;
//    }
//
//
//}
