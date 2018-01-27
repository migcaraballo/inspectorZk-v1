package com.migs.zk.inspectorzk.services;

import com.migs.zk.inspectorzk.domain.ZKConnInfo;
import com.migs.zk.inspectorzk.domain.ZKDataResult;
import com.migs.zk.inspectorzk.domain.ZKSchemeDefs;
import com.migs.zk.inspectorzk.util.ZKUtils;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.a7.A7_Grids;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by migc on 1/9/18.
 */
public class ZKServiceTest {
	private static ZKService zkService;
	private static ZKConnInfo ci;

	private static final String HOST = "localhost";
	private static final int PORT = 2181;

	private static String testCaseNode = "/testCase";
	private static String tmpSubZNode;

	private static String tstUsr = "tstusr";
	private static String tstpw = "tstpw";

	private static String timeStamp;

	@BeforeClass
	public static void initOnce() {
		ci = new ZKConnInfo(HOST, PORT);
		zkService = ZKService.getInstance();
		zkService.connectToZK(ci);
		tmpSubZNode = String.format("testsubnode-%d", System.currentTimeMillis());
		timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
	}

	@Test
	public void testGetZnodeData() throws Exception {
		System.out.println("Testing: **************************************************| testGetZnodeData\n");
		ZKDataResult dataResult = zkService.getZNodeData(testCaseNode);
//		String data = zkService.getZNodeData(testCaseNode);
		assertNotNull(dataResult);

		System.out.printf("data: %s\n", dataResult);
		System.out.println();
	}

	@Test
	public void testSetZNodeData() throws Exception {
		System.out.println("Testing: **************************************************| testSetZNodeData\n");
		boolean updated = zkService.setZNodeData(testCaseNode, timeStamp);
		System.out.println("ZNode data updated: "+ updated);
		assertTrue(updated);

		System.out.println();
	}

	@Test
	public void testGetZnodeChildren() throws Exception{
		System.out.println("Testing: **************************************************| testGetZnodeChildren\n");
		List<String> paths = zkService.getZnodeChildren(testCaseNode);

		assertTrue(paths != null);
		assertTrue(paths.size() > 0);

		if(paths != null)
			paths.forEach( p -> System.out.println("\t- "+ p));

		System.out.println();
	}

	@Test
	public void testCreateZnode() throws Exception {
		System.out.println("Testing: **************************************************| testCreateZnode\n");
		boolean created = zkService.createZnode(
										testCaseNode +"/"+ tmpSubZNode,
										"sub-node-value-"+ System.currentTimeMillis(),
										CreateMode.EPHEMERAL
									);

		assertTrue(created);
		System.out.println();
	}

	@Test
	public void testDeleteZNode() throws Exception {
		System.out.println("Testing: **************************************************| testDeleteZNode\n");

		// 1 - create temp node
		String tempNode = testCaseNode +"/test-delete-node";
		boolean created = zkService.createZnode(tempNode, "temp-value", CreateMode.EPHEMERAL);
		assertTrue(created);

		boolean deleted = zkService.deleteZnode(tempNode);
		assertTrue(deleted);
		System.out.println();
	}

	@Test
	public void testCreateDuplicateZnode() throws Exception {
		System.out.println("Testing: **************************************************| testCreateDuplicateZnode\n");

		String znode = testCaseNode +"/"+ tmpSubZNode +"-dup";
		String znodeValue = znode +"-value";

		boolean created = zkService.createZnode(znode, znodeValue, CreateMode.EPHEMERAL);
		assertTrue(created);

		created = zkService.createZnode(znode, znodeValue, CreateMode.EPHEMERAL);
		assertFalse(created);
		System.out.println();
	}

	@Test
	public void testSetAclWithoutAuth() throws Exception {
		System.out.println("Testing: **************************************************| testSetAclWithoutAuth");
		boolean actSet = false;

		try {
			actSet = zkService.setAclForZnode(testCaseNode +"/"+ tmpSubZNode, ZKSchemeDefs.DIGEST, tstUsr, "c,d,r,w");
		} catch (ZKDataException e) {
			System.out.println(e.getMessage());
		}

		assertFalse(actSet);
		System.out.println();
	}

	@Test
	public void testSetAclWithAuth() throws Exception {
		System.out.println("Testing: **************************************************| testSetAclWithAuth");

		boolean authed = zkService.authDigestUser(tstUsr, tstpw, ZKSchemeDefs.DIGEST);
		assertTrue(authed);

		boolean aclSet = zkService.setAclForZnode(testCaseNode +"/"+ tmpSubZNode, ZKSchemeDefs.AUTH, tstUsr, "c,d,r,w");
		assertTrue(aclSet);

		System.out.println();
	}

	@Test
	public void testGetAclMap() throws Exception {
		System.out.println("Testing: **************************************************| testGetAclMap\n");

		Map<String, ACL> aclList = zkService.getAclMap(testCaseNode);
		assertNotNull(aclList);

		System.out.println("ACL List for: "+ testCaseNode);

		AsciiTable at = new AsciiTable();
		at.getContext().setGrid(A7_Grids.minusBarPlusEquals());
		at.setPadding(0);
		at.addRule();
		at.addRow("USER", "SCHEME", "PERMS");

		aclList.forEach( (k, v) -> {
			at.addRule();
			at.addRow(k, v.getId().getScheme(), ZKUtils.translatePerm(v.getPerms()));

		});

		at.addRule();

		System.out.println(at.render());
		System.out.println();
	}

	@Test
	public void testAuthenticationToZK() throws Exception{
		System.out.println("Testing: **************************************************| testAuthenticationToZK\n");

		boolean authed = zkService.authDigestUser("mig", "mig", ZKSchemeDefs.DIGEST);
		assertTrue(authed);
		System.out.printf("Authenticated: %s\n\n", authed);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if(zkService != null)
			zkService.disconnectFromZK();

		System.out.println();
	}
}