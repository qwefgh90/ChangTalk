package com.chang.im.service.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.chang.im.util.IMUtil;

/**
 * 
 * @author cheochangwon
 * 해시함수 테스트
 * MessageDigest를 사용하므로 간한단 테스트
 */
public class UtilTest {
	@Test
	public void sha256(){
		String sha = IMUtil.sha256("hello");
		String sha2 = IMUtil.sha256("hello");
		assertTrue(sha.equals(sha2));
	}
}
