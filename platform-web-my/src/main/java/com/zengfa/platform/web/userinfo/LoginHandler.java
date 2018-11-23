package com.zengfa.platform.web.userinfo;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * 登录接口.
 * 
 * 
 */
public abstract class LoginHandler {


	/**
	 * 用户验证通过后触发该事件(即将当前用户名写入session时)
	 * 
	 * @param sessAccount
	 */
	public void logined(Object sessAccount, HttpServletRequest request) {

	}

	/**
	 * 不需要登录的页面地址.
	 * 
	 * @return
	 */
	public abstract List<String> getExcludeUris();

	public List<String> getAdminExcludeUris() {
		return null;
	}
	
	/**
	 * 需要做并发限制的页面地址.
	 * 
	 * @return null:表示所有页面不做并发限制.
	 */
	public List<String> getConnectionLimitIncludeUris() {
		return null;
	}
}
