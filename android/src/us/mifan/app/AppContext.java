package us.mifan.app;

import java.util.Properties;
import java.util.UUID;

import us.mifan.app.AppConfig;
import us.mifan.app.api.ApiClient;
import net.thinkalways.util.StringUtils;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.util.Log;

/**
 * ȫ�ֲ�����
 *
 * @author xie.hj
 * @version 0.1
 *
 */
public class AppContext extends Application {
	
	private static String TAG = AppContext.class.getSimpleName();
	
	public static final int PAGE_SIZE = 20;	//��ҳ
	
	public static final String access_token = null;
	
	public void onCreate() {
		super.onCreate();
        //ע��App�쳣����������
        Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
        
	}
	
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try { 
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "������");
			e.printStackTrace(System.err);
		} 
		if(info == null) info = new PackageInfo();
		return info;
	}
	
	public String getAppId() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
		if(StringUtils.isEmpty(uniqueID)){
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}
	
	/**
	 * �û�ע��
	 */
	public void Logout() {
		ApiClient.cleanCookie();
		this.cleanCookie();
		
	}
	
	/**
	 * δ��¼���޸������Ĵ���
	 */
	public Handler getUnLoginHandler() {
		return null;
	}
	
	/**
	 * �������Ļ���
	 */
	public void cleanCookie()
	{
		removeProperty(AppConfig.CONF_COOKIE);
	}
	
	public boolean containsProperty(String key){
		Properties props = getProperties();
		return props.containsKey(key);
	}
	
	public Properties getProperties(){
		return AppConfig.getAppConfig(this).get();
	}
	
	public void setProperty(String key,String value){
		AppConfig.getAppConfig(this).set(key, value);
	}

	public String getProperty(String key){
		return AppConfig.getAppConfig(this).get(key);
	}
	
	public void removeProperty(String...key){
		AppConfig.getAppConfig(this).remove(key);
	}
	
}
