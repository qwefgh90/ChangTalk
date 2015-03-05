package com.chang.im.service.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.chang.im.config.Application;
import com.chang.im.dao.IndexDAO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class IndexDAOTest {

	@Autowired 
	IndexDAO indexDAO;
	
	String roomIndex;
	String messageIndex;
	
	@Test
	public void increaseTest(){
		roomIndex = indexDAO.increaseRoomIndex();
		messageIndex = indexDAO.increaseMessageIndex(roomIndex);
		
		String croomIndex = roomIndex;
		String cmessageIndex = messageIndex;
		
		String nridx = indexDAO.increaseRoomIndex();
		String nidx = indexDAO.increaseMessageIndex(croomIndex);

		assertTrue(((Integer)(Integer.parseInt(croomIndex) + 1)).toString().equals(nridx));
		assertTrue(((Integer)(Integer.parseInt(cmessageIndex) + 1)).toString().equals(nidx));
		
		indexDAO.deleteRoomIndex();
		indexDAO.deleteMessageIndex(croomIndex);
		
		assertNull(indexDAO.currentRoomIndex());
		assertNull(indexDAO.currentMessageIndex(croomIndex));
	}
}
