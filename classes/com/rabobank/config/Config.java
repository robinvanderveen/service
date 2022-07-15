package com.rabobank.config;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/*
 *  Config File.. 
 * 
 */
@Root
public class Config {
	
	@Element
	private Database database = null;

	@Element
	private String exportdirectory;

	@Element(required=false)
	private boolean runalways = false;

	@Element(required=false)
	private int sleeptime = 60;

	@Element(required=false)
	private boolean emptyexportdirectory=false;
	
	@Element(required=false)
	private String TableUCMD;

	@Element(required=false)
	private String UCMD;
	
	public String getExportdirectory() {
			return exportdirectory;
	}

	public void setExportdirectory(String exportdirectory) {
		this.exportdirectory = exportdirectory;
	}

	public String getTableUCMD() {
		return TableUCMD;
	}

	public String getUCMD() {
		return UCMD;
	}

	public void setTableUCMD(String TableUCMD) {
		this.TableUCMD = TableUCMD;
	}

	public void setUCMD(String UCMD) {
		this.UCMD = UCMD;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public boolean isRunalways() {
		return runalways;
	}

	public void setRunalways(boolean runalways) {
		this.runalways = runalways;
	}

	public int getSleeptime() {
		return sleeptime;
	}

	public void setSleeptime(int sleeptime) {
		this.sleeptime = sleeptime;
	}

}
