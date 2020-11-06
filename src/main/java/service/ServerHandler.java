package service;

/**
 * Create by mirror on 2020/11/2
 */

import com.sun.deploy.util.StringUtils;
import common.StatusCode;
import common.RequestsBody;
import common.ResponseBody;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import utils.KryoUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServerHandler extends ChannelInboundHandlerAdapter {




    final public static String protocol = "nRPC";


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("client connect success");
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
        System.out.println("client close success");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestsBody requestsBody = KryoUtil.getSerializableObject(RequestsBody.class, msg.toString());
        // 检查协议
        if (!requestsBody.getProtocol().equals(protocol)) {
            ctx.writeAndFlush(StatusCode.PROTOCOL_COMMON);
            return;
        }
        // 服务端是否有实现相关类
        Class<?> serviceClass = ServiceContextManager.getClass(requestsBody.getClassName());
        if (serviceClass == null) {
            ctx.writeAndFlush(StatusCode.NON_IMPL);
            return;
        }

        invoke(ctx, requestsBody, serviceClass);
    }

    /**
     * 根据客户端的入参，调用服务端实现的类方法
     * @param ctx
     * @param requestsBody
     * @param serviceClass
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private void invoke(ChannelHandlerContext ctx,  RequestsBody requestsBody, Class<?> serviceClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        ResponseBody<Object> responseBody = new ResponseBody<>();
        Object serviceInstance = serviceClass.newInstance();
        // method type
        String[] methodTypeList = StringUtils.splitString(requestsBody.getMethodTypes(), "##");
        Class<?>[] parameterTypes = new Class[methodTypeList.length];
        for (int methodTypeIndex = 0; methodTypeIndex < methodTypeList.length; methodTypeIndex++) {
            Class<?> typeName = Class.forName(methodTypeList[methodTypeIndex]);
            parameterTypes[methodTypeIndex] = typeName;
        }
        Method method = serviceClass.getMethod(requestsBody.getMethodName(), parameterTypes);
        Object result = method.invoke(serviceInstance, requestsBody.getArgs());

        responseBody.setResult(result);
        responseBody.setStatusCode(StatusCode.SUCCESS);
        ctx.writeAndFlush(KryoUtil.setSerializableObject(responseBody));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}