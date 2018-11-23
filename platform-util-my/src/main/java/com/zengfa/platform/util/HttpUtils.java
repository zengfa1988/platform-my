package com.zengfa.platform.util;

import java.io.File;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;


/**
 * 
 * @author cwli
 *
 */
public class HttpUtils {
	private static Logger logger = Logger.getLogger(HttpUtils.class);
	private static int MAX_PER_ROUTE = 10;
	private static int MAX_TOTAL = 100;
	private static int SO_TIMEOUT = 50000;
	private static int CONNECTION_TIMETOU = 50000;
	private static HttpClient httpClient = null;
	private static String CHARSET = "UTF-8";
	private static String UA = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";;
	static {
		httpClient = getThreadSafeHttpClient(MAX_PER_ROUTE, MAX_TOTAL, SO_TIMEOUT, CONNECTION_TIMETOU);
	}

	public static HttpClient getThreadSafeHttpClient(int maxPerRoute, int maxTotal, int soTimeout,
			int connectionTimeout) {
		// 初始化一个httpClient
		DefaultHttpClient httpClient = null;
		try {
			// 线程安全
			PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
			cm.setDefaultMaxPerRoute(maxPerRoute);
			cm.setMaxTotal(maxTotal);
			httpClient = new DefaultHttpClient(cm);
			httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, soTimeout);
			httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, connectionTimeout);
			httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
			// 保持cookie,带到下面的请求
			HttpClientParams.setCookiePolicy(httpClient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
			// httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,
			// Boolean.TRUE);
			// HttpClientParams.setRedirecting(httpClient.getParams(), true);
			// post自动处理302
			httpClient.setRedirectStrategy(new LaxRedirectStrategy());

			/*
			 * httpClient.setRedirectHandler(new DefaultRedirectHandler() {
			 * 
			 * @Override public boolean isRedirectRequested(HttpResponse
			 * response, HttpContext context) { boolean isRedirect =
			 * super.isRedirectRequested(response, context); if (!isRedirect) {
			 * int responseCode = response.getStatusLine().getStatusCode(); if
			 * (responseCode == 301 || responseCode == 302) { return true; } }
			 * return isRedirect; } });
			 */

			// https不加载证书
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub

				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub

				}

