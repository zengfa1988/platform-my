package com.zengfa.platform.web.userinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.zengfa.platform.util.CookieUtil;
import com.zengfa.platform.util.security.UserInfo;
import com.zengfa.platform.util.security.UserResMenu;


public class UserUtil {

	public UserUtil(){}
	
	
	/**
	 * 获取用户所在系统中的sessionId
	 * @param request
	 * @return
	 */
	public static String getSessionId(HttpServletRequest request){
		String token = CookieUtil.getCookie(SessionKey.ZHC_COOKIE_USER_NAME, request);
		
		if(StringUtils.isBlank(token)){
			return null;
		}
		return token;
		
	}
	
	/**
	 * 获取登陆后的User信息
	 * @param request
	 * @return
	 */
	public static UserInfo getUserInfo(HttpServletRequest request){
		String 	  token = request.getParameter("token");//屏端token
		if(StringUtils.isBlank(token)){
			token = LoginUtil.checkLoginByCookie(request, SessionKey.ZHC_COOKIE_USER_NAME);//web端token
		}
		
		
		if(StringUtils.isBlank(token)){
			return null;
		}
		
		UserInfo loginInfo = (UserInfo) request.getSession().getAttribute(token);
		
//		UserInfo loginInfo = (UserInfo) SerializeUtil.unserialize(RedisSlave.getInstance().get(token));
		
		if(null == loginInfo){
			return null;
		}
//		loginInfo.setResourcesMenu(null);
		loginInfo.setSessionId(token);
		
		return loginInfo;
		
	}
	/**
	 * 获取登陆后的功能菜单
	 * @param request
	 * @return
	 */
	public static List<UserResMenu> getUserResMenu(HttpServletRequest request){
		String 	  token = request.getParameter("token");//屏端token
		if(StringUtils.isBlank(token)){
			token = CookieUtil.getCookie(SessionKey.ZHC_COOKIE_USER_NAME, request);
		}
		if(StringUtils.isBlank(token)){
			return null;
		}
		UserInfo loginInfo = (UserInfo) request.getSession().getAttribute(token);
//		UserInfo loginInfo = (UserInfo) SerializeUtil.unserialize(RedisSlave.getInstance().get(token));
		
		if(null == loginInfo){
			return null;
		}
		
		return loginInfo.getResourcesMenu();
	}
	
	/**
	 * 获取登陆后的用户数据权限
	 * @param request
	 * @return
	 */
	public static Map<Integer,List<Long>> getUserDataAuthority(HttpServletRequest request){
		
		UserInfo loginInfo = getUserInfo(request);
		
		if(null == loginInfo){
			return null;
		}
		
		return loginInfo.getDataAuthority();
		
	}
	/**
	 * 获取登陆后的UserId
	 * @param request
	 * @return
	 */
	public static Long getUserId(HttpServletRequest request){
		

		UserInfo loginInfo = getUserInfo(request);
		
		if(null != loginInfo)
			return loginInfo.getUserId();
		
		return null;
		
	}
	
	/**
	 * 获取登陆后的角色类型
	 * @param request
	 * @return
	 */
	public static Integer getUserRoleType(HttpServletRequest request){
		

		UserInfo loginInfo = getUserInfo(request);
		
		if(null != loginInfo)
			return loginInfo.getRoleType();
		
		return null;
		
	}

	/**
	 * 获取该用户的供应商标识ID
	 * 获取该用户的网点标识ID
	 * 获取该用户的县域标识ID
	 * @param request
	 * @return
	 */
	public static Long getBizId(HttpServletRequest request){
		

		UserInfo loginInfo = getUserInfo(request);
		
		if(null != loginInfo)
			return loginInfo.getBizId();
		
		return null;
		
	}

	/**
	 * 获取该用户的网点或供应商
	 * 所属的县域标识ID
	 * @param request
	 * @return
	 */
	public static Long getUserForAreaId(HttpServletRequest request){

		UserInfo loginInfo = getUserInfo(request);
		
		if(null != loginInfo)
			return loginInfo.getBelongId();
		
		return null;
		
	}

	/**
	 * 获取用户名称
	 * @param request
	 * @return
	 */
	public static String getUserName(HttpServletRequest request){

		UserInfo loginInfo = getUserInfo(request);
		
		if(null != loginInfo)
			return loginInfo.getLoginName();
		return null;
	}
	
}
