package com.migs.zk.inspectorzk.domain;

import com.migs.zk.inspectorzk.util.ZKUtils;
import org.apache.zookeeper.data.ACL;

/**
 * Created by migc on 1/14/18.
 */
public class ZKAclData {

	private String id;
	private String scheme;
	private String perms;

	public ZKAclData(ACL acl){
		this.id = ZKUtils.parseAclId(acl.getId().getId());
		this.scheme = acl.getId().getScheme();
		this.perms = ZKUtils.translatePerm(acl.getPerms());
	}

	public String getId() {
		return id;
	}

	public String getScheme() {
		return scheme;
	}

	public String getPerms() {
		return perms;
	}
}
