/**
 * 
 * @(#)RequestUtil.java V0.0.1 2010-3-30
 */
package com.zengfa.platform.web.userinfo.util;

import java.util.Enumeration;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.zengfa.platform.util.CookieUtil;
import com.zengfa.platform.util.StringUtil;
import com.zengfa.platform.web.userinfo.SessionKey;

/**
 * 
 */
public class RequestUtil {
	private static final Log logger = LogFactory.getLog(RequestUtil.class);

	public static String getRequestContextUri(HttpServletRequest request) {
		return com.zengfa.platform.util.RequestUtil.getRequestContextUri(request);
	}

	/**
	 * 获取上传的文件.
	 * 
	 * @param request
	 *            请求
	 * @param name
	 *            文件名
	 * @return 文件
	 */
	public static MultipartFile getFile(HttpServletRequest request, String name) {
		// try {
		MultipartHttpServletRequest mrequest = (MultipartHttpServletRequest) request;
		MultipartFile file = mrequest.getFile(name); // 发送对象
		if (file == null || file.isEmpty()) {
			return null;
		}
		return file;
		// }
		// catch (ClassCastException e) {
		// logger.warn("ClassCastException " + e.getMessage());
		// return null;
		// }
	}

	/**
	 * 获取域名.
	 * 
	 * @param request
	 * @return
	 */
	public static String getDomain(HttpServletRequest request) {
		String domain = "http://" + request.getServerName();
		return domain;
	}

	/**
	 * 获取user-agent.
	 */
	public static String getUserAgent(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		return userAgent;
	}

	/**
	 * 获取代理服务器IP.
	 * 
	 * @param request
	 * @return
	 */
	public static String getProxyIp(HttpServletRequest request) {
		return com.zengfa.platform.util.RequestUtil.getProxyIp(request);
	}

	/**
	 * 从cookie获取用户通行证.
	 * 
	 * @param request
	 * @return
	 */
	public static String getCookieUsername(HttpServletRequest request) {
		String username = CookieUtil.getCookie("username", request);
		username = StringUtil.urlDecode(username);
		return username;
	}

	/**
	 * 把null或无效的页码转成1.
	 * 
	 * @param pageid
	 * @return
	 */
	public static int getPageid(Integer pageid) {
		if (pageid == null || pageid <= 0) {
			return 1;
		}
		return pageid;
	}

	/**
	 * 从请求中获取页码.
	 * 
	 * @param request
	 * @return
	 */
	public static int getPageid(HttpServletRequest request) {
		int pageid = NumberUtils.toInt(request.getParameter("page"));
		if (pageid <= 0) {
			return 1;
		}
		return pageid;
	}

	/**
	 * 打印header信息.
	 * 
	 * @param request
	 */
	public static void printHeaders(HttpServletRequest request) {
		Enumeration<String> e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String name = e.nextElement();
			String value = request.getHeader(name);
			logger.info(name + ":" + value);
		}
	}

	private static final java.util.regex.Pattern IS_LICIT_IP_PATTERN = java.util.regex.Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");

	/**
	 * 判断IP是否合法.
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isLicitIp(final String ip) {
		if (StringUtils.isEmpty(ip)) {
			return false;
		}
		Matcher m = IS_LICIT_IP_PATTERN.matcher(ip);
		return m.find();
		// return false;
		// }
		// return true;
	}

	/**
	 * 打印请求中的对象.
	 * 
	 * @param request
	 */
	public static void printAttributes(HttpServletRequest request) {
		Enumeration<String> e = request.getAttributeNames();
		while (e.hasMoreElements()) {
			String name = e.nextElement();
			Object value = request.getAttribute(name);
			logger.info(name + ":" + value);
		}
	}

	/**
	 * 从Session中获取帐号.
	 * 
	 * @param request
	 * @return
	 */
	public static String getSessionUsername(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		String username = (String) session.getAttribute(SessionKey.USERNAME_SESSION_NAME);
		return username;
	}

	/**
	 * 从Session中获取帐号.
	 * 
	 * @param session
	 * @return
	 */
	public static String getSessionUsername(HttpSession session) {
		String username = (String) session.getAttribute(SessionKey.USERNAME_SESSION_NAME);
		return username;
	}

	/**
	 * 获取上次访问的地址.
	 * 
	 * @param request
	 * @return
	 */
	public static String getReferer(HttpServletRequest request) {
		return request.getHeader("referer");
	}

	/**
	 * 获取请求参数的值，若不存在则返回默认值.
	 * 
	 * @param request
	 * @param parameterName
	 * @param defaultValue
	 * @return
	 */
	public static String getString(HttpServletRequest request, String parameterName, String defaultValue) {
		String value = request.getParameter(parameterName);
		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

	public static String getBasePath(HttpServletRequest request) {
		String path = request.getContextPath(); 
		String basePath = request.getScheme()+"://"+request.getServerName()+":"+ request.getServerPort()+path;
		return basePath;
	}
	
	/**
	 * 获取全路径
	 * @param request
	 * @return
	 */
	public static String getFullPath(HttpServletRequest request) {
		return getBasePath(request)+getRequestContextUri(request);
	}
}
