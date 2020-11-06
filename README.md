# nRPC
基于netty、zookeeper实现一个简单的rpc框架
### 运行方式
#### 前提
需要预先安装好zookeeper
#### 服务端
``` java
@ZkConfig(zkHost = "127.0.0.1")
@RpcServiceStartConfig(packagePath = "com", name = "testService1", nettyPort = 7001)
public class TestService {
    @Test
    public void startService() throws NonRpcScanException, IOException, ClassNotFoundException, InterruptedException {
        Server.start(TestService.class);
    }
}

```
@ZkConfig是配置服务端需要注册服务到zookeeper。
@RpcServiceStartConfig配置服务端的基本配置。packagePath配置扫描服务端实现类的路径，name是配置服务名称，供客户端做服务发现，nettyPort配置服务的启动端口
#### 客户端
``` java
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
```
客户端@ZkConfig是配置客户端zookeeper做服务发现
