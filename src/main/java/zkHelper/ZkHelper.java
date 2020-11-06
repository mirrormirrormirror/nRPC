package zkHelper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author mirror
 */
public class ZkHelper {
    protected ZooKeeper zk;

    public static final String ROOT = "/nRpcRoot";

    synchronized public void init(String zkHost) throws IOException, InterruptedException {
        if (zk == null) {
            final CountDownLatch connectedSignal = new CountDownLatch(1);
            zk = new ZooKeeper(zkHost, 60000, new Watcher() {
                @Override
                public void process(WatchedEvent we) {
                    if (we.getState() == Event.KeeperState.SyncConnected) {
                        connectedSignal.countDown();
                    }
                }
            });
            connectedSignal.await(20, TimeUnit.SECONDS);
        }
    }
}
