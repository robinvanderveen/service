package com.rabobank.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import com.rabobank.config.Config;
import com.rabobank.model.Publication;

/*
 * Lowlevel DAO layer to communicate with datbase..
 * 
 */
public class DataHubDAO {
	static Logger logger = Logger.getLogger(DataHubDAO.class.getName());
	
/*	
	 * create table PUBLICATION_CONTROL_TABLE(
	          ID int IDENTITY(1,1) primary key not null,
	          PUBLICATIONNAME varchar(100) not null,
	          DELIVERY_SQN int,
	          PUBLICATIONTABLE varchar(100) not NULL,
	          PUBLICATIONDATE date,
	          source varchar(100) NOT NULL,
	          NROFRECORDs  int,
	          DELIVERY_STARTTIME_SOURCE datetime,
	          DELIVERY_ENDTIME_SOURCE datetime,
	          TRIGGER_TIME datetime,
	          STATUS varchar(20))
	          
		); */
	public final static String STATUS_NEW="NEW"; 
	public final static String STATUS_SEND="SEND";
	public final static String STATUS_RECEIVED="RECEIVED";
	
	
	private static DataHubDAO thisObject = new DataHubDAO();

	private static String query_by_Status="select Status, Id, PublicationName, Delivery_SQN, PublicationTable, PublicationDate, NROFRECORDS from %SCHEMA%PUBLICATION_CONTROL_TABLE where Status= ?";
	private static String update_Publication="update %SCHEMA%PUBLICATION_CONTROL_TABLE set Status =?, Trigger_Time=? where ID = ?"; 
//	private static String insert_Publication="insert into PUBLICATION_CONTROL_TABLE(PublicationName,PublicationTable,PublicationDate,Delivery_SQN,SOURCE,NROFRECORDS,DELIVERY_STARTTIME_SOURCE,DELIVERY_ENDTIME_SOURCE,Trigger_Time,STATUS) VALUES(?,?,?,?,?,?,?,?,?,?)"; 

	private Connection conn = null;
	private Config config = null;
	static boolean initialized = false;
    
	/*
	 * 
	 * create private constructor.. 
	 * Use getInstance to retrieve this object.
	 */
	private DataHubDAO()
	{
		
	}
	
	
	public void initialize(Config configuration)
	{
		if (! initialized)
		{	
			thisObject.config = configuration;
			if (config != null && config.getDatabase() != null && config.getDatabase().getDriver() != null)
			{	
				try {
					Class.forName(config.getDatabase().getDriver());
				} catch (ClassNotFoundException e) {
					logger.error("Error loading the driver. Error : " + e.getMessage());
				}
			} else
			{
				logger.warn("No database driver found to load driver..");
			}
			
		}	
		initialized = true;

	}
	
	public static  DataHubDAO getInstance()
	{
		return thisObject;
	}

