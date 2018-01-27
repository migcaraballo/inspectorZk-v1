package com.migs.zk.inspectorzk.domain;

import org.apache.zookeeper.data.Stat;

import java.util.*;

/**
 * Created by migc on 1/15/18.
 */
public class ZKDataResult {

	private String znodeData;
	private Stat zkStat;

	public ZKDataResult(String znodeData, Stat zkStat) {
		if(znodeData != null && !znodeData.isEmpty())
			this.znodeData = znodeData;
		else
			this.znodeData = "Znode has no data set";

		this.zkStat = zkStat;
	}

	/******************************************************************************************************************/

	public List<Map<String, Object>> getStatMapList(){
		List<Map<String, Object>> mapList = new ArrayList<>();

		if(zkStat != null){
			mapList.add(getPropMap("Version", zkStat.getVersion()));
			mapList.add(getPropMap("Number Children", zkStat.getNumChildren()));
			mapList.add(getPropMap("Data Length", zkStat.getDataLength()));
			mapList.add(getPropMap("A Version", zkStat.getAversion()));
			mapList.add(getPropMap("C Version", zkStat.getCversion()));
			mapList.add(getPropMap("C Time", zkStat.getCtime()));
			mapList.add(getPropMap("M Time", zkStat.getMtime()));
			mapList.add(getPropMap("Ephemeral Owner", zkStat.getEphemeralOwner()));
			mapList.add(getPropMap("CZXID", zkStat.getCzxid()));
			mapList.add(getPropMap("MZXID", zkStat.getMzxid()));
			mapList.add(getPropMap("PZXID", zkStat.getPzxid()));
		}

		return mapList;
	}

	private Map<String, Object> getPropMap(String key, Object val){
		HashMap<String, Object> map = new HashMap<>();
		map.put("key", key);
		map.put("val", val);
		return map;
	}

	/******************************************************************************************************************/

	public String getZnodeData() {
		return znodeData;
	}

	public Stat getZkStat() {
		return zkStat;
	}
}
