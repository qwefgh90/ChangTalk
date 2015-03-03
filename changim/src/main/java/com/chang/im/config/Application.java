package com.chang.im.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.chang.im.dto.LoginInfo;
import com.chang.im.dto.Member;
import com.chang.im.dto.TokenListItem;

@Configuration
//현재의 클래스가 Spring의 설정파일임을 어플리케이션 컨텍스트에게 알려주는 역할을 합니다.
@ComponentScan(basePackages={"com.chang.im.controller","com.chang.im.service","com.chang.im.dao","com.chang.im.chat.controller"})
//Spring에게 hello 패키지 안에서 다른 컴포넌트, 설정, 서비스를 찾도록 합니다. 이 설정을 통해 Controller를 찾는것이 가능해집니다.
@EnableAutoConfiguration
//classpath를 기준으로 properties의 소스를 알려주는 어노테이션
@PropertySource("classpath:com/chang/im/config/redis.properties")
//Spring Boot가 클래스패스 세팅, 다른 Bean들, 다양한 설정들에 의해 Bean을 추가하도록 합니다.
@Import({ SecurityConfiguration.class, Initializer.class })
public class Application {

	@Value("${redis.host}")
	String host;
	@Value("${redis.port}")
	int port;
	@Value("${redis.passwd}")
	String passwd;
	
	public static void main(String[] args) {
		SpringApplication.run( Application.class, args);
	}
	
	/**
	 * @Value를 사용하기 위해 필요한 빈 
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

}
