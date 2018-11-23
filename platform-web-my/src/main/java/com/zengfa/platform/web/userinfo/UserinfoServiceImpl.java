package com.zengfa.platform.web.userinfo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.zengfa.platform.util.ListUtil;
import com.zengfa.platform.web.userinfo.util.RequestUtil;

public class UserinfoServiceImpl implements UserinfoService {

	private static List<String> EXCLUDE_URI_LIST = new ArrayList<String>();
	static {
		EXCLUDE_URI_LIST.add("/monitor/");
		EXCLUDE_URI_LIST.add("/webservice/");
		EXCLUDE_URI_LIST.add("/platform/");
		EXCLUDE_URI_LIST.add("/login.do");
	};

	private LoginHandler loginHandler;

	private UriListChecker excludeLoginUriListChecker;// 忽略登录的URL列表
	
	@Override
	public void setLoginHandler(LoginHandler loginHandler) {
		this.loginHandler = loginHandler;
		excludeLoginUriListChecker = new UriListChecker(this.getExcludeUris(this.loginHandler));
	}

	protected List<String> getExcludeUris(LoginHandler loginHandler) {
		List<String> excludeUris = loginHandler.getExcludeUris();
		excludeUris = ListUtil.defaultList(excludeUris);
		excludeUris.addAll(EXCLUDE_URI_LIST);
		return excludeUris;
	}
	
	@Override
	public boolean isExcludeUri(HttpServletRequest request) {
		String uri = RequestUtil.getRequestContextUri(request);
		boolean isExcludeUri = excludeLoginUriListChecker.exists(uri);
		return isExcludeUri;
	}
}
