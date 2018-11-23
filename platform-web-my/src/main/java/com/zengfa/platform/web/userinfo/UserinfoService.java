package com.zengfa.platform.web.userinfo;

import javax.servlet.http.HttpServletRequest;

public interface UserinfoService {

	boolean isExcludeUri(HttpServletRequest request);
	
	void setLoginHandler(LoginHandler loginHandler);
}
