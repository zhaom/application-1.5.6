package com.babeeta.butterfly.application.gateway.ios.impl;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.SSLSocket;

import org.apache.commons.codec.digest.DigestUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.babeeta.butterfly.application.gateway.ios.AccountService;
import com.babeeta.butterfly.application.gateway.ios.ApnsNotification;

@Ignore
public class PooledApnConnectionFactoryTest {

	private KeyStore keystore = null;
	private static final String APID = "48857174287d4dbcaf87bfc17c8bd198";
	private PooledApnConnectionFactory pooledApnConnectionFactory;
	private AccountService accountService = null;

	@Test
	public void push() throws Exception {
		ApnsConnection conn = (ApnsConnection) pooledApnConnectionFactory
				.makeObject(APID);

		ApnsNotification n = new ApnsNotification(
				"UUID",
				"713760F5501A6BF3270B9D8AB35C9608BF482B360CDCE85C73A393FF5988F327",
				"{\"aps\":{\"alert\":\"Hello from liang's unit test..\",\"badge\":20, \"sound\":\"default\"}, \"whisper\":\"A sound mind in a sound body.\"}");

		conn.send(n);
		assertEquals(true, conn.validate());
	}

	@Before
	public void setUp() throws Exception {
		keystore = KeyStore.getInstance("PKCS12");
		InputStream certificate = Thread
				.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(
						"com/babeeta/butterfly/application/gateway/ios/impl/apns_cert.p12");
		if (certificate == null) {
			certificate = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"apns_cert.p12");
		}
		assertNotNull("keystore load failed.", certificate);
		keystore.load(certificate, DigestUtils.md5Hex(APID).toCharArray());
		certificate.close();

		accountService = EasyMock.createMock(AccountService.class);
		pooledApnConnectionFactory = new PooledApnConnectionFactory(
				accountService);
		EasyMock.expect(
				accountService.findByApplicationId(APID))
				.andReturn(keystore).once();
		EasyMock.expect(accountService.isApplicationInDevelopment(APID))
				.andReturn(
						true).times(2);
		EasyMock.replay(accountService);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDestroyObject() throws Exception {
		ApnsConnection conn = (ApnsConnection) pooledApnConnectionFactory
				.makeObject(APID);

		pooledApnConnectionFactory.destroyObject(APID, conn);

		SSLSocket socket = (SSLSocket) ReflectionTestUtils.getField(conn,
				"socket");
		assertEquals(true, socket.isClosed());

	}

	@Test
	public void testMakeObject() throws Exception {
		ApnsConnection conn = (ApnsConnection) pooledApnConnectionFactory
				.makeObject(APID);

		assertNotNull(conn);
		assertEquals(APID, conn.getApplicationId());

		SSLSocket socket = (SSLSocket) ReflectionTestUtils.getField(conn,
				"socket");

		assertNotNull(socket);
		assertEquals(true, socket.isConnected());
		assertEquals(true, conn.isDevelopment());
		assertEquals(new InetSocketAddress("gateway.sandbox.push.apple.com",
				2195), socket.getRemoteSocketAddress());
	}

	@Test
	public void testValidateObject() throws Exception {
		ApnsConnection conn = (ApnsConnection) pooledApnConnectionFactory
				.makeObject(APID);

		SSLSocket socket = (SSLSocket) ReflectionTestUtils.getField(conn,
				"socket");

		for (int i = 0; i < 2; i++) {
			// 几次Validate应该都是正常的
			assertEquals(true,
					pooledApnConnectionFactory.validateObject(APID, conn));

			assertEquals(true, socket.isConnected());
			Thread.sleep(1000L);
		}

		socket.close();

		assert socket.isClosed();

		assertEquals(false,
				pooledApnConnectionFactory.validateObject(APID, conn));
	}

}
