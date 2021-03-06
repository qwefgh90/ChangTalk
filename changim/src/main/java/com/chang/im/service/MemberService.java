package com.chang.im.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chang.im.dao.LoginInfoDAO;
import com.chang.im.dao.MemberDAO;
import com.chang.im.dao.TokenDAO;
import com.chang.im.dto.LoginInfo;
import com.chang.im.dto.Member;
import com.chang.im.dto.MemberContext;
import com.chang.im.dto.TokenListItem;
import com.chang.im.util.IMUtil;

@Service
public class MemberService implements UserDetailsService{
	@Autowired
	MemberDAO memberDAO;

	@Autowired
	LoginInfoDAO loginInfoDAO;

	@Autowired
	TokenDAO tokenDAO;

	@Override
	public UserDetails loadUserByUsername(String id)
			throws UsernameNotFoundException {
		Member member = memberDAO.getMember(id);
		if(member == null)
			throw new UsernameNotFoundException("User " + id + " not found");
		else
			return new MemberContext(member);
	}

	public static int timeoutSecond = 3600;
	private static Long makeExpireTime(){
		return IMUtil.getCurrentUnixTime()+timeoutSecond*1000;
	}

	/**
	 * 회원 등록
	 * @param member
	 * @return
	 */
	public boolean registerMember(Member member){
		boolean result = isExistsMember(member);
		if(result == false && member.getId() != null && member.getPassword() != null){
			memberDAO.registerMember(member);
			return isExistsMember(member);
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
		return memberDAO.isExistsMember(member.getId());
	}

	/**
	 * 회원 삭제
	 * @param member
	 * @return
	 */
	public boolean deleteMember(Member member){
		boolean result = isExistsMember(member);
		if(result == true){
			memberDAO.deleteMember(member.getId());
			memberDAO.removeID(member.getId());
			return ! isExistsMember(member);
		}else{
			return true;
		}
	}

	/**
	 * 회원 정보
	 * ID정보만 포함되어 있으면 동작
	 * @param member
	 * @return
	 */
	public Member getMember(Member member){
		if(member == null || isExistsMember(member) == false){
			return null;
		}else{
			Member resultMember = memberDAO.getMember(member.getId());
			return resultMember;
		}
	}

	public boolean isValidIdAndPassword(String id, String password)
	{
		if(id != null && password != null){
			Member dbMember = memberDAO.getMember(id);
			if(dbMember.getId().equals(id) == true && dbMember.getPassword().equals(password) == true){
				return true;
			}
		}
		return false;
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
			Member dbMember = memberDAO.getMember(member.getId());
			if(isValidIdAndPassword(member.getId(),member.getPassword())){
				long unixtime = IMUtil.getCurrentUnixTime();
				long expire = makeExpireTime();	//3시간
				String token = IMUtil.sha256(member.getId() + unixtime);

				//transaction required
				LoginInfo info = new LoginInfo();
				info.setId(member.getId());
				info.setPhone(member.getPhone());
				info.setToken(token);
				info.setRoles(member.getRoles());
				loginInfoDAO.insertUserInfo(info, expire);

				TokenListItem item = new TokenListItem();
				item.setId(member.getId());
				item.setExpire(expire);
				item.setToken(token);
				tokenDAO.insertTokenList(item);

				return true;
			}else{
				return false;
			}
		}
	}

	/**
	 * 토큰 삭제
	 * @param token
	 * @return
	 */
	public boolean logout(String token){
		boolean exists = loginInfoDAO.isExistsUserInfo(token);
		if(exists== false){
			return false;
		}else{
			LoginInfo info = loginInfoDAO.getUserInfo(token);
			loginInfoDAO.deleteUserInfo(token);
			tokenDAO.deleteTokenList(info.getId());
			return true;
		}
	}

	public TokenListItem getTokenListItem(String id){
		if(id == null)
			return null;
		return tokenDAO.getTokenList(id);
	}

	public LoginInfo getUserInfo(String token){
		if(token == null)
			return null;
		return loginInfoDAO.getUserInfo(token);
	}

	public boolean isExistToken(String token){
		if(token == null)
			return false;
		return loginInfoDAO.isExistsUserInfo(token);
	}
	//1425235429 1425235610
	public boolean updateTokenDate(String token){
		if(token == null)
			return false;
		LoginInfo info = getUserInfo(token);
		if(null != info){
			TokenListItem tokenItem = getTokenListItem(info.getId());
			if(null != tokenItem){
				tokenItem.setExpire(makeExpireTime());
				//expire Timeout 적용됨
				tokenDAO.insertTokenList(tokenItem);
				loginInfoDAO.insertUserInfo(info, tokenItem.getExpire());
				return true;
			}
		}
		return false;
	}

	public boolean getLoginState(String id){

		if(id == null)
			return false;
		return tokenDAO.isExistsTokenList(id);
	}
	
	@Deprecated
	public Set<String> getAllID(){
		return memberDAO.getAllID();
	}
	public void addID(String id){
		memberDAO.addID(id);
	}
	public void removeID(String id){
		memberDAO.removeID(id);
	}
	
}

