import annotation.ZkConfig;
import cli.NettyProxyManager;
import common.RequestsBody;
import commonInterface.ObjectService;
import org.junit.Test;

/**
 * Create by mirror on 2020/11/5
 */
@ZkConfig(zkHost = "127.0.0.1")
public class Testclient {
    @Test
    public void startCli() {
        //创建代理对象
        ObjectService objectService = (ObjectService) NettyProxyManager.getNettyProxyBean("testService1", "localhost", ObjectService.class);
        RequestsBody requests = objectService.getRequests("你好 dubbo~1");
        System.out.println("result:" + requests.getMethodName());
    }
}
