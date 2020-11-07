import annotation.ZkConfig;
import cli.NettyProxyManager;
import commonInterface.HelloService;
import org.junit.Test;

/**
 * Create by mirror on 2020/11/5
 */
@ZkConfig(zkHost = "127.0.0.1")
public class Testclient {
    @Test
    public void startCli() {
        //创建代理对象
        HelloService helloService = (HelloService) NettyProxyManager.getNettyProxyBean("testService1", "localhost", HelloService.class);
        String msg = helloService.hello("你好 dubbo~1");
        System.out.println("result:" + msg);
    }
}
