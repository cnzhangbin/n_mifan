package us.mifan.app.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

import android.util.Log;
import us.mifan.app.AppContext;
import us.mifan.app.AppException;
import us.mifan.app.bean.URLs;

/**
 * API�ͻ��˽ӿڣ����ڷ�����������
 *
 * @author xie.hj
 *
 */
public class ApiClient {
	private static final String TAG = ApiClient.class.getSimpleName();
	private static final String ENCODE = "UTF-8";	//�ַ���
	
	private final static int TIMEOUT_CONNECTION = 20000;	//���ӳ�ʱʱ��
	private final static int TIMEOUT_SOCKET = 20000;	//�����ݳ�ʱʱ��
	private final static int RETRY_TIME = 3;
	
	private static String appCookie;
	private static String appUserAgent;
	
	public static void cleanCookie() {
		appCookie = "";
	}

	private static String getCookie(AppContext appContext) {
		if(appCookie == null || appCookie == "") {
			appCookie = appContext.getProperty("cookie");
		}
		return appCookie;
	}
	
	private static String getUserAgent(AppContext appContext) {
		if(appUserAgent == null || appUserAgent == "") {
			StringBuilder ua = new StringBuilder("mifan.us");
			ua.append('/'+appContext.getPackageInfo().versionName+'_'+appContext.getPackageInfo().versionCode);//App�汾
			ua.append("/Android");//�ֻ�ϵͳƽ̨
			ua.append("/"+android.os.Build.VERSION.RELEASE);//�ֻ�ϵͳ�汾
			ua.append("/"+android.os.Build.MODEL); //�ֻ��ͺ�
			ua.append("/"+appContext.getAppId());//�ͻ���Ψһ��ʶ
			appUserAgent = ua.toString();
		}
		return appUserAgent;
	}
	
	private static HttpClient getHttpClient() {
        HttpClient httpClient = new HttpClient();
		// ���� HttpClient ���� Cookie,���������һ���Ĳ���
		httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        // ���� Ĭ�ϵĳ�ʱ���Դ������
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT_CONNECTION);
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpClient.getParams().setContentCharset(ENCODE);
		
		
		return httpClient;
	}
	
	private static GetMethod getHttpGet(String url, String cookie, String userAgent) {
		GetMethod httpGet = new GetMethod(url);
		// ���� ����ʱʱ��
		httpGet.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpGet.setRequestHeader("Host", URLs.HOST);
		httpGet.setRequestHeader("Connection","Keep-Alive");
		httpGet.setRequestHeader("Cookie", cookie);
		httpGet.setRequestHeader("User-Agent", userAgent);
		return httpGet;
	}
	
	private static PostMethod getHttpPost(String url, String cookie, String userAgent) {
		PostMethod httpPost = new PostMethod(url);
		// ���� ����ʱʱ��
		httpPost.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpPost.setRequestHeader("Host", URLs.HOST);
		httpPost.setRequestHeader("Connection","Keep-Alive");
		httpPost.setRequestHeader("Cookie", cookie);
		httpPost.setRequestHeader("User-Agent", userAgent);
		return httpPost;
	}
	
	private static String _MakeURL(String p_url, Map<String, Object> params) {
		StringBuilder url = new StringBuilder(p_url);
		if(url.indexOf("?")<0)
			url.append('?');

		for(String name : params.keySet()){
			url.append('&');
			url.append(name);
			url.append('=');
			url.append(String.valueOf(params.get(name)));
			//����URLEncoder����
			//url.append(URLEncoder.encode(String.valueOf(params.get(name)), UTF_8));
		}

		return url.toString().replace("?&", "?");
	}
	
	public static InputStream http_get(AppContext appContext, String url) throws AppException{
		
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);
		
		HttpClient httpClient = null;
		GetMethod httpGet = null;

		InputStream responseBody = null;
		int time = 0;
		
		do{				
			try {
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, cookie, userAgent);		
				int statusCode = httpClient.executeMethod(httpGet);
				
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
				responseBody = httpGet.getResponseBodyAsStream();
				break;
			} catch (HttpException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// �����������쳣��������Э�鲻�Ի��߷��ص�����������,��������
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// ���������쳣
				e.printStackTrace();
				throw AppException.network(e);
			}finally{
				//httpGet.releaseConnection();
				//httpClient = null;
				//((MultiThreadedHttpConnectionManager) httpClient.getHttpConnectionManager()).shutdown();
				
			}			
			
		}while(time<RETRY_TIME);
		
		return responseBody;
		
	}
	
	public static InputStream http_post(AppContext appContext,String url,Map<String, Object> params,Map<String,File> files) throws AppException{
		
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);
		
		HttpClient httpClient = null;
		PostMethod httpPost = null;
		InputStream responseBody = null;
		int time = 0;
		int i = 0;
		
		//��������
		int length = (params == null ? 0 : params.size()) + (files == null ? 0 : files.size());
		Part[] parts = new Part[length];
		if(null != params){
			for(String name : params.keySet()){
	        	parts[i++] = new StringPart(name, String.valueOf(params.get(name)), ENCODE);
	        }
		}
		
		if(null!=files){
			for(String file : files.keySet()){
				try {
					parts[i++] = new FilePart(file, files.get(file));
				} catch (FileNotFoundException e) {
					Log.e(TAG, "http_post==>�ļ�������!");
					e.printStackTrace();
				}
			}
		}
		
		do{			
			try {
				httpClient = getHttpClient();
				httpPost = getHttpPost(url, cookie, userAgent);
				httpPost.setRequestEntity(new MultipartRequestEntity(parts,httpPost.getParams()));
				int statusCode = httpClient.executeMethod(httpPost);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
				
				responseBody = httpPost.getResponseBodyAsStream();
				break;
			} catch (HttpException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// �����������쳣��������Э�鲻�Ի��߷��ص�����������,��������
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// ���������쳣
				e.printStackTrace();
				throw AppException.network(e);
			}
			
			
		}while(time<RETRY_TIME);
		
		
		return responseBody;
		
	}
	
}
