package com.revature.service;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.beans.*;
import com.revature.data.*;
import com.revature.utils.*;

public class RequestServiceImpl implements RequestService {
	UserDao ud = new UserDaoImpl();
	RequestDao rd = new RequestDaoImpl();
	UserService us = new UserServiceImpl();
	private static Logger log = LogManager.getLogger(RequestServiceImpl.class);
	
	/*
	 * 
	 * How do you get rid of notices once they're no longer applicable???
	 * 
	 */
	
	@Override
	public void sendNewRequestTo(Request r, String name) {
		log.trace("Sending new request with id " + r.getRequestId() + " to " + name);
		User u = ud.getUserByName(name);
		u.addRequestToReviewById(r.getRequestId());
		r.setWhoHasIt(name);
		r.setNotice("You have a reimbursement request to review, ID no:" + r.getRequestId());
	}
	
	@Override
	public void sendRequestTo(Request r, String name) {
		log.trace("Sending request with id " + r.getRequestId() + " to " + name);
		User u = ud.getUserByName(name);
		u.addRequestToReviewById(r.getRequestId());
		r.setWhoHasIt(name);
		r.setNotice("You have a reimbursement request to review, ID no:" + r.getRequestId());
		rd.updateRequest(r);
	}
	
	@Override
	public void sendRequestToWithNote(Request r, String name, String note) {
		User u = ud.getUserByName(name);
		log.trace(name);
		u.addRequestToReviewById(r.getRequestId());
		r.setWhoHasIt(name);
		log.trace(r.getWhoHasIt());
		r.setNotice("Request ID no: " + r.getRequestId() + ". " + note);
		log.trace(r.getNotice());
		rd.updateRequest(r);
	}
	
	@Override
	public void sendNewRequest (Request r) {
		log.trace("We're at the start of sendNewRequest");
		User u = r.getSubmitter();
		log.trace("User: " + u.getName());
		log.trace("is depthead: " + u.getIsDeptHead());
		log.trace("ispreapproved"+ r.getPreapproved());
		if (u.getIsDeptHead() || r.getPreapproved()) {
			 sendNewRequestTo(r, ud.getBencoUser());
		 } else if (u.getDirectSupervisor() != null) {
			 sendNewRequestTo(r, u.getDirectSupervisor());
		 } else if (u.getDepartment() != null){
			 sendNewRequestTo(r, ud.getHeadOfDept(u.getDepartment()));
		 } else {
			 sendNewRequestTo(r, ud.getBencoUser());
		 }
	}
	
	@Override
	public void sendRequest (Request r) {
		log.trace("Attempting to send a request");
		log.trace("Can we get a user by name?");
		User u = ud.getUserByName(r.getWhoHasIt());
		log.trace("The person who has it now is " + u.getName());
		if (!u.getIsDeptHead() && u.getDepartment() != null) {
			log.trace("You're not a deptHead & your dept is " + u.getDepartment());
			log.trace("Gonna send it to " +ud.getHeadOfDept(u.getDepartment()));
			sendRequestTo(r, ud.getHeadOfDept(u.getDepartment()));
		 } else {
			 sendRequestTo(r, ud.getBencoUser());
		 }
	}
	
	@Override
	public void exceedReimb (Request r, Float amt, String justification) {
		r.setActualReimb(amt);
		r.setExceedingJustification(justification);
		sendRequestToWithNote(r, r.getSubmitter().getName(), "Benco has sent an altered reimbursment amount for your review.");
	}
	
	@Override
	public void infoRequest (Request r, User requestor, String request) {
		sendRequestToWithNote(r, r.getSubmitter().getName(), request + " Requested by " + requestor);
	}
	
	@Override
	public Request getRequestById (Integer id) {
		return rd.loadRequest(id);
	}
	
	@Override
	public void createRequest (Request r) {
		log.trace("Creat new request");
		sendNewRequest(r);
		rd.addRequest(r);
	}
	
	@Override
	public void requestDoc (Request r) {
		User u = r.getSubmitter();
		String note = "Request ID no: " + r.getRequestId() + ". Please upload a supporting document for this request";
		sendRequestToWithNote(r, u.getName(), note);
	}
	
	@Override
	public void uploadDoc (Request r, String doc, String whoSentIt) {
//		User u = ud.getUserByName(r.getWhoHasIt());
		r.setEventDoc(doc);
		String note = "Request ID no: " + r.getRequestId() + " has been updated with a supporting document";
		sendRequestToWithNote(r, whoSentIt, note);
	}
	
	@Override
	public void sendForGrade (Request r) {
		r.setStatus(Status.AWAITINGGRADE);
		User u = r.getSubmitter();
		String note = "Request ID no: " + r.getRequestId() + ". Please upload grade when event is complete.";
		sendRequestToWithNote(r, u.getName(), note);
		r.setWhoHasIt(u.getName());
	}
	
	@Override
	public void uploadGrade (Request r, String doc) {
		User u = r.getSubmitter();
		r.setGradeProof(doc);
		User sup = ud.getUserByName(u.getDirectSupervisor());
		String note = "Request ID no: " + r.getRequestId() + ". Proof of grade submitted for review";
		r.setStatus(Status.GRADEINREVIEW);
		sendRequestToWithNote(r, sup.getName(), note);
		
	}
	
	@Override
	public void acceptGrade (Request r) {
		User u = r.getSubmitter();
		Float remaining = us.moneyRemainingThisYear(u.getName());
		Float reimb = r.getActualReimb();
		if (remaining < r.getActualReimb()) {
			reimb = remaining;
		}
		r.setPayoutAmt(reimb);
		r.setStatus(Status.COMPLETED);
		rd.updateRequest(r);
	}
	
	@Override
	public void denyRequest (Request r) {
		r.setStatus(Status.DENIED);
		User u = r.getSubmitter();
		String note = "Request ID no: " + r.getRequestId() + " has been denied by " + r.getWhoHasIt();
		sendRequestToWithNote (r, u.getName(), note);
	}
	
	@Override
	public void requestVerify (Request r) {
		String sup = r.getSubmitter().getDirectSupervisor();
		sendRequestToWithNote (r, sup, " Please confirm that this request has been preapproved.");
	}
	
	@Override
	public void verify (Request r) {
		sendForGrade(r);
//		String bc = ud.getBencoUser();
//		sendRequestToWithNote(r, bc, " This request has been preapproved.");
	}
	
	@Override
	public InputStream getDoc (Request r, String docType) {
		String docName = r.getRequestId() + docType;
		InputStream rs = S3Util.getInstance().getObject(docName);
		return rs;
	}

}
