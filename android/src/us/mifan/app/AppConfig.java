package us.mifan.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import android.content.Context;

/**
 * Ӧ�ó���������
 *
 * @author xie.hj
 *
 */
public class AppConfig {
	private final static String APP_CONFIG = "config";	
	public final static String CONF_COOKIE = "cookie";

	public final static String CONF_APP_UNIQUEID = "APP_UNIQUEID";
	public final static String CONF_ACCESSTOKEN = "accessToken";
	
	private static AppConfig appConfig = null;

	private Context mContext;

	public static AppConfig getAppConfig(Context context) {
		if (appConfig == null) {
			appConfig = new AppConfig();
			appConfig.mContext = context;
		}
		return appConfig;
	}
	
	public void setAccessToken(String accessToken) {
		set(CONF_ACCESSTOKEN, accessToken);
	}	
	
	public void remove(String... key) {
		Properties props = get();
		for (String k : key)
			props.remove(k);
		setProps(props);
	}
	
	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try {
			// ��config����filesĿ¼��
			// fos = activity.openFileOutput(APP_CONFIG, Context.MODE_PRIVATE);
			// ��config����(�Զ���)app_config��Ŀ¼��
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File conf = new File(dirConf, APP_CONFIG);
			fos = new FileOutputStream(conf);

			p.store(fos, null);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}
	public String get(String key) {
		Properties props = get();
		return (props != null) ? props.getProperty(key) : null;
	}
	
	public Properties get() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			// ��ȡfilesĿ¼�µ�config
			// fis = activity.openFileInput(APP_CONFIG);

			// ��ȡapp_configĿ¼�µ�config
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			fis = new FileInputStream(dirConf.getPath() + File.separator
					+ APP_CONFIG);

			props.load(fis);
		} catch (Exception e) {
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return props;
	}

	public void set(Properties ps) {
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}

	public void set(String key, String value) {
		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	}
}
