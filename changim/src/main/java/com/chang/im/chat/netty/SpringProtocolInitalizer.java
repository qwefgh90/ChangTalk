package com.chang.im.chat.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
/**
 * Just a dummy protocol mainly to show the ServerBootstrap being initialized.
 * 
 * @author Abraham Menacherry
 * 
 */
@Component
@Qualifier("springProtocolInitializer")
public class SpringProtocolInitalizer extends ChannelInitializer<SocketChannel> {

	@Autowired
	StringDecoder stringDecoder;

	@Autowired
	StringEncoder stringEncoder;
	
	@Autowired
	JsonAndAuthDecoder jsonAndAuthDecoder;

	@Autowired
	JsonHandler jsonHandler;

	/**
	 * 서버 채널 파이프라인 설정
	 */
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("decoder", stringDecoder);
		pipeline.addLast("encoder", stringEncoder);
		pipeline.addLast("jsonCodec", jsonAndAuthDecoder);
		pipeline.addLast("jsonHandler", jsonHandler);	//모든 파이프라인이 핸들러를 공유한다.
	}

	public StringDecoder getStringDecoder() {
		return stringDecoder;
	}

	public void setStringDecoder(StringDecoder stringDecoder) {
		this.stringDecoder = stringDecoder;
	}

	public StringEncoder getStringEncoder() {
		return stringEncoder;
	}

	public void setStringEncoder(StringEncoder stringEncoder) {
		this.stringEncoder = stringEncoder;
	}

}
