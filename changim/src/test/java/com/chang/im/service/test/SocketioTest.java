package com.chang.im.service.test;

import static org.junit.Assert.*;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.chang.im.config.Application;

/**
 * https://github.com/Gottox/socket.io-java-client
 * socket.io-java-clint로 socketio 테스트
 * @author cheochangwon
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class SocketioTest {

	String host = "localhost";
	int port = 9090;
	String url;
	String token;
	SocketIO socket;

	/** Countdown latch */
	private CountDownLatch lock = new CountDownLatch(1);

	@Before
	public void setup(){
		token = "true";
		url = "http://" +host+ ":" +port;
	}

	boolean checkConnect;
	
	@Test
	public void connection() throws MalformedURLException, InterruptedException{
		socket = new SocketIO(url);
		socket.addHeader("token", token);
		socket.connect(new IOCallback() {

			@Override
			public void on(String arg0, IOAcknowledge arg1, Object... arg2) {
				System.out.println("[on]"+arg0 +", "+arg1 +", "+arg2);
//				lock.countDown();
			}
			@Override
			public void onConnect() {
				System.out.println("[onConnect]");
				checkConnect = true;
//				lock.countDown();
			}
			@Override
			public void onDisconnect() {
				System.out.println("[onDisconnect]");
//				lock.countDown();
			}
			@Override
			public void onError(SocketIOException arg0) {
				System.out.println("[onError]"+arg0);
//				lock.countDown();
			}
			@Override
			public void onMessage(String arg0, IOAcknowledge arg1) {
				System.out.println("[onMessage]"+arg0);
//				lock.countDown();
			}
			@Override
			public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
				System.out.println("[onMessage]" + arg0);
//				lock.countDown();
			}
		});
		lock.await(3000,TimeUnit.MILLISECONDS);
		//onConnect
		assertTrue(checkConnect);

	}
}
