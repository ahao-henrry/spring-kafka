package com.ahao.kafka.test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertyConfigUtil {
	private static final String SEP = ",";

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(PropertyConfigUtil.class);

	private static final String SEP2 = ":";

	private static Hashtable<String, PropertyConfigUtil> propertyConfigUtils = new Hashtable<String, PropertyConfigUtil>();

	private URL url;

	public String getPropertiesPath() {
		return url.getPath();
	}

	private PropertyConfigUtil(String propertiesPath) {
		logger.info("get propertiesPath="+propertiesPath);
		this.url = PropertyConfigUtil.class.getClassLoader().getResource(
				propertiesPath);
		logger.info("get url="+this.url);

	}

	public static PropertyConfigUtil getInstance(String propertiesPath) {
		PropertyConfigUtil configUtil = (PropertyConfigUtil) propertyConfigUtils
				.get(propertiesPath);
		if (configUtil == null) {
			configUtil = new PropertyConfigUtil(propertiesPath);
			propertyConfigUtils.put(propertiesPath, configUtil);
		}
		return configUtil;
	}

	public int getIntValue(String key) {
		return Integer.parseInt(getValue(key));
	}

	public long getLongValue(String key) {
		return Long.parseLong(getValue(key));
	}

	public synchronized String getValue(String key) {
		Properties properties = new Properties();
		InputStream inputStream = null;
		try {
			logger.debug("load the proerties :" + this.url);
			inputStream = url.openStream();
			properties.load(inputStream);
			
			check(key, properties);
			String value = properties.getProperty(key);
			logger.debug("getValue from proerties:" + url
					+ ":" + key + "=" + value);
			return value;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("getValue(String)resourse:" + this.url,
					e);
		} catch (IOException e) {
			throw new RuntimeException("getValue(String)", e);
		} finally {
			try {
				if (inputStream == null) {
					logger.error("can not get resourse:" + this.url);
				} else {
					inputStream.close();
				}
			} catch (IOException e) {
				logger.error("getValue(String)", e);
			}
		}
	}

	protected boolean check(String key, Properties properties) {
		if (properties.containsKey(key)) {
			return true;
		} else {
			throw new RuntimeException("the property file[" + this.url
					+ "] do not have the key:" + key);
		}
	}

	public synchronized void setValue(String key, String value) {
		Properties properties = new Properties();
		OutputStream os = null;
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = this.url.openStream();
			properties.load(resourceAsStream);
			properties.put(key, value);
			String fileAbPath = url.getFile();
			os = new FileOutputStream(new File(fileAbPath));
			properties.store(os, this.url.getFile());
		} catch (IOException e) {
			throw new RuntimeException(
					"setValue(String key, String value, String fileAbPath)resourse:"
							+ this.url, e);
		} finally {
			try {
				resourceAsStream.close();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.debug("set value to proerties:" + this.url
				+ " " + key + "=" + value);
	}

	public boolean getBoolean(String key) {
		return new Boolean(this.getValue(key)).booleanValue();
	}

	public String[] getArray(String key) {
		return this.getValue(key).split(SEP);
	}

	public Map<String, String> getMap(String key) {
		String[] strings = this.getArray(key);
		HashMap<String, String> hashMap = new HashMap<String, String>();
		for (int i = 0; i < strings.length; i++) {
			String[] att_value = strings[i].split(SEP2);
			if (att_value.length != 2) {
				throw new IllegalArgumentException("\"" + strings[i]
						+ "\"config error!");
			} else {
				hashMap.put(att_value[0], att_value[1]);
			}
		}
		return hashMap;
	}
}
