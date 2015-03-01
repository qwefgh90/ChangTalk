package com.chang.im.util;

import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

public class IMUtil {
	public static Map objectToMap(Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		return BeanUtils.describe(object);
	}
	public static void mapToObject(Map map, Object object) throws NullPointerException, IllegalAccessException, InvocationTargetException{
		if(map == null || object == null){
			throw new NullPointerException();
		}
		BeanUtils.populate(object, map);
	}
	public static String sha256(String str){
		String SHA = ""; 
		try{
			MessageDigest sh = MessageDigest.getInstance("SHA-256"); 
			sh.update(str.getBytes()); 
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer(); 
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			SHA = sb.toString();
			
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace(); 
			SHA = null; 
		}
		return SHA;
	}
	public static long getCurrentUnixTime(){
		return System.currentTimeMillis()/1000L;
	}
	
}