				public X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return null;
				}
			} }, null);
			SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme sch = new Scheme("https", 443, sf);
			Scheme sch2 = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
			// Scheme sch3 = new Scheme("http", 82,
			// PlainSocketFactory.getSocketFactory());
			httpClient.getConnectionManager().getSchemeRegistry().register(sch);
			httpClient.getConnectionManager().getSchemeRegistry().register(sch2);
			// httpClient.getConnectionManager().getSchemeRegistry().register(sch3);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return httpClient;
	}

	public static String getPostContent(String url, Map<String, String> formEntity, String charset) {
		try {
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entity : formEntity.entrySet()) {// account
																			// and
																			// password
				nvps.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
			}
			// 添加头部信息
			httpPost.setHeader("User-Agent", "Http_Client_4.2");

			httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
			HttpResponse response = httpClient.execute(httpPost);
			String content = null;
			if (response.getEntity() != null) {
				charset = EntityUtils.getContentCharSet(response.getEntity()) == null ? charset
						: EntityUtils.getContentCharSet(response.getEntity());
				content = new String(EntityUtils.toByteArray(response.getEntity()), charset);
			}
			httpPost.abort();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}

	}

	public static String getPostContent(String url, Map<String, String> headerEntity, String dataStr, String charset) {
		try {
			HttpPost httpPost = new HttpPost(url);
			// 添加header
			for (Map.Entry<String, String> entity : headerEntity.entrySet()) {
				httpPost.addHeader(entity.getKey(), entity.getValue());
			}
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
			httpPost.setEntity(new StringEntity(dataStr, Charset.forName(charset)));
			HttpResponse response = httpClient.execute(httpPost);
			String content = null;
			if (response.getEntity() != null) {
				charset = EntityUtils.getContentCharSet(response.getEntity()) == null ? charset
						: EntityUtils.getContentCharSet(response.getEntity());
				content = new String(EntityUtils.toByteArray(response.getEntity()), charset);
			}
			httpPost.abort();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static byte[] getPostStream(String url, Map<String, String> formEntity, String charset) {
		try {
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entity : formEntity.entrySet()) {// account
																			// and
																			// password
				nvps.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
			}
			// 添加头部信息
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");

			httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
			HttpResponse response = httpClient.execute(httpPost);
			byte[] content = null;
			if (response.getEntity() != null) {
				content = EntityUtils.toByteArray(response.getEntity());
			}
			httpPost.abort();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static byte[] getMultiPartPostStream(String url, Map<String, Object> formEntity, String charset) {
		try {
			HttpPost httpPost = new HttpPost(url);
			MultipartEntity reqEntity = new MultipartEntity();

			for (Map.Entry<String, Object> entity : formEntity.entrySet()) {// account
																			// and
																			// password
				if (entity.getValue() instanceof File) {
					reqEntity.addPart(entity.getKey(), new FileBody((File) entity.getValue()));
				} else {
					reqEntity.addPart(entity.getKey(),
							new StringBody(entity.getValue().toString(), Charset.forName(CHARSET)));
				}
			}
			// 添加头部信息
			httpPost.setHeader("User-Agent", "Http_Client_4.2");

			httpPost.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(httpPost);
			byte[] content = null;
			if (response.getEntity() != null) {
				content = EntityUtils.toByteArray(response.getEntity());
			}
			httpPost.abort();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static String getPostContent(String url, byte[] data, String charset) {
		try {
			HttpPost httpPost = new HttpPost(url);
			// 添加头部信息
			httpPost.setHeader("User-Agent", "Http_Client_4.2");

			httpPost.setEntity(new ByteArrayEntity(data));
			HttpResponse response = httpClient.execute(httpPost);
			String content = null;
			if (response.getEntity() != null) {
				charset = EntityUtils.getContentCharSet(response.getEntity()) == null ? charset
						: EntityUtils.getContentCharSet(response.getEntity());
				content = new String(EntityUtils.toByteArray(response.getEntity()), charset);
			}
			httpPost.abort();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static String getContent(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			// 添加头部信息
			httpGet.setHeader("User-Agent", UA);

			HttpResponse response = httpClient.execute(httpGet);
			String content = null;
			if (response.getEntity() != null) {
				CHARSET = EntityUtils.getContentCharSet(response.getEntity()) == null ? CHARSET
						: EntityUtils.getContentCharSet(response.getEntity());
				content = new String(EntityUtils.toByteArray(response.getEntity()), CHARSET);
			}
			httpGet.abort();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static String postContent(String url,  Map<String, Object> formEntity) {
		try {
			HttpPost httpPost = new HttpPost(url);
			// 添加头部信息
			httpPost.setHeader("User-Agent", UA);
			MultipartEntity reqEntity = new MultipartEntity();
			for (Map.Entry<String, Object> entity : formEntity.entrySet()) {
				if (entity.getValue() instanceof File) {
					reqEntity.addPart(entity.getKey(), new FileBody((File) entity.getValue()));
				} else {
					reqEntity.addPart(entity.getKey(),
							new StringBody(entity.getValue().toString(), Charset.forName(CHARSET)));
				}
			}
			httpPost.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(httpPost);
			String content = null;
			if (response.getEntity() != null) {
				CHARSET = EntityUtils.getContentCharSet(response.getEntity()) == null ? CHARSET
						: EntityUtils.getContentCharSet(response.getEntity());
				content = new String(EntityUtils.toByteArray(response.getEntity()), CHARSET);
			}
			httpPost.abort();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	public static String postContent(String url,  Map<String, Object> formEntity,int soTimeout,int connectionTimeout) {
		try {
			HttpPost httpPost = new HttpPost(url);
			// 添加头部信息
			httpPost.setHeader("User-Agent", UA);
			MultipartEntity reqEntity = new MultipartEntity();
			for (Map.Entry<String, Object> entity : formEntity.entrySet()) {
				if (entity.getValue() instanceof File) {
					reqEntity.addPart(entity.getKey(), new FileBody((File) entity.getValue()));
				} else {
					reqEntity.addPart(entity.getKey(),
							new StringBody(entity.getValue().toString(), Charset.forName(CHARSET)));
				}
			}
			httpPost.setEntity(reqEntity);
			httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, soTimeout);
			httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, connectionTimeout);
			HttpResponse response = httpClient.execute(httpPost);
			String content = null;
			if (response.getEntity() != null) {
				CHARSET = EntityUtils.getContentCharSet(response.getEntity()) == null ? CHARSET
						: EntityUtils.getContentCharSet(response.getEntity());
				content = new String(EntityUtils.toByteArray(response.getEntity()), CHARSET);
			}
			httpPost.abort();
			return content;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static void main(String[] args) {
		String url = "https://www.kuaidi100.com/query?type=ups&postid=1ZA5136W0471401747";
		System.out.println(url);
		String result = HttpUtils.getContent(url);
		System.out.println("===get====result:" + result);
	}

}
