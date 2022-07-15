package com.rabobank.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.rabobank.config.Config;
import com.rabobank.dao.DataHubDAO;
import com.rabobank.model.Publication;

import java.io.BufferedReader;

public class Datahub {

	static Logger logger = Logger.getLogger(Datahub.class.getName());
	static Config config;
	static String filename_pattern="__yyyyMMdd";
	static String filename_pattern2="_yyyyMMddHHmmss";
	static String filename_pattern3="YYMM";	// Used for Universal command (PROD)
	
	/**
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		System.out.println("Starting Datahub scanner.. ");
		if (args.length < 2)
		{
			System.out.println("Usage: configuration-file logging-file");
			System.exit(0);
		}
	     // BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure(args[1]);
	 	 logger.info("Starting up Datahub monitor 1.0/2022");
		 Datahub hub = new Datahub();
		 hub.monitorcontroltable(args[0]);
		
	}
	
	/*
	 * 
	 * Monitor Control table...
	 * 
	 */
	public void monitorcontroltable(String configFile)
	{
		Serializer serializer = new Persister();
		boolean runforever = false;
		try {
			config = serializer.read(Config.class, new FileInputStream(configFile), false); //StreamUtils.readXML(configfile),false);
		} catch (Exception e)
		{
			logger.fatal("Error processing configuration file. Check your config file.. Error : "+e.getMessage());
			System.exit(-1);
		}
		try {	
			runforever = config.isRunalways();
			do {
				//logger.info(config.getExportdirectory());
				logger.info("Writing export files to: " + config.getExportdirectory());
				
				//logger.info(config.getTableUCMD());
				logger.info("Table(s) used for Universal command: " + config.getTableUCMD()+".");
				
				DataHubDAO dao = DataHubDAO.getInstance();
				dao.initialize(config);
				logger.info("Query database for looking for new files..");
				List<Publication> newpubs = dao.queryDatabase(dao.STATUS_NEW);
				if (newpubs != null)
				{	
					logger.info("Found: " + newpubs.size()+" publication(s).");
					createFile(newpubs);
				}	
				dao.closeConnection();
				if (config.isRunalways())
				{
					logger.info("Sleeping to wait for next run..");
					try {
						Thread.sleep(config.getSleeptime()*1000);
					} catch (InterruptedException e)
					{
					    runforever = false;	
					}
				}
			} while (runforever);	
		}
		
		catch (Exception e)
		{
			logger.error("An error occured processing monitoring the Datahub or the monitor proces was interrupted by System user.");
		}
		logger.info("Datahub Monitoring stopped.." );
	}

	/*
	 * create a trigger file or generate a Universal command
	 * 
	 */
	public void createFile(List<Publication> pubs) throws IOException, InterruptedException
	{
		SimpleDateFormat formatter = new SimpleDateFormat(filename_pattern);
		SimpleDateFormat formatter2 = new SimpleDateFormat(filename_pattern2);
		SimpleDateFormat formatter3 = new SimpleDateFormat(filename_pattern3);	// Used for Universal command (PROD)
		String Table, Tables;
    
		for (int i=0; i < pubs.size(); i++)
		{
			Publication pub = pubs.get(i);
			Tables = config.getTableUCMD();
			Table = pub.getPublicationtable();
			
			if (Tables.contains(Table))
			{
				logger.info("Creating Universal command for: "+pub.getPublicationtable()+".");
				String commands = config.getUCMD();
				logger.info("UCMD Table: "+Table+" is part of "+Tables+ " in the configuration file.");
				String filenamepart3 = formatter3.format(new Date());	// Used for Universal command (PROD)
			//	commands=commands.replace("YYMMhhmmss",filenamepart3);	// Used for Universal command (PROD)
				commands=commands.replace("YYMM",filenamepart3);	// Used for Universal command (PROD)
				logger.info("Command will be executed: "+commands);						
				
			//	Process child = Runtime.getRuntime().exec("ping -c 1 8.8.8.8"); 									// Returns: 1
			//	Process child = Runtime.getRuntime().exec("ucmd -cmd \"TSOPSTAT A=PSJTM01W O=010 W=OPER S=C\"");	// Returns: 1020 
				Process child = Runtime.getRuntime().exec(commands);
				child.waitFor();
							
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(child.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(child.getErrorStream()));
				
			// Read the output from the command
				String s = null;
				while ((s = stdInput.readLine()) != null) {
					System.out.println(s);
					logger.info(s);
				}

			// Read any errors from the attempted command
				while ((s = stdError.readLine()) != null) {
				    System.out.println(s);
				    logger.error(s);
				}
				
				int returnCode = child.exitValue();	
				if (returnCode == 0)
				{	
					logger.info("Update status ("+Table+") in the publication control table.");
					DataHubDAO dao = DataHubDAO.getInstance();
					dao.updatePublication(dao.STATUS_SEND, pub.getId());
				} else if (returnCode == 1020)
				{
					logger.info("Error in the configuration file or command options (code: "+returnCode+").");
				}
				else
				{
					logger.fatal("The Universal command for "+Table+" was not succesful (code: "+returnCode+").");
				}
			} else if (pub.getPublicationtable() != null && pub.getPublicationtable().length() > 0 && pub.getPublicatedate() != null ) // && !pub.getPublicationtable().equals(Table))
			{
				boolean succesfull = false;
				logger.info("Creating trigger file for: "+pub.getPublicationtable()+"-"+pub.getSequence_id()+ " ("+pub.getId()+")");
				try {
					String filenamepart1 = formatter.format(pub.getPublicatedate());
					String filenamepart2 = formatter2.format(new Date());
										
					File file = new File(config.getExportdirectory()+"/"+pub.getPublicationtable()+filenamepart1+filenamepart2+".tok");
					PrintWriter writer = new PrintWriter(file);
					writer.write(pub.getPublicatename()+"-"+pub.getSequence_id()+"  "+pub.getSource()+"  Records:"+pub.getNfofrecords());
					writer.write("\n");
					writer.close();
					succesfull = true;
				} catch (Exception e)
				{
					logger.error("Error writing trigger file..."+e.getMessage()+".\n Check  File rights for "+config.getExportdirectory());
				}
				if (succesfull)
				{
					logger.info("Update status ("+pub.getPublicationtable()+") in the publication control table.");
					DataHubDAO dao = DataHubDAO.getInstance();
					dao.updatePublication(dao.STATUS_SEND, pub.getId());
				}
			} else
			{
				logger.fatal("The publicationTableName or the PublicationDate is empty of Publication "+pub.getId()+". NO TRIGGER FILE CAN BE SENT");
			}
		}
	}
}
