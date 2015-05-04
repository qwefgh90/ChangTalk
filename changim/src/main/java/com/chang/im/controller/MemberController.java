package com.chang.im.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chang.im.dto.Member;
import com.chang.im.service.MemberService;

@RestController
public class MemberController {

	@Autowired
	MemberService memberService;
	
	// 로그인, 로그아웃은 Spring Security의 인증필터에서 처리 
	// RestAuthenticationFilter.class
	// "/v1/auth/login";
	// "/v1/auth/logout";
		
	/**
	 * \@RequestsBody를 이용해 Obejct 응답 (Object -> JSON 매핑)을 할 경우 Object의 Setter/Getter가 없을경우
	 * 406 에러를 발생시킨다. 정말 중요하다.
	 * @param member
	 * @return
	 */
	
	@RequestMapping(value="/v1/member", method=RequestMethod.POST)
	ResponseEntity<ResultDTO>  registerMember(@RequestBody Member member){
		member.setRoles(Member.MEMBER_ROLE);
		if(memberService.registerMember(member) == true){
			return new ResponseEntity<ResultDTO>(new ResultDTO(1), HttpStatus.OK);
		}else{
			return new ResponseEntity<ResultDTO>(new ResultDTO(0), HttpStatus.OK);			
		}
	}

	@RequestMapping(value="/v1/hello",method=RequestMethod.GET)
	ResponseEntity<String> index(){
		String hello = "Hello World";
		return new ResponseEntity<String>(hello, HttpStatus.OK);
	}

	@RequestMapping(value="/hello",method=RequestMethod.GET)
	ResponseEntity<ResultDTO> index2(){
		String hello = "Hello World";
		return new ResponseEntity<ResultDTO>(new ResultDTO(1), HttpStatus.OK);
	}

}

class ResultDTO {
	int resultCode;

	public ResultDTO(int resultCode) {
		super();
		this.resultCode = resultCode;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
}