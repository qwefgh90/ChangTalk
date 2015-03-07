package com.chang.im.service.test;



import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.chang.im.config.Application;
import com.chang.im.dao.LoginInfoDAO;
import com.chang.im.dao.MemberDAO;
import com.chang.im.dao.MessageDAO;
import com.chang.im.dao.TokenDAO;
import com.chang.im.dto.Packet;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class MessageDAOTest {
	@Autowired
	MemberDAO memberDAO;

	@Autowired
	LoginInfoDAO userInfoDAO;

	@Autowired
	TokenDAO tokenDAO;

	@Autowired
	MessageDAO messageDAO;
	
	Long roomId = -1L;

	@Before
	public void before(){
		messageDAO.delete("Message:-1");
	}
	
	@After
	public void after(){
		
	}
	
	@Test
	public void roomTest(){
		final String userId = "qwefgh90";
		final String roomId = "hellroom";
		messageDAO.saveRoomList(userId,roomId);
		messageDAO.saveRoomUser(roomId, userId);
		
		assertTrue(messageDAO.findRoomList(userId, roomId));
		assertTrue(messageDAO.findRoomUser(roomId, userId));

		messageDAO.deleteRoomList(userId, roomId);
		messageDAO.deleteRoomUser(roomId, userId);
			
		assertFalse(messageDAO.findRoomList(userId, roomId));
		assertFalse(messageDAO.findRoomUser(roomId, userId));
	}
	
	@Test
	public void messageTest(){
		sendMessage();
		readMessage();
	}
	
	public void sendMessage(){
		Packet p = new Packet();
		p.setTimestamp(1111L);
		messageDAO.saveMessage(roomId.toString(), p);
		p.setTimestamp(2222L);
		messageDAO.saveMessage(roomId.toString(), p);
	}
	
	public void readMessage(){
		Set<String> string = messageDAO.readMessage(roomId.toString(), 1111L);
		assertTrue(string.size() == 2);
	}
}

