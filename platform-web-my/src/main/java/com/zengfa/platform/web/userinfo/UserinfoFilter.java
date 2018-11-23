package com.zengfa.platform.web.userinfo;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zengfa.platform.util.security.UserInfo;


public class UserinfoFilter implements Filter {
	private final Log logger = LogFactory.getLog(this.getClass());

	@Resource
	private LoginHandler loginHandler;
	private UserinfoService userinfoService = new UserinfoServiceImpl();

//	@Autowired(required = false)
//	public void setDistributeSession(DistributeSession distributeSession) {
//		logger.info("session:" + distributeSession);
//		SessionService.distributeSession = distributeSession;
//	}

	@PostConstruct
	public void init() {
		userinfoService.setLoginHandler(loginHandler);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
//		MonitorContext.setRequest(request);// 按入口进行性能监控
		this.doFilter2(req, res, chain);
	}

	protected void doFilter2(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
//		UserinfoWrapper httpRequestWraper = new UserinfoWrapper(request, response);

		if (!checkLogin(request, response)) {
			return;
		}

//		Object account = httpRequestWraper.getSession().getAttribute("account");
//		if (account != null) {
//			ConnectionLimitInterceptor.setAccount(request, account);
//		}
		chain.doFilter(request, response);
	}

	protected boolean checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/*String uri = request.getRequestURI();
		String str= request.getQueryString();*/
		boolean isExcludeUri = userinfoService.isExcludeUri(request);
		if (isExcludeUri) {
			return true;
		}

