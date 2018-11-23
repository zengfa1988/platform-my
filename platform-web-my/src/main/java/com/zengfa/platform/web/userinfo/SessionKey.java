package com.zengfa.platform.web.userinfo;

public class SessionKey {

	public static final String SESSIONID_COOKIE_NAME = "jsessionid";
	public static final String USERNAME_SESSION_NAME = "sessUsername";
	public static final String USERID_SESSION_NAME = "sessUserId";
	public static final String USERINFO_SESSION_NAME = "sessUserInfo";

	public static final String SESSION_TOKEN = "token";
	public static final String SESSION_ORIGINALURL = "originalUrl";
	public static final String SESSION_SETCOOKIE = "setCookie";
	public static final String SESSION_LOGINED = "logined";
								
	/**
	 * 单位秒.
	 */
	public static final int TOKEN_EXPIRE_TIME = 60 * 30;     
	
	//redis常量
	public static final int COOKIE_EXPIRATIOIN_TIME = 60 * 30; // cookie过期时间
	
	
	public static final String REDIS_KEY_LEAF_MUEN_VALUE = "resource_muen_";
	public static final String REDIS_KEY_ALL_MUEN_VALUE = "resource_muen_all_";
	
	public static final String COOKIE_USER_NAME = "zhc_nk";
	public static final String ZHC_COOKIE_USER_NAME = "zhc_jsession";
	
	
	public static String  APP_TOKEN_PREFIX = "token_tsh_";
	 public static Integer APP_TOKEN_EXPIRE_TIME = 86400 * 7;		//一周

	public static final int REDIS_KEY_BASE_TIME = 60 * 60 * 24 * 3;
}
