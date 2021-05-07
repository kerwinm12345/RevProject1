package com.revature.beans;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.data.*;

public class Request {
	static int idGenerator;
	int requestId;
	Timestamp submissionDate = null;
	User submitter = null;
	Status status = null;
	Timestamp eventDate = null;
	Boolean isUrgent = null;
	String eventLocation = null;
	Float cost = null;
	EventType eventType = null;
	String justification = null;
	String exceedingJustification = null; //only if exceeding == true
	String whoHasIt = null;
	Float projectedReimb = null;
	Float actualReimb = null; //only to be edited by BenCo
	Float payoutAmt = null; //final number, only generated when request is complete
	Boolean preapproved = null; 
	Boolean preapproveAccepted = null; 
	String preapproveDoc = null; //bucket key
	String eventDoc = null; //bucket key
	Float timeMissed = null;
	String notice = null;
	String gradeProof = null; //bucket key
	
	private static final Logger log = LogManager.getLogger(Request.class);
	
	public Request() {
		super();
		log.trace("Request created");
		
	}

	public Request(User u, Timestamp d, String loc, EventType type, Float cost1, String just, String preDoc, String supportingDoc, Float timeOff) {
		super();
		RequestDao rd = new RequestDaoImpl();
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		log.trace(ts);
		
		idGenerator = rd.getIdGen();
		requestId = idGenerator;
		idGenerator ++;
		log.trace("Id Generator upped, " + idGenerator);
		submissionDate = ts;
		submitter = u;
		status = Status.PENDING;
		eventDate = d;
		eventLocation = loc;
		eventType = type;
		justification = just;
		cost = cost1;
		projectedReimb = cost1*type.getValue()/100;
		actualReimb = projectedReimb;
		payoutAmt = (float) 0.0;
		preapproveDoc = preDoc;
		eventDoc = supportingDoc;
		timeMissed = timeOff;
		
		if (preDoc != null) {
			preapproved = true;
		} else {
			preapproved = false;
		}
		
		long fortnight = 1209600000;
		 if (d.getTime() - ts.getTime() < fortnight) {
			 isUrgent = true;
		 } else {
			 isUrgent = false;
		 }
		 
		 rd.saveIdGen(idGenerator);
		 log.trace("New request created with ID " + this.requestId);
		  
	}


	public static int getIdGenerator() {
		return idGenerator;
	}


	public static void setIdGenerator(int idGenerator) {
		Request.idGenerator = idGenerator;
	}


	public int getRequestId() {
		return requestId;
	}


	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}


	public Timestamp getSubmissionDate() {
		return submissionDate;
	}


	public void setSubmissionDate(Timestamp submissionDate) {
		this.submissionDate = submissionDate;
	}


	public User getSubmitter() {
		return submitter;
	}


	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}


	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}


	public Timestamp getEventDate() {
		return eventDate;
	}


	public void setEventDate(Timestamp eventDate) {
		this.eventDate = eventDate;
	}


	public Boolean getIsUrgent() {
		return isUrgent;
	}


	public void setIsUrgent(Boolean isUrgent) {
		this.isUrgent = isUrgent;
	}


	public String getEventLocation() {
		return eventLocation;
	}


	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}


	public Float getCost() {
		return cost;
	}


	public void setCost(Float cost) {
		this.cost = cost;
	}


	public EventType getEventType() {
		return eventType;
	}


	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}


	public String getJustification() {
		return justification;
	}


	public void setJustification(String justification) {
		this.justification = justification;
	}


	public String getExceedingJustification() {
		return exceedingJustification;
	}


	public void setExceedingJustification(String exceedingJustification) {
		this.exceedingJustification = exceedingJustification;
	}


	public String getWhoHasIt() {
		return whoHasIt;
	}


	public void setWhoHasIt(String whoHasIt) {
		this.whoHasIt = whoHasIt;
	}


	public Float getProjectedReimb() {
		return projectedReimb;
	}


	public void setProjectedReimb(Float projectedReimb) {
		this.projectedReimb = projectedReimb;
	}


	public Float getActualReimb() {
		return actualReimb;
	}


	public void setActualReimb(Float actualReimb) {
		this.actualReimb = actualReimb;
	}


	public Float getPayoutAmt() {
		return payoutAmt;
	}


	public void setPayoutAmt(Float payoutAmt) {
		this.payoutAmt = payoutAmt;
	}


	public Boolean getPreapproved() {
		return preapproved;
	}


	public void setPreapproved(Boolean preapproved) {
		this.preapproved = preapproved;
	}


	public Boolean getPreapproveAccepted() {
		return preapproveAccepted;
	}


	public void setPreapproveAccepted(Boolean preapproveAccepted) {
		this.preapproveAccepted = preapproveAccepted;
	}


	public String getPreapproveDoc() {
		return preapproveDoc;
	}


	public void setPreapproveDoc(String preapproveDoc) {
		this.preapproveDoc = preapproveDoc;
	}


	public String getEventDoc() {
		return eventDoc;
	}


	public void setEventDoc(String eventDoc) {
		this.eventDoc = eventDoc;
	}


	public Float getTimeMissed() {
		return timeMissed;
	}


	public void setTimeMissed(Float timeMissed) {
		this.timeMissed = timeMissed;
	}
	
	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getGradeProof() {
		return gradeProof;
	}


	public void setGradeProof(String gradeProof) {
		this.gradeProof = gradeProof;
	}
	
}

/*date, time, location, description, cost, grading format, and type of event; 
work-related justification.  The employee can optionally include: event-related 
attachments of pdf, png, jpeg, txt, or doc file type, attachments of approvals 
already provided of .msg (Outlook Email File) file type and type of approval, work 
time that will be missed */
