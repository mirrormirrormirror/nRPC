
import annotation.RpcServiceStartConfig;
import annotation.ZkConfig;
import exception.NonRpcScanException;
import org.junit.Test;
import service.Server;

import java.io.IOException;

/**
 * Create by mirror on 2020/11/3
 */
@ZkConfig(zkHost = "127.0.0.1")
@RpcServiceStartConfig(packagePath = "com", name = "testService1", nettyPort = 7002)
public class TestService {
    @Test
    public void startService() throws NonRpcScanException, IOException, ClassNotFoundException, InterruptedException {
        Server.start(TestService.class);
    }
}
