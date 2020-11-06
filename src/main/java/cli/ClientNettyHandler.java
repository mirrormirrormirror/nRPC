package cli;

/**
 * Create by mirror on 2020/11/2
 */

import common.RequestsBody;
import common.ResponseBody;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import utils.KryoUtil;

import java.util.concurrent.Callable;

/**
 * @author mirror
 */
public class ClientNettyHandler extends ChannelInboundHandlerAdapter implements Callable {

    /**
     * 上下文
     */
    private ChannelHandlerContext context;


    /**
     * 返回的结果
     */
    private ResponseBody result;


    /**
     * 远程过程调用参数
     */
    private RequestsBody requestsBody;


    public RequestsBody getRequestsBody() {
        return requestsBody;
    }

    public void setRequestsBody(RequestsBody requestsBody) {
        this.requestsBody = requestsBody;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }


    public void setResult(ResponseBody result) {
        this.result = result;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(" channelActive 被调用  ");
        context = ctx;
    }


    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(" channelRead 被调用  ");
        result = KryoUtil.getSerializableObject(ResponseBody.class, msg.toString());
        notify();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public synchronized Object call() throws Exception {
        String request = KryoUtil.setSerializableObject(requestsBody);
        System.out.println("request:" + request);
        context.writeAndFlush(request);
        wait();
        System.out.println("客户端收到：" + result);
        return result.getResult();
    }


}