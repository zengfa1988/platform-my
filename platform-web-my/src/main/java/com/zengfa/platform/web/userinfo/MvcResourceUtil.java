package com.zengfa.platform.web.userinfo;

import java.util.ArrayList;
import java.util.List;

public class MvcResourceUtil {

	private static final List<String> LIST = new ArrayList<String>();

	static {
		LIST.add(".css");
		LIST.add(".jpg");
		LIST.add(".gif");
		LIST.add(".png");
		LIST.add(".js");
		LIST.add(".txt");
		LIST.add(".apk");
		LIST.add(".ico");
		LIST.add(".htc");
		/*LIST.add(".html");
		LIST.add(".htm");*/
	}

	public static boolean isResource(String uri) {
		if (uri.endsWith(".do")) {
			return false;
		}
		for (String name : LIST) {
			if (uri.endsWith(name)) {
				return true;
			}
		}
		return false;
	}
}
