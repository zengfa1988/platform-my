package com.zengfa.platform.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zengfa.platform.util.CookieUtil;
import com.zengfa.platform.util.bean.Result;
import com.zengfa.platform.util.bean.ReturnDTO;
import com.zengfa.platform.web.userinfo.SessionKey;


@Controller
@RequestMapping(value = "/platform")
public class UserCenterLoginController {
	
	/**
	 * 用于用户未授权登陆返回未授权状态的ReturnDTO...
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping
	@ResponseBody
	public void  unauthorizedloginOut(HttpServletRequest request,HttpServletResponse response) throws IOException {
		
		String login_url="login.html";
		response.sendRedirect(login_url);
		//response.sendRedirect("http://auc.tsh.com:8030/views/login.html");
		//return new Result().NO(500, "未授权的访问");
	}
	@RequestMapping
	@ResponseBody
	public ReturnDTO  unauthorized() throws IOException {
		
		return new Result().NO(500, "未授权的访问").DTO();
	}
	
	@RequestMapping
	@ResponseBody
	public ReturnDTO  unauthorizedApp() throws IOException {
		
		return new Result().NO(401, "未授权的访问(登录过期)").DTO();
	}
	
	@RequestMapping
	@ResponseBody
	public ReturnDTO  loginAuthorized() throws IOException {
		
		return new Result().NO(403, "未授权的访问(登录已被强制退出)").DTO();
	}
	/**
	 * 获取功能编码
	 * @param request
	 * @return
	 */
//	@RequestMapping
//	@ResponseBody
//	public List<String>  getfunCode(HttpServletRequest request) {
//		
//		List<UserResMenu> resourcesMenu = UserUtil.getUserResMenu(request);
//		if(resourcesMenu==null||resourcesMenu.size()==0){
//			return null;
//		}
//		List<String> menuCode = new ArrayList<String>();
//		for (UserResMenu userResMenu : resourcesMenu) {
//			if(userResMenu.getLevel()==3){
//				menuCode.add(userResMenu.getFunCode());
//			}
//		}
//		return menuCode;
//	}
	
//	@RequestMapping
//	public void setCookie(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		String token = request.getParameter(SessionKey.SESSION_TOKEN);
//		String url = request.getParameter(SessionKey.SESSION_ORIGINALURL);
//
//		if (StringUtils.isNotBlank(token)) {
//			CookieUtil.setTopDomainCookie(SessionKey.SESSION_TOKEN, token, SessionKey.TOKEN_EXPIRE_TIME, true, request, response);
//			// 此处验证token，验证失败，不能跳转到登录中心，否则会引起循环访�			
//			UserInfo user = TokenUtil.getUser(token);
//			if (user != null && user.getUserId()>0) {
//				CookieUtil.setTopDomainCookie(SessionKey.SESSION_LOGINED, String.valueOf(user.getUserId()), SessionKey.TOKEN_EXPIRE_TIME, false, request,
//						response);
//			}
//		}
//
//		if (StringUtils.isBlank(url)) {
//			url = RequestUtil.getBasePath(request);
//		}
//
//		response.sendRedirect(url);
//	}

//	@RequestMapping
//	@ResponseBody
//	public JSONPObject logOut(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		String callback = request.getParameter("callback");
//		CookieUtil.deleteCookie(SessionKey.SESSION_TOKEN, request, response);
//		CookieUtil.deleteCookie(SessionKey.SESSION_LOGINED, request, response);
//		request.getSession().invalidate();
//		String logOutUrl = AppProperties.getProperty("user_center") + "/logOut.do";
//		return new JSONPObject(callback, logOutUrl);
//	}
}
