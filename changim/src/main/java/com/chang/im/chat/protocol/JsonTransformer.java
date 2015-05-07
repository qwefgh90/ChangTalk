package com.chang.im.chat.protocol;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

public abstract class JsonTransformer{
	final ObjectMapper mapper = new ObjectMapper();
	public abstract JSONObject json() throws Exception;
}
