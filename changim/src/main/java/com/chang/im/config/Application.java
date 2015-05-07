package com.chang.im.config;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.chang.im.chat.netty.TCPServer;
import com.chang.im.dto.LoginInfo;
import com.chang.im.dto.Member;
import com.chang.im.dto.Packet;
import com.chang.im.dto.TokenListItem;
import com.chang.im.service.MessageListenerImpl;
import com.nhncorp.mods.socket.io.SocketIOSocket;

@Configuration
//현재의 클래스가 Spring의 설정파일임을 어플리케이션 컨텍스트에게 알려주는 역할을 합니다.
@ComponentScan(basePackages={"com.chang.im.controller","com.chang.im.service"
		,"com.chang.im.dao","com.chang.im.chat.netty"
		//,"com.chang.im.chat.controller"
})
//Spring에게 hello 패키지 안에서 다른 컴포넌트, 설정, 서비스를 찾도록 합니다. 이 설정을 통해 Controller를 찾는것이 가능해집니다.
@EnableAutoConfiguration
@PropertySource("classpath:com/chang/im/config/redis.properties")
//classpath를 기준으로 properties의 소스를 알려주는 어노테이션
@Import({NettyConfiguration.class, SecurityConfiguration.class, Initializer.class })
//Spring Boot가 클래스패스 세팅, 다른 Bean들, 다양한 설정들에 의해 Bean을 추가하도록 합니다.
public class Application {
	@Value("${redis.host}")
	String host;
	@Value("${redis.port}")
	int port;
	@Value("${redis.passwd}")
	String passwd;

	public static void main(String[] args) {
		ConfigurableApplicationContext context  = null;
		TCPServer svr = null;
		try{
			context = SpringApplication.run( Application.class, args);
			svr = (TCPServer)context.getBean("tcpServer");
			svr.start();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	/**
	 * \@Value를 사용하기 위해 필요한 빈 
	 * @return
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public JedisConnectionFactory jedisConnFactory(){
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName(host);
		factory.setPort(port);
		factory.setPassword(passwd);
		factory.setUsePool(true);
		return factory;
	}	

	@Bean
	public RedisTemplate<String,String> redisTemplate(){
		RedisTemplate<String,String> template = new RedisTemplate<String,String>();
		template.setConnectionFactory(jedisConnFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		return template;
	}

	@Bean
	public RedisTemplate<String,String> redisTemplateForMessage(){
		RedisTemplate<String,String> template = new RedisTemplate<String,String>();
		template.setConnectionFactory(jedisConnFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		return template;
	}

	@Bean
	public RedisTemplate<String,Member> redisTemplateForMember(){
		RedisTemplate<String,Member> template = new RedisTemplate<String,Member>();
		template.setConnectionFactory(jedisConnFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new JacksonJsonRedisSerializer<Member>(Member.class));
		return template;
	}

	@Bean
	public RedisTemplate<String,LoginInfo> redisTemplateForUserinfo(){
		RedisTemplate<String,LoginInfo> template = new RedisTemplate<String,LoginInfo>();
		template.setConnectionFactory(jedisConnFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new JacksonJsonRedisSerializer<LoginInfo>(LoginInfo.class));
		return template;
	}

	@Bean
	public RedisTemplate<String,TokenListItem> redisTemplateForTokenListItem(){
		RedisTemplate<String,TokenListItem> template = new RedisTemplate<String,TokenListItem>();
		template.setConnectionFactory(jedisConnFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new JacksonJsonRedisSerializer<TokenListItem>(TokenListItem.class));
		return template;
	}

	@Bean
	public RedisTemplate<String,String> redisTemplateForFail(){
		RedisTemplate<String,String> template = new RedisTemplate<String,String>();
		template.setConnectionFactory(jedisConnFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new JacksonJsonRedisSerializer<Packet>(Packet.class));
		return template;
	}

	////Messanger

	@Bean
	RedisMessageListenerContainer redisContainer() {
		final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory( jedisConnFactory() );
		container.addMessageListener( sampleMessageListener(), sampleTopic() );
		return container;
	}

	@Bean
	MessageListenerAdapter sampleMessageListener() {
		return new MessageListenerAdapter( new MessageListenerImpl() );
	}

	@Bean
	ChannelTopic sampleTopic() {
		return new ChannelTopic( "CHANGIM" );
	}

	@Bean
	ConcurrentHashMap<String, Channel> tokenChannelMap(){
		return new ConcurrentHashMap<String, Channel>();
	}

	@Bean
	ConcurrentHashMap<String, List<MessageListener>> listenerMap(){
		return new ConcurrentHashMap<String, List<MessageListener>>();
	}

	@Bean
	ConcurrentHashMap<Channel, String> channelIdMap(){
		return new ConcurrentHashMap<Channel, String>();
	}

}
