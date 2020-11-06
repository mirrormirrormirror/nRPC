package zkHelper;


import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;

/**
 * Create by mirror on 2020/11/5
 */
public class ClientZkHelper extends ZkHelper {
    public ClientZkHelper(String zkHost) throws IOException, InterruptedException {
        init(zkHost);
    }

    private String servicePath(String serviceName) {
        return ROOT + "/" + serviceName;
    }


    public List<String> getInstanceList(String serviceName) throws KeeperException, InterruptedException {
        return zk.getChildren(servicePath(serviceName), false);
    }
}
