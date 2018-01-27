package com.migs.zk.inspectorzk.services;

import com.migs.zk.inspectorzk.domain.*;
import com.migs.zk.inspectorzk.util.ZKUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by migc on 1/9/18.
 */
public class ZKService {

    private static Logger log = LogManager.getLogger(ZKService.class);

	private static ZKService instance;
    private static final int ANY_NODE_VERSION = -1;

	private static ZKConnection zcon;
    private static ZKConnInfo ci;

    private ZKService(){
        zcon = new ZKConnection();
    }

    private static synchronized void instantiate(){
        if(instance == null)
            instance = new ZKService();
    }

    public static ZKService getInstance(){
        if(instance == null)
            instantiate();

        return instance;
    }

    public boolean connectToZK(ZKConnInfo connInfo) {
        ci = connInfo;

        try {
            zcon.getZooKeeper(ci);
        } catch (ZKConnectionException e) {
            e.printStackTrace();
        }

        return zcon.isConnected();
    }

    public ZKDataResult getZNodeData(String path) throws ZKDataException{
        log.debug(String.format("Getting znode data for: %s\n", path));

        ZKDataResult dataResult = null;
        try {
            ZooKeeper zk = zcon.getZooKeeper(ci);

            if(zk != null){
                Stat nodeStat = new Stat();
                byte[] data = zk.getData(path, false, nodeStat);

                log.debug("nodeStat = "+ nodeStat);

                if(data != null){
                    dataResult = new ZKDataResult(new String(data, StandardCharsets.UTF_8).trim(), nodeStat);
                }
            }
        } catch (Exception e) {
            String msg = "";
            if(ZKUtils.isNoAuth(e.getMessage()))
                msg = String.format("Error: trying to get data for: %s due to: %s\nMight need to authenticate first (Menu -> Connection -> Auth)", path, e.getMessage());
            else
                msg = String.format("Error: trying to get data for: %s due to: %s", path, e.getMessage());

            throw new ZKDataException(msg);
        }

        return dataResult;
    }

