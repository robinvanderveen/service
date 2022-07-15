package com.rabobank.model;

import java.util.Date;

public class Publication {

	private String publicatename;
	private int id;
	private String publicationtable;
	private int sequence_id;
	private Date publicatedate; 
	private String status;
	private String source;
	private int nfofrecords;
	public String getPublicatename() {
		return publicatename;
	}
	
	public void setPublicatename(String publicatename) {
		this.publicatename = publicatename;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPublicationtable() {
		return publicationtable;
	}
	public void setPublicationtable(String publicationtable) {
		this.publicationtable = publicationtable;
	}
	public int getSequence_id() {
		return sequence_id;
	}
	public void setSequence_id(int sequence_id) {
		this.sequence_id = sequence_id;
	}
	public Date getPublicatedate() {
		return publicatedate;
	}
	public void setPublicatedate(Date publicatedate) {
		this.publicatedate = publicatedate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public int getNfofrecords() {
		return nfofrecords;
	}
	public void setNfofrecords(int nfofrecords) {
		this.nfofrecords = nfofrecords;
	}
	
	
}