		return this.doLogin(request, response);
	}

	protected boolean doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 
		  String reqSource = request.getParameter("reqSource");//请求来源、手机端，大屏

		  if(StringUtils.isBlank(reqSource)){//判断移动还是web端请求，reqSource为空为移动端
			  return webSessionFilter(request, response);
		  }else{
			  return appSessionFilter(request, response,reqSource);
		  }
	}
	/**
	 * 移动端用户登陆验证
	 * @author ds <br>
	 * @Date 2016年5月19日<br>
	 * @param request
	 * @param response
	 * @return
	 */
	
	private boolean appSessionFilter(HttpServletRequest request, HttpServletResponse response,String reqSource){
				String 	 cookie = LoginUtil.checkLoginByCookie(request, SessionKey.ZHC_COOKIE_USER_NAME);//web端token
				if(StringUtils.isNotBlank(cookie)){//
					webSessionFilter(request, response);
				}
				String 	  token = request.getParameter("token");//屏端token
				// token为空，表示没有登录过登录中心，需要跳转
				if (StringUtils.isBlank(token)) {
					// 跳转到登陆页面
					response.setHeader("Cache-Control", "no-store");
					response.setDateHeader("Expires", 0);
					response.setHeader("Prama", "no-cache");
					//未登录跳转
					unauthorizedApp(request, response);
					return false;
				} else {
					// 此处验证token，验证失败，不能跳转到登录中心，否则会引起循环访问
//					UserInfo user = (UserInfo) SerializeUtil.unserialize(RedisSlave.getInstance().get(token));
//					if (user != null && user.getUserId()>0) {//用户验证
//						if("mobile_b2b".equals(user.getReqSource())&&user.isSignOut()){
//							RedisSlave.getInstance().del(token);
//							loginAuthorized(request, response);
//							return false;
//						}
//						RedisSlave.getInstance().expire(token, SessionKey.APP_TOKEN_EXPIRE_TIME);
//						return true;
//					}else{
//						//未登录未授权跳转
//						unauthorizedApp(request, response);
//						return false;
//					}
				}
				return true;
	}
	
	/**
	 * web用户登陆验证
	 * @author ds <br>
	 * @Date 2016年5月19日<br>
	 * @param request
	 * @param response
	 * @return
	 */
	
	private boolean webSessionFilter(HttpServletRequest request, HttpServletResponse response){
		// 如果服务端会话过期，那么从cookie获取用户的token进行验证
			String 	 token = LoginUtil.checkLoginByCookie(request, SessionKey.ZHC_COOKIE_USER_NAME);//web端token
				// token为空，表示没有登录过登录中心，需要跳转
				if (StringUtils.isBlank(token)) {
					// 跳转到登陆页面
					response.setHeader("Cache-Control", "no-store");
					response.setDateHeader("Expires", 0);
					response.setHeader("Prama", "no-cache");
					//未登录未授权跳转
					unauthorizedloginOut(request, response);
					return false;
				} else {
					// 此处验证token，验证失败，不能跳转到登录中心，否则会引起循环访问
					UserInfo user = (UserInfo) request.getSession().getAttribute(token);
//					UserInfo user = (UserInfo) SerializeUtil.unserialize(RedisSlave.getInstance().get(token));
					if (user != null && user.getUserId()>0) {//用户验证
						LoginUtil.saveUserInfo(user, token, request, response);
//						RedisSlave.getInstance().expire(token, SessionKey.COOKIE_EXPIRATIOIN_TIME);
						//权限过滤
						String uri = request.getRequestURI();
						if(uri.endsWith(".do")||uri.endsWith(".html")){
							boolean flag=false;
							//获取系统配置的权限
//							List<UserResMenu> allList =(List<UserResMenu>) SerializeUtil.unserialize(RedisSlave.getInstance().get(SessionKey.REDIS_KEY_ALL_MUEN_VALUE+user.getUserId()));
//							if(allList==null){
//								//未登录未授权跳转
//								unauthorizedloginOut(request, response);
//								return false;
//							}else{
//								RedisSlave.getInstance().expire(SessionKey.REDIS_KEY_ALL_MUEN_VALUE+user.getUserId(),SessionKey.REDIS_KEY_BASE_TIME);
//							}
//							for (UserResMenu menu : allList) {//判断url是否在系统设置的权限中
//								String menuPath = menu.getMenulink();
//									if(uri.equals(menuPath)){
//											 flag=true;
//											 break;
//										}
//							}
//							if(flag){//url在系统设置的权限中需验证角色权限
//								List<UserResMenu> resourcesMenu =user.getResourcesMenu(); 
//								for (UserResMenu menu : resourcesMenu) {
//									String menuPath = menu.getMenulink();
//									if(menu.getLevel()==2){//菜单权限的需要带参数判断权限，功能权限无需带参数
//										//html带参数验证权限
//										String  uriparam=uri;
//										if(StringUtils.isNotBlank(request.getQueryString())&&uri.endsWith(".html")){
//											uriparam=uri+"?"+request.getQueryString();
//										}
//										if(menuPath.equals(uriparam)){//html带参数验证
//											return true;
//										}
//									}else{
//										if(menuPath.equals(uri)){//html不带参数验证
//											return true;
//										}
//									}
//								}
//							}else{//url未在系统设置的权限中，直接通过
//								return true;
//							}
							//登录没有权限
//							unauthorized(request, response);
//							return false; 
						}
						return true;
					}else{
						//未登录未授权跳转
						unauthorizedloginOut(request, response);
						return false; 
					}
				}
	}
	
	/**
	 * 未授权访问跳转（未登录）.
	 * 
	 * @param request
	 * @param response
	 */
	private void unauthorizedloginOut(HttpServletRequest request, HttpServletResponse response) {
		RequestDispatcher rd = request.getRequestDispatcher("/platform/unauthorizedloginOut.do");
		try {
			rd.forward(request, response);
		} catch (Exception e) {
			logger.error("登录未授权跳转异常.", e);
		}
	}

	/**
	 * 未授权访问（已登录）.
	 * 
	 * @param request
	 * @param response
	 */
	private void unauthorized(HttpServletRequest request, HttpServletResponse response) {
		RequestDispatcher rd = request.getRequestDispatcher("/platform/unauthorized.do");
		try {
			rd.forward(request, response);
		} catch (Exception e) {
			logger.error("未授权跳转异常.", e);
		}
	}
	/**
	 * 未授权访问（app未登录）.
	 * 
	 * @param request
	 * @param response
	 */
	private void unauthorizedApp(HttpServletRequest request, HttpServletResponse response) {
		RequestDispatcher rd = request.getRequestDispatcher("/platform/unauthorizedApp.do");
		try {
			rd.forward(request, response);
		} catch (Exception e) {
			logger.error("app未授权异常.", e);
		}
	}
	/**
	 * 已被强制退出
	 * 
	 * @param request
	 * @param response
	 */
	private void loginAuthorized(HttpServletRequest request, HttpServletResponse response) {
		RequestDispatcher rd = request.getRequestDispatcher("/platform/loginAuthorized.do");
		try {
			rd.forward(request, response);
		} catch (Exception e) {
			logger.error("app未授权异常.", e);
		}
	}
	@Override
	public void destroy() {
		logger.info("destroy");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("init");
	}
}
