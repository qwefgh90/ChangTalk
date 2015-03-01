package com.chang.im.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chang.im.dao.MemberDAO;
import com.chang.im.dto.Member;
import com.chang.im.dto.MemberContext;
import com.chang.im.util.IMUtil;

@Service
public class MemberService implements UserDetailsService{
	@Autowired
	MemberDAO memberDAO;

	@Override
	public UserDetails loadUserByUsername(String id)
			throws UsernameNotFoundException {
		Member member = memberDAO.getMember(id);
		if(member == null)
			return null;
		else
			return new MemberContext(member);
	}

	/**
	 * 회원 등록
	 * @param member
	 * @return
	 */
	public boolean registerMember(Member member){
		boolean result = isExistsMember(member);
		if(result == false){
			memberDAO.registerMember(member);
			result = isExistsMember(member);
			return result;
		}else{
			return false;
		}

	}

	/**
	 * 회원이 존재하는지 확인
	 * @param member
	 * @return
	 */
	public boolean isExistsMember(Member member){
		Member memberinfo = memberDAO.getMember(member.getId());
		return memberinfo == null ? false : true;
	}

	/**
	 * 회원 삭제
	 * @param member
	 * @return
	 */
	public boolean deleteMember(Member member){
		boolean result = isExistsMember(member);
		if(result==true){
			memberDAO.deleteMember(member.getId());
			result = !isExistsMember(member);
			return result;
		}else{
			return true;
		}
	}

	/**
	 * 회원 정보
	 * @param member
	 * @return
	 */
	public Member getMember(Member member){
		if(member == null || this.isExistsMember(member) == false){
			return null;
		}else{
			Member resultMember = memberDAO.getMember(member.getId());
			return resultMember;
		}
	}

	/**
	 * 존재 유무 확인 후 로그인
	 * @param member
	 * @return
	 */
	public boolean login(Member member){
		if(isExistsMember(member) == false){
			return false;
		}else{
			Member dbMember = this.getMember(member);
			if(dbMember.getId().equals(member.getId()) == true && dbMember.getPassword().equals(member.getPassword()) == true){
				long unixtime = IMUtil.getCurrentUnixTime();
				long expire = IMUtil.getCurrentUnixTime()+3600*3;
				String hash = IMUtil.sha256(member.getId() + unixtime);
				member.setToken(hash);
				member.setExpire(expire);
				//transaction required
				memberDAO.insertTokenList(member);
				memberDAO.insertUserInfo(member);
				return true;
			}else{
				return false;
			}
		}
	}

	public boolean logout(String token){
		boolean exists = memberDAO.isExistsUserInfo(token);
		if(exists== false){
			return false;
		}else{
			Member info =memberDAO.getUserInfo(token);
			memberDAO.deleteUserInfo(token);
			memberDAO.deleteTokenList(info);
			return true;
		}

	}
}
