package com.migs.zk.inspectorzk.services;

import com.migs.zk.inspectorzk.domain.ZKConnInfo;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by migc on 1/14/18.
 */
public class TestZKConnection {

	@Test
	public void testBadGoodConnection() throws Exception{
		ZKConnInfo ci = new ZKConnInfo("aasdfasdfasdf", 34324);

		ZKConnection zcon = new ZKConnection();
		zcon.getZooKeeper(ci);
		Assert.assertFalse(zcon.isConnected());
		System.out.println("Done with BAD info...");
		System.out.println();

		System.out.println("Connecting with right info");
		ci = new ZKConnInfo("localhost", 2181);
		zcon.getZooKeeper(ci);
		Assert.assertTrue(zcon.isConnected());
		System.out.println("Done with GOOD info...");
	}
}
