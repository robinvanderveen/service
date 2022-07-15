package com.rabobank.config;

/*
 * 
 * config for the database connection..
 * 
 * URL/driver/Userid/schemaname/password.
 * 
 * Password can optional be encrypted.
 * 
 */
import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;

import com.rabo.encryption.AESEncryption;
import com.rabobank.dao.DataHubDAO;

public class Database {

	static Logger logger = Logger.getLogger(Database.class.getName());

	@Element 
	private String url;
	
	@Element 
	private String driver;

	@Element 
	private String userid;

	@Element(required=false) 
	private String schemaname;

	@Element 
	private String password;

	@Element(required=false)
	private boolean deleteData=false;


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPassword() {
		if (password != null && password.startsWith("{") && password.endsWith("}"))
		{
			AESEncryption aes = new AESEncryption();
			try {
				String decrypted = aes.decrypt(password);
				if (decrypted == null)
				{	
					logger.fatal("Error decrypting the database password. Check your config file. Used encrypted string:" +password);
					return password;
				} 
				return decrypted;
			} catch (Exception e)
			{
				logger.fatal("Error decrypting the database password. Check your config file. Used encrypted string:" +password);
				logger.fatal(e.getMessage());
				
			}
		}
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public boolean isDeleteData() {
		return deleteData;
	}

	public void setDeleteData(boolean deleteData) {
		this.deleteData = deleteData;
	}

	public String getSchemaname() {
		if (schemaname == null)
		{
			return "";
		}
		return schemaname;
	}

	public void setSchemaname(String schemaname) {
		this.schemaname = schemaname;
	}

	
	

}
