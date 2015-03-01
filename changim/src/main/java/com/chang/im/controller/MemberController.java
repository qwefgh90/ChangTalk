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

	@RequestMapping(value="/v1/member",method=RequestMethod.POST)
	ResponseEntity<ResultDTO>  registerMember(@RequestBody Member member){
		if(memberService.registerMember(member) == true){
			return new ResponseEntity<ResultDTO>(new ResultDTO(1), HttpStatus.OK);
		}else{
			return new ResponseEntity<ResultDTO>(new ResultDTO(0), HttpStatus.OK);			
		}
//		return new ResponseEntity<ResultTokenDTO>("LOGIN CONTROLLER",HttpStatus.OK); 
	}

	@RequestMapping(value="/v1/auth/login",method=RequestMethod.POST)
	ResponseEntity<String> login(){
		System.out.println("[MemberController]login");
		return new ResponseEntity<String>(HttpStatus.OK); 
	}

	@RequestMapping(value="/v1/auth/logout", method=RequestMethod.POST)
	ResponseEntity<String> logoutMember(){
		return new ResponseEntity<String>("LOGOUT CONTROLLER",HttpStatus.OK); 
	}
	

	@RequestMapping(value="/v1/hello",method=RequestMethod.GET)
	ResponseEntity<String> index(){
		String hello = "Hello World";
		return new ResponseEntity<String>(hello, HttpStatus.OK);
	}

	@RequestMapping(value="/hello",method=RequestMethod.GET)
	ResponseEntity<String> index2(){
		String hello = "Hello World";
		return new ResponseEntity<String>(hello, HttpStatus.OK);
	}

	@RequestMapping(value="/",method=RequestMethod.GET)
	ResponseEntity<String> index3(){
		String hello = "Hello World";
		return new ResponseEntity<String>(hello, HttpStatus.OK);
	}

}

class ResultDTO {
	int resultCode;

	public ResultDTO(int resultCode) {
		super();
		this.resultCode = resultCode;
	}
}
class ResultTokenDTO {
	int resultCode;
	String token;
	public ResultTokenDTO(int resultCode, String token) {
		super();
		this.resultCode = resultCode;
		this.token = token;
	}

}