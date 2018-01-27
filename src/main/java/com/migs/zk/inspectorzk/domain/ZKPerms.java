package com.migs.zk.inspectorzk.domain;

import org.apache.zookeeper.ZooDefs;

import java.util.Arrays;
import java.util.List;

/**
 * Created by migc on 1/9/18.
 */
public enum ZKPerms {

	admin(ZooDefs.Perms.ADMIN,		new String[]{ "ad" }),
	all(ZooDefs.Perms.ALL, 			new String[]{ "al" }),
	create(ZooDefs.Perms.CREATE, 	new String[]{ "c" }),
	delete(ZooDefs.Perms.DELETE, 	new String[]{ "d" }),
	read(ZooDefs.Perms.READ, 		new String[]{ "r" }),
	write(ZooDefs.Perms.WRITE, 		new String[]{ "w" });

	private int zKPerm;
	private String[] altIds;

	ZKPerms(int zKPerm, String[] altIds) {
		this.zKPerm = zKPerm;
		this.altIds = altIds;
	}

	public static int translatePerms(String[] roles){
		int transPerm = 0;

		for(String r : roles){
			r = r.trim();
			ZKPerms p = ZKPerms.getMatch(r);

			if(p != null)
				transPerm |= p.zKPerm;
		}

		return transPerm;
	}

	public static int translatePerms(List<ZKPerms> perms){
		int transPerm = 0;

		for(ZKPerms p : perms){
			transPerm |= p.zKPerm;
		}

		return transPerm;
	}

	public static ZKPerms getMatch(String role) {
		for (ZKPerms p : ZKPerms.values()) {

			if (p.name().equalsIgnoreCase(role))
				return p;

			if (Arrays.asList(p.altIds).contains(role))
				return p;

			if(p.name().equalsIgnoreCase(role))
				return p;
		}

		return null;
	}

}