	/*
	 * update the Publication information.
	 * 
	 */
	public void updatePublication (String status, int iD)
	{
		// if there is no status..	
	    if (status == null)
	    {
	    	return;
	    }
	    
	    // check for a proper database connection..
		try {
			if (thisObject.conn == null || thisObject.conn.isClosed())
			{
				createConnection();
			}
		} catch (Exception e)
		{
			createConnection();
		}
		
		if (isConnected())
		{	
			int  result = 0;
			// query the database to check if the meta file was already processed.. 
			PreparedStatement statement = null;
			logger.info(" Updating status of publication "+ iD);
			try {
				if (thisObject.conn != null)
				{	
		           statement = thisObject.conn.prepareStatement(update_Publication.replace("%SCHEMA%", config.getDatabase().getSchemaname()));
	
		           statement.setString(1, status);
		           statement.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime())); 
		           statement.setInt(3, iD);
		           result = statement.executeUpdate();
			     }
			 }
			 catch (Exception e)
			 {
				 	logger.error(" Error updating status table. error: "+e.getMessage() );
				    return;
			 } finally {
	
				    try {
					     statement.close();
				     } catch (Exception e)
				    {}
			  }
		} else
		{
			logger.error("Failed to update status.. No db connection possible");
		}
		return;
	}

	/*
	 * insert the Publication information.
	 * Using query;
	 * 	private static String insert_Publication="insert into PUBLICATION_CONTROL_TABLE(PublicationName,PublicationTable,PublicationDate,Delivery_SQN,SOURCE,NROFRECORDS,DELIVERY_STARTTIME_SOURCE,DELIVERY_ENDTIME_SOURCE,Trigger_Time,STATUS) VALUES(?,?,?,?,?,?,?,?,?,?)"; 

	 */
	public void insertPublication (String status, int iD)
	{
		// if there is no filename..	
	    if (status == null)
	    {
	    	return;
	    }
	    
	    // check for a proper database connection..
		try {
			if (thisObject.conn == null || thisObject.conn.isClosed())
			{
				createConnection();
			}
		} catch (Exception e)
		{
			createConnection();
		}

		int  result = 0;
		// query the database to check if the meta file was already processed.. 
		PreparedStatement statement = null;
		try {
			if (thisObject.conn != null)
			{	
	           statement = thisObject.conn.prepareStatement(update_Publication);

	           statement.setString(1, status);
	           statement.setTime(2, new java.sql.Time(new java.util.Date().getTime())); 
	           statement.setInt(3, iD);
	           result = statement.executeUpdate();
		     }
		 }
		 catch (Exception e)
		 {
			 	logger.error(" Error updating status table. error: "+e.getMessage() );
			    return;
		 } finally {

			    try {
				     statement.close();
			     } catch (Exception e)
			    {}
		  }
		  return;
	}

	/*
	 * 
	 * check if there is a database connection.
	 */
		
	
	private boolean isConnected()
	{
		try {
			return (thisObject.conn != null && !thisObject.conn.isClosed());
		} catch (Exception e)
		{}
		return false;
	}
	/*
	 * 
	 * Query database to find publications of a particular status.
	 */
	public List<Publication> queryDatabase(String status)
	{
		// if there is no status..	
	    if (status == null)
	    {
	    	return null;
	    }
	    
	    // check for a proper database connection..
		try {
			if (thisObject.conn == null || thisObject.conn.isClosed())
			{
				createConnection();
			}
		} catch (Exception e)
		{
			createConnection();
		}
		if  (isConnected())
		{	
				ResultSet result = null;
				// query the database to check if the meta file was already processed.. 
				PreparedStatement statement = null;
				List<Publication> results = new ArrayList<Publication>();
				logger.info("Querying database for publications with status : " + status);
				try {
					if (thisObject.conn != null)
					{	
			           statement = thisObject.conn.prepareStatement(query_by_Status.replace("%SCHEMA%", config.getDatabase().getSchemaname()));
		
			           statement.setString(1, status);
			           if (logger.isDebugEnabled())
			           {	   
			        	   logger.debug("Executing query : "+query_by_Status.replace("%SCHEMA%", config.getDatabase().getSchemaname()));
			           }	   
			           result = statement.executeQuery();
			           while (result.next())
			           {
			        	   Publication pub= convertToPublication(result);
			        	   results.add(pub);
			           } 
			           return results;
				     }
				 }
				 catch (Exception e)
				 {
					 logger.error(" Error quering status table. error:"+e.getMessage() );
					 return null;
				 } finally {
					    try {
						   result.close();
					    } catch (Exception e)
				  	    {}
		
					    try {
						     statement.close();
					     } catch (Exception e)
					    {}
				  }
		  } else
		  {
					logger.error("Failed to query for statusses.. No db connection possible");
		  }
		  return null;
	}
	
	/*
	 * 
	 * convert a resultset to a Publication Object
	 */
	private Publication convertToPublication(ResultSet result)
	{
		Publication pub = new Publication();
		try {
			pub.setId(result.getInt("ID"));
			pub.setNfofrecords(result.getInt("NROFRECORDS"));
			pub.setPublicatedate(result.getDate("PUBLICATIONDATE"));
			pub.setPublicatename(result.getString("PUBLiCATIONNAME"));
			pub.setPublicationtable(result.getString("PUBLICATIONTABLE"));
			pub.setSequence_id(result.getInt("DELIVERY_SQN"));
			pub.setStatus(result.getString("sTATUS"));
		} catch (Exception e)
		{
			Logger.getLogger(" DAO ").error("Error converting database record to object.."+e.getMessage());
		}
		return pub;
	}	
	/*
	 * 
	 * Create database Connection..
	 */
	public synchronized void createConnection()
	{
		
		if (conn != null)
		{
			try 
			{
				conn.close();
			
			} catch (Exception e)
			{}
			conn = null;
		}
		if (config.getDatabase() == null)
		{
			logger.warn("No database configuration made.. Can't create a connection.");
			return;
		}
		initialize(config);
		logger.info("Creating db connection..");
		 Properties props = new Properties(); 
		 props.put("user", config.getDatabase().getUserid()); 
		 props.put("password", config.getDatabase().getPassword()); 
		 try {
			 conn = DriverManager.getConnection (config.getDatabase().getUrl(), props);
			 if (conn != null)
			 {	 
				 conn.setAutoCommit(true);
			 }	 
			 logger.info("Creating db connection was succesful.");
		 } catch (Exception e)	 
		 {
			 logger.error(" Error connecting to database.. error : " + e.getMessage(),e);
		 }
	}
	
	/*
	 * 
	 * Create database Connection..
	 */
	public void closeConnection()
	{

		try {
			if (thisObject.conn != null)
			{
				thisObject.conn.close();
				
			}
		} catch (Exception e) {}
		thisObject.conn = null;
	}
	}
