package cli;

/**
 * Create by mirror on 2020/11/2
 */

import com.sun.deploy.util.StringUtils;
import common.RequestsBody;
import exception.NonServiceInstanceException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.zookeeper.KeeperException;
import service.ServerHandler;
import zkHelper.ClientZkHelper;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mirror
 */
public class NettyProxyManager {

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    private static Map<String, List<ClientNettyHandler>> serviceName2InstanceClients = new ConcurrentHashMap<>();


    public static Object getNettyProxyBean(String serviceName, String zkHost, final Class<?> serivceClass) {

        return Proxy.newProxyInstance(serivceClass.getClassLoader(), new Class[]{serivceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                List<ClientNettyHandler> clientNettyHandlers = getClientNettyHandlers(serviceName, zkHost);
                if (clientNettyHandlers == null || clientNettyHandlers.isEmpty()) {
                    throw new NonServiceInstanceException(serviceName + " non service instance.");
                }
                ClientNettyHandler clientNettyHandler = clientNettyHandlers.get((int) (0 + Math.random() * clientNettyHandlers.size() - 1));
                RequestsBody requestsBody = getRequestsBody(method, args, serivceClass);
                clientNettyHandler.setRequestsBody(requestsBody);
                return executor.submit(clientNettyHandler).get();
            }
        });
    }

    /**
     * 组装参数发送给服务端
     * @param method
     * @param args
     * @param serivceClass
     * @return
     */
    private static RequestsBody getRequestsBody(Method method, Object[] args, Class<?> serivceClass) {
        RequestsBody requestsBody = new RequestsBody();
        requestsBody.setProtocol(ServerHandler.protocol);
        requestsBody.setClassName(serivceClass.getName());
        // param type
        List<String> typeNameList = new LinkedList<>();
        for (Parameter parameter : method.getParameters()) {
            String typeName = parameter.getType().getTypeName();
            typeNameList.add(typeName);
        }
        requestsBody.setMethodTypes(StringUtils.join(typeNameList, "##"));
        requestsBody.setArgs(args);
        requestsBody.setMethodName(method.getName());
        return requestsBody;
    }

    /**
     * 获取服务对应的代理
     * @param serviceName
     * @param zkHost
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws KeeperException
     */
    private static List<ClientNettyHandler> getClientNettyHandlers(String serviceName, String zkHost) throws IOException, InterruptedException, KeeperException {
        List<ClientNettyHandler> clientNettyHandlers = serviceName2InstanceClients.get(serviceName);
        if (clientNettyHandlers == null || clientNettyHandlers.isEmpty()) {
            initInstanceClients(serviceName, zkHost);
        }
        clientNettyHandlers = serviceName2InstanceClients.get(serviceName);
        return clientNettyHandlers;
    }

    /**
     * 初始化netty服务代理
     * @param serviceName
     * @param zkHost
     * @throws IOException
     * @throws InterruptedException
     * @throws KeeperException
     */
    private static void initInstanceClients(String serviceName, String zkHost) throws IOException, InterruptedException, KeeperException {
        ClientZkHelper clientZkHelper = new ClientZkHelper(zkHost);
        List<String> instanceList = clientZkHelper.getInstanceList(serviceName);
        List<ClientNettyHandler> instanceClients = new LinkedList<>();
        for (String instance : instanceList) {
            ClientNettyHandler client = initClient(instance);
            instanceClients.add(client);
        }
        serviceName2InstanceClients.put(serviceName, instanceClients);
    }

    /**
     * 初始化客户端
     */
    private static ClientNettyHandler initClient(String instance) {
        String[] instanceSplit = instance.split(":");

        ClientNettyHandler client = new ClientNettyHandler();
        //创建EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new StringDecoder());
                                pipeline.addLast(new StringEncoder());
                                pipeline.addLast(client);
                            }
                        }
                );
        try {
            bootstrap.connect(instanceSplit[0], Integer.valueOf(instanceSplit[1])).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

}