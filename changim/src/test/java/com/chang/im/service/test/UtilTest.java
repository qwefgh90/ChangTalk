package com.chang.im.service.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.chang.im.util.IMUtil;

public class UtilTest {
	@Test
	public void sha256(){
		String sha = IMUtil.sha256("hello");
		assertNotNull(sha);
	}
}
