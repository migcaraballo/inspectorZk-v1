package com.migs.zk.inspectorzk.util;

import org.apache.zookeeper.ZooDefs;

/**
 * Created by migc on 1/11/18.
 */
public class ZKUtils {

	public static String translatePerm(int permDef){
		switch (permDef){
			case ZooDefs.Perms.ALL:
				return "all";
			case ZooDefs.Perms.ADMIN:
				return "admin";
			case 15:
				return "delete, create, read, write";
			case 14:
				return "delete, create, write";
			case 13:
				return "delete, create, read";
			case 12:
				return "delete, create";
			case 11:
				return "delete, read, write";
			case 10:
				return "delete, write";
			case 9:
				return "delete, read";
			case ZooDefs.Perms.DELETE:
				return "delete";
			case 7:
				return "create, read, write";
			case 6:
				return "create, write";
			case 5:
				return "create, read";
			case ZooDefs.Perms.CREATE:
				return "create";
			case 3:
				return "read, write";
			case ZooDefs.Perms.WRITE:
				return "write";
			case ZooDefs.Perms.READ:
				return "read";
			default:
				return permDef +": create, delete, read, write, +";
		}
	}

	public static String parseAclId(String aclId){
		String[] idParts = aclId.split(":");
		return idParts[0];
	}

	public static String getErrMsg(String msg){
		if(msg.contains("NoAuth"))
			return "Not Authorized";

		return msg;
	}

	public static boolean isNoAuth(String msg){
		return msg.contains("NoAuth");
	}

}
