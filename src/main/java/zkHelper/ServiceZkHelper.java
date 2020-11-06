package zkHelper;

import annotation.RpcServiceStartConfig;

import annotation.ZkConfig;
import org.apache.zookeeper.*;
import org.apache.zookeeper.common.ZKConfig;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.InetAddress;


/**
 * @author mirror
 */
public class ServiceZkHelper extends ZkHelper {


    private static String serviceHost;
    private static int servicePort;
    private static String serviceName;


    public ServiceZkHelper(Class<?> serverClass) throws IOException, InterruptedException {
        serviceHost = InetAddress.getLocalHost().getHostAddress();
        init(serverClass.getAnnotation(ZkConfig.class).zkHost());
        RpcServiceStartConfig rpcServiceStartConfig = serverClass.getAnnotation(RpcServiceStartConfig.class);

        if (InetAddress.getLocalHost().getHostAddress().startsWith("192.")) {
            serviceHost = InetAddress.getLocalHost().getHostAddress();
        } else {
            serviceHost = "127.0.0.1";

        }
        servicePort = rpcServiceStartConfig.nettyPort();
        serviceName = rpcServiceStartConfig.name();
    }


    private String servicePath() {
        return ROOT + "/" + serviceName;
    }


    public boolean existsRoot() {
        Stat exists = null;
        try {
            exists = zk.exists(ROOT, false);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return exists != null;
    }


    public void createRoot() {
        try {
            zk.create(ROOT, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 某个服务代表的路径是否存在
     *
     * @return
     */
    public boolean existsServicePath() {
        Stat exists = null;
        try {
            exists = zk.exists(servicePath(), false);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return exists != null;
    }

    public void createService() throws KeeperException, InterruptedException {
        zk.create(servicePath(), "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }


    public void addInstance() {
        try {
            zk.create(servicePath() + "/" + instanceName(), "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException | InterruptedException ignored) {

        }
    }


    private String instanceName() {
        return serviceHost + ":" + servicePort;
    }

    private String instancePath() {
        return servicePath() + "/" + instanceName();
    }


    public void close() {
        try {
            zk.delete(instancePath(), -1);
            zk.delete(servicePath(), -1);
            zk.delete(ROOT, -1);
            zk.close();

        } catch (InterruptedException | KeeperException ignored) {
        }
    }
}
