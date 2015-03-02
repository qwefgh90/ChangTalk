package com.chang.im.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.chang.im.security.AuthenticationService;
import com.chang.im.security.AuthenticationServiceImpl;
import com.chang.im.security.RestAuthenticationFilter;
import com.chang.im.security.UnauthorizedEntryPoint;
import com.chang.im.service.MemberService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	MemberService memberService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
	 	http
		.csrf()
 			.disable()	
 		.addFilterAfter(restAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)	//커스텀 인증 필터
 		.sessionManagement()
 			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
 			.and()
 		.exceptionHandling()	
 			.authenticationEntryPoint(unauthorizedEntryPoint())	//예외 발생시 핸들러 등록
 			.and()
		.authorizeRequests()	//use-expressions = true 를 포함함
			//위쪽부터 일치하는지 검사하므로 허가 허용하고 싶을 경우 위쪽으로 배치
			.regexMatchers("/").permitAll()	//URL 허가
			.regexMatchers("/hello").permitAll()	//URL 허가
			.regexMatchers(HttpMethod.POST,"/v1/member").permitAll()	//회원 가입
			.regexMatchers("/v1/.*").fullyAuthenticated();	//회원 가입
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		auth
		.userDetailsService(memberService);
		//.passwordEncoder(passwordEncoder);	//실제 서비스 시 인코더 적용?
	}
	
	/**
	 * 빈 설정
	 * 예외 발생 시 401 발생 핸들러
	 * @return
	 */
	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return new UnauthorizedEntryPoint();
	}

	/**
	 * 빈 설정
	 * 인증 필터 설정
	 * @return
	 */
	@Bean
	public RestAuthenticationFilter restAuthenticationFilter(){
		return new RestAuthenticationFilter();
	}

	/**
	 * 빈 설정
	 * 인증 전용 서비스
	 * @return
	 */
	@Bean
	public AuthenticationService authenticationService(){
		return new AuthenticationServiceImpl();
	}
}


/**
 * 참고 XML
 * 
<?xml version="1.0" encoding="UTF-8"?>
<bean:beans
	xmlns:bean="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:c="http://www.springframework.org/schema/c"
	xmlns="http://www.springframework.org/schema/security"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
		http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
		http://www.springframework.org/schema/security
		http://www.springframework.org/schema/security/spring-security-3.2.xsd">

	<!--
	Applies to root appContext beans only, for MVC Controllers is this declaration repeated in MVC config.
	Actually, we currently don't need this as we have on annotation outside of MVC.
	There is more here that can go wrong. If you use interface-based proxy (our demo uses CGLib), you may
	need to add proxy-target-class="true" as well. Book "Spring Security 3.1", Chapter 10, Fine-grained
	Access Control, from header "Method security on Spring MVC controllers" on discusses these topics.
	-->
	<global-method-security secured-annotations="enabled"/>

	<http realm="Protected API"
		use-expressions="true"
		create-session="stateless"
		entry-point-ref="unauthorizedEntryPoint"
		authentication-manager-ref="restAuthenticationManager">

		<!--
		This is not easily possible, because it causes:
		DEBUG o.s.s.w.a.ExceptionTranslationFilter - Authentication exception occurred; redirecting to authentication entry point
		org.springframework.security.authentication.AuthenticationCredentialsNotFoundException: An Authentication object was not found in the SecurityContext
		-->
		<!--<anonymous enabled="false"/>-->
		<custom-filter ref="restAuthenticationFilter" position="FORM_LOGIN_FILTER"/>

		<intercept-url pattern="/*" access="permitAll"/>
		<intercept-url pattern="/secure/**" access="isFullyAuthenticated()"/>
	</http>

	<bean:bean id="unauthorizedEntryPoint" class="com.github.virgo47.respsec.main.restsec.UnauthorizedEntryPoint"/>

	<bean:bean id="userDetailService" class="com.github.virgo47.respsec.main.secimpl.MyUserDetailsService"/>

	<authentication-manager id="restAuthenticationManager">
		<authentication-provider user-service-ref="userDetailService">
			<!--
			Default password encoder is PlaintextPasswordEncoder, which fits with our hardcoded users.
			Obviously not a good choice otherwise.
			-->
		</authentication-provider>
	</authentication-manager>

	<bean:bean id="tokenManager" class="com.github.virgo47.respsec.main.secimpl.TokenManagerSingle"/>

	<bean:bean id="authenticationService" class="com.github.virgo47.respsec.main.secimpl.AuthenticationServiceDefault"
		c:authenticationManager-ref="restAuthenticationManager" c:tokenManager-ref="tokenManager"/>

	<bean:bean id="restAuthenticationFilter" class="com.github.virgo47.respsec.main.restsec.TokenAuthenticationFilter"
		c:authenticationService-ref="authenticationService" c:logoutLink="/logout"/>
</bean:beans>

*/