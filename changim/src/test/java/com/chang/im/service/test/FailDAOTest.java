package com.chang.im.service.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.chang.im.config.Application;
import com.chang.im.dao.FailDAO;
import com.chang.im.dto.Packet;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class FailDAOTest {
	
	@Autowired
	FailDAO failDAO;
	
	@Before
	public void setup(){
		failDAO.clearFailMessage("qwefgh90", "-1");
	}
	
	@Test
	public void failQueueTest(){
		List<Packet> result = failDAO.getFailMessage("qwefgh90", "-1");
		assertNull(result);
		
		Packet packet = new Packet();
		packet.setContent("테스트");
		packet.setFromId("qwefgh90");
		packet.setMessageIndex("10");
		packet.setRoomId("-1");
		packet.setTimestamp(9999999L);
		
		failDAO.saveFailMessage("qwefgh90", packet);
		packet.setMessageIndex("20");
		failDAO.saveFailMessage("qwefgh90", packet);
		
		result = failDAO.getFailMessage("qwefgh90", "-1");
		assertTrue(result.size() == 2);
		
		failDAO.deleteFailMessage("qwefgh90", "-1", "10");
		
		result = failDAO.getFailMessage("qwefgh90", "-1");
		assertTrue(result.size() == 1);
		
		failDAO.clearFailMessage("qwefgh90", "-1");
		result = failDAO.getFailMessage("qwefgh90", "-1");
		assertNull(result);

		
	}
}