    public boolean setZNodeData(String path, String data) throws ZKDataException {
        try {
            ZooKeeper zk = zcon.getZooKeeper(ci);
            Stat currNodeStat = zk.exists(path, null);

            if(currNodeStat != null){
                int currVer = currNodeStat.getVersion();
                Stat newStat = zk.setData(path, data.getBytes(), currVer);
                log.debug("updated znode: "+ path);
                return newStat.getVersion() >= currVer;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ZKDataException(
                String.format("Error setting data for: %s", path),
                e
            );
        }
        return false;
    }

    public List<String> getZnodeChildren(String path) throws ZKDataException {
        try {
            ZooKeeper zk = zcon.getZooKeeper(ci);

            if(zk != null){
                List<String> sortedChildren = zk.getChildren(path, false);
                Collections.sort(sortedChildren);
                return sortedChildren;
            }
        } catch (Exception e) {
            String msg = "";
            if(ZKUtils.isNoAuth(e.getMessage()))
                msg = String.format("Error: trying to get children for: %s due to: %s\nMight need to authenticate first (Menu -> Connection -> Auth)", path, e.getMessage());
            else
                msg = String.format("Error: trying to get children for: %s due to: %s", path, e.getMessage());

            throw new ZKDataException(msg);
        }

        return null;
    }

    public boolean authDigestUser(String user, String pw, ZKSchemeDefs schemeDef) throws ZKAuthException {
        zcon.authenticate(user, pw, schemeDef);
        return true;
    }

    public void disconnectFromZK(){
        try {
            if(zcon != null)
                zcon.close();
        } catch (ZKConnectionException e) {
            e.printStackTrace();
        }
    }

    public Map<String, ACL> getAclMap(String path) throws ZKDataException {
        Map<String, ACL> aclMap = new TreeMap<>();

        try {
            ZooKeeper zk = zcon.getZooKeeper(ci);

            List<ACL> aclList = zk.getACL(path, null);

            if(aclList != null)
                aclList.forEach( acl -> aclMap.put(ZKUtils.parseAclId(acl.getId().getId()), acl) );
        } catch (Exception e) {
            e.printStackTrace();
            throw new ZKDataException(String.format("Error getting ACL for: %s", path));
        }

        return aclMap;
    }

    public boolean setAclForZnode(String path, ZKSchemeDefs zkSchemeDef, String usrId, String rolesStr) throws ZKDataException {
        try {
            int translatedPerms = ZKPerms.translatePerms(rolesStr.split(","));
            log.debug(String.format("\nrolesStr: %s | translatedPerms = %d\n", rolesStr, translatedPerms));

            Id id = new Id(zkSchemeDef.getSchemeValue(), usrId);
            ACL acl = new ACL(translatedPerms, id);

            Map<String, ACL> currentAcls = getAclMap(path);
            currentAcls.put(id.getId(), acl);

            ZooKeeper zk = zcon.getZooKeeper(ci);

            if(zk != null){
                zk.setACL(path, new ArrayList<>(currentAcls.values()), -1);
                log.debug("Perms Set");
                return true;
            }
        } catch (Exception e) {
            throw new ZKDataException(
                String.format("Error trying to set ACL for: %s | caused by: %s", path, e.getMessage()),
                e
            );
        }

        return false;
    }

    public boolean setAclForZnode(String path, ZKSchemeDefs zkSchemeDef, String usrId, String pw, List<ZKPerms> perms) throws ZKDataException {
        try {
            int translatedPerms = ZKPerms.translatePerms(perms);
            log.debug(String.format("\nrolesStr: %s | translatedPerms = %d\n", perms.size(), translatedPerms));

            if(zkSchemeDef == ZKSchemeDefs.DIGEST)
                usrId +=":"+pw;

            Id id = new Id(zkSchemeDef.getSchemeValue(), usrId);
            ACL acl = new ACL(translatedPerms, id);

            Map<String, ACL> currentAcls = getAclMap(path);
            currentAcls.put(id.getId(), acl);

            ZooKeeper zk = zcon.getZooKeeper(ci);

            if(zk != null){
                zk.setACL(path, new ArrayList<>(currentAcls.values()), -1);
                log.debug("Perms Set");
                return true;
            }
        } catch (Exception e) {
            throw new ZKDataException(e.getMessage(),e);
        }

        return false;
    }

    public boolean removeAclForZnode(String path, String usrId) throws ZKDataException{
        boolean removed = false;

        Map<String, ACL> currentAcls = getAclMap(path);

        if(currentAcls != null && !currentAcls.isEmpty()){

            Collection<ACL> aclList = currentAcls.values();
            Iterator<ACL> aclIterator = aclList.iterator();

            while(aclIterator.hasNext()){
                ZKAclData zkAclData = new ZKAclData(aclIterator.next());
                if(zkAclData.getId().equals(usrId)){
                    aclIterator.remove();
                }
            }

            try {
                ZooKeeper zk = zcon.getZooKeeper(ci);

                if(zk != null){
                    zk.setACL(path, new ArrayList<>(currentAcls.values()), -1);
                    removed = true;
                }
            } catch (Exception e) {
                log.error("Error removing ACL: "+ e.getMessage());
                throw new ZKDataException("Error removing ACL", e);
            }
        }

        return removed;
    }

    public boolean createZnode(String path, String data, CreateMode createMode) throws ZKDataException {
        try {
            ZooKeeper zk = zcon.getZooKeeper(ci);

            if(zk != null){
                String res = zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.debug("create res: "+ res);
                return true;
            }
        } catch (Exception e) {
            if(e.getMessage().contains("NodeExists"))
                log.error("ERROR: Node already exists\n");
            else
                log.error(String.format("ERROR: %s", e.getMessage()));
        }

        return false;
    }

    public boolean deleteZnode(String path) throws ZKDataException {
        try {
            ZooKeeper zk = zcon.getZooKeeper(ci);
            zk.delete(path, ANY_NODE_VERSION);
            log.debug(String.format("Znode deleted: %s\n", path));
            return true;
        } catch (Exception e) {
            throw new ZKDataException(String.format("Error deleting node: %s | cause: %s", path, e.getMessage()));
        }
    }
}
