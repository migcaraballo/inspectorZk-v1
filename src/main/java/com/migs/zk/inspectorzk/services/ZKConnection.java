package com.migs.zk.inspectorzk.services;

import com.migs.zk.inspectorzk.domain.ZKConnInfo;
import com.migs.zk.inspectorzk.domain.ZKSchemeDefs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.*;

import java.util.concurrent.*;

/**
 * Created by migc on 1/9/18.
 */
public class ZKConnection {

	private static Logger log = LogManager.getLogger(ZKConnection.class);

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_TRIES_LIMIT = 3;

	private ZooKeeper zooKeeper;
	private final CountDownLatch connectedSignalLatch = new CountDownLatch(1);

	public ZKConnection() {}

	/**
	 * This method spins up a separate thread to connect to ZK. Unlike regular zk connect method, this will eventually timeout
	 * and not leave caller hanging. Default is 3 attempts * DEFAULT_TIMEOUT
	 *
	 * @param host
	 * @param port
	 * @param timeout timeout most likely to be honored by connection thread vs zk api thread
	 * @throws Exception
	 */
	private void connect(String host, int port, int timeout) throws Exception {
		Callable<ZooKeeper> connectThread = () -> {
			zooKeeper = new ZooKeeper( (host+":"+port), timeout, (WatchedEvent we) -> {
				if(we.getState() == Watcher.Event.KeeperState.SyncConnected) {
					connectedSignalLatch.countDown();
					log.info(String.format("Connected to ZK: %s:%d\n", host, port));
				}
			});

			connectedSignalLatch.await();
			return zooKeeper;
		};

		FutureTask<ZooKeeper> connectTask = new FutureTask<>(connectThread);
		ExecutorService ex = Executors.newFixedThreadPool(1);
		ex.execute(connectTask);

		int tries = 0;
		while(tries++ < DEFAULT_TRIES_LIMIT){
			log.debug(String.format("Connection Attempt: %d", tries));

			try {
				zooKeeper = connectTask.get(timeout, TimeUnit.MILLISECONDS);
			} catch (Exception e) { }

			if(zooKeeper != null && zooKeeper.getState().isConnected()) {
				log.debug("Connection Established");
				break;
			}
			else{
				log.debug("Connection failed, trying again");
				Thread.sleep(DEFAULT_TIMEOUT);
			}
		}

		ex.shutdown();
	}

	public ZooKeeper getZooKeeper(ZKConnInfo ci) throws ZKConnectionException {
        if(!isConnected()){
            try {
                connect(ci.getHost(), ci.getPort(), DEFAULT_TIMEOUT);
            } catch (Exception e) {
                throw new ZKConnectionException(e);
            }
        }
        return zooKeeper;
    }

	public void authenticate(String user, String cred, ZKSchemeDefs schemeDef) throws ZKAuthException {
        try {
            if(zooKeeper != null)
                zooKeeper.addAuthInfo(schemeDef.getSchemeValue(), String.format("%s:%s", user, cred).getBytes());
        } catch (Exception e) {
            throw new ZKAuthException("Error trying to authenticate", e);
        }
    }

	public void close() throws ZKConnectionException {
        try {
            if(zooKeeper != null) {
                zooKeeper.close();
                zooKeeper = null;
                log.debug("Disconnected from ZK");
            }
        } catch (InterruptedException e) {
            throw new ZKConnectionException("Error closing ZK Connection", e);
        }
    }

	public boolean isConnected() {
        if(zooKeeper == null)
            return false;

        return zooKeeper.getState().isConnected();
	}
}
