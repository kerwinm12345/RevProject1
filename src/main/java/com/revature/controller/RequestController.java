package com.revature.controller;

import java.io.InputStream;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import com.datastax.oss.driver.shaded.guava.common.io.ByteStreams;
import com.revature.beans.*;
import com.revature.data.RequestDao;
import com.revature.data.RequestDaoImpl;
import com.revature.service.*;
import com.revature.utils.*;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import software.amazon.awssdk.core.sync.RequestBody;

public class RequestController {
	private static Logger log = LogManager.getLogger(RequestController.class);
	public static UserService us = new UserServiceImpl();
	public static RequestService rs = new RequestServiceImpl();
	public static RequestDao rd = new RequestDaoImpl();
	
	public static void newRequest (Context ctx){
		log.trace("Submitting a new request");
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		String d1 = ctx.formParam("eventDate");
		Timestamp d = new Timestamp(Long.parseLong(d1));
		String loc = ctx.formParam("location");
		EventType type = EventType.valueOf(ctx.formParam("eventType"));//error?
		Float cost = Float.parseFloat(ctx.formParam("cost"));
		String just = ctx.formParam("justification");
//		String preDoc = ctx.formParam("preapproveDoc"); //
//		String supportingDoc = ctx.formParam("eventDoc"); //
		Float timeOff = Float.parseFloat(ctx.formParam("timeOff"));
//		log.trace("Before");
		String preDoc = null;
		String supportingDoc = null;
//		log.trace("After");
		Request r = new Request(u,d,loc,type,cost,just,preDoc,supportingDoc,timeOff);
		
//		log.trace("Still going");
		if (ctx.uploadedFile("preapproveDoc") != null) {
			preDoc = r.getRequestId() + "PreapproveDoc";
			UploadedFile preDocFile = ctx.uploadedFile("preapproveDoc");
			S3Util.getInstance().uploadToBucket(preDoc, RequestBody.fromInputStream(preDocFile.getContent(), preDocFile.getSize()));
			r.setPreapproveDoc(preDoc);
			r.setPreapproved(true);
		}
//		log.trace("Ooof");
		if (ctx.uploadedFile("eventDoc") != null) {
			supportingDoc = r.getRequestId() + "EventDoc";
			UploadedFile supportingDocFile = ctx.uploadedFile("eventDoc");
			S3Util.getInstance().uploadToBucket(supportingDoc, RequestBody.fromInputStream(supportingDocFile.getContent(), supportingDocFile.getSize()));
			r.setEventDoc(supportingDoc);
		}
//		log.trace("Here???");
		rs.createRequest(r);
		ctx.result(r.getProjectedReimb().toString());
	}
	
	public static void cancelRequest (Context ctx){
		//log.trace("");
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		Integer id = Integer.parseInt(ctx.formParam("RequestId"));
		Request r = rs.getRequestById(id);
		if (u.getName().equals(r.getSubmitter().getName())) {
			r.setStatus(Status.CANCELLED);
			r.setWhoHasIt(null);
			//log.trace("");
		} else {
			//log.warn("");
			//return error
		}
	}
	
	public static void approve (Context ctx){
		log.trace("Attempting to approve");
		if (ctx.sessionAttribute("User") == null) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		log.trace(u.getName() + " is logged in");
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		log.trace("Request ID: " + id);
		Request r = rs.getRequestById(id);
		log.trace(r);
		Boolean approved = Boolean.parseBoolean(ctx.formParam("Approved"));
		log.trace("Is it approved? " + approved);
		log.trace("Who Has it: " + r.getWhoHasIt());
		if (u.getName().equals(r.getWhoHasIt())){
			log.trace("If you got here, loggedin == whohasit");
			if (approved) {
				if (u.getDepartment().equals("BenCo")) {
					log.trace("If you got here, your logged in as benco");
					rs.sendForGrade(r);
					log.trace("If you got here the send for grade went through");
				} else {
					log.trace("If you got here you're not logged in as Benco");
					rs.sendRequest(r);
					log.trace("The send request went through");
				}
			} else {
				log.trace("Request denied");
				rs.denyRequest(r);
				}
		}
//		} else { 
//			log.warn("This person doesn't have access to this request");
//			ctx.status(403);
	}
	
	public static void requestDoc (Context ctx){
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		Request r = rs.getRequestById(id);
		if (u.getName().equals(r.getWhoHasIt())){
			rs.requestDoc(r);
		} else {
			//you don't have the authority to do this
		}
	}
	
	public static void requestVerify (Context ctx){
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		Request r = rs.getRequestById(id);
//		if (u.getDepartment().equals("BenCo") && u.getName().equals(r.getWhoHasIt())){
			log.trace("Did this work???? Am I in this if statement");
			rs.requestVerify(r);
//		} else {
//			log.trace("You blew it, you're not in the if statement");
			//You don't have permission to do this
		}
//	}
	
	public static void verify (Context ctx){
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		Request r = rs.getRequestById(id);
		Boolean verified = Boolean.parseBoolean(ctx.formParam("Verified"));
		if (u.getName().equals(r.getWhoHasIt())) {
			if (verified) {
				rs.verify(r);
			} else {
				rs.denyRequest(r);
			}
		} else {
			//no permission
		}
	}
	
	public static void exceed (Context ctx){
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		Request r = rs.getRequestById(id);
		Float money = Float.parseFloat(ctx.formParam("Money"));
		String just = ctx.formParam("Justification");
		if (u.getName().equals(r.getWhoHasIt()) && u.getDepartment().equals("BenCo")) {
			r.setActualReimb(money);
			r.setExceedingJustification(just);
			rs.exceedReimb(r, money, just);
		} else {
			
		}
	}
	
	public static void acceptExceed (Context ctx) {
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		Request r = rs.getRequestById(id);
		Boolean accept = Boolean.parseBoolean(ctx.formParam("Accept"));
		if (u.getName().equals(r.getWhoHasIt()) && u.equals(r.getSubmitter())) {
			if (accept) {
				rs.sendForGrade(r);
			} else {
				r.setStatus(Status.CANCELLED);
				r.setWhoHasIt(null);
			}
		} else {
			//you can't accept this request
		}
	}
	
	public static void uploadEventDoc (Context ctx){
		log.trace("");
		log.trace("Starting EventDoc upload");
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		log.trace("You're logged in as " + u.getName());
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		log.trace("The requestId is " + id);
		Request r = rs.getRequestById(id);
		String whoSentIt = ctx.formParam("whoSentIt");
		log.trace("whoSentIt is " + whoSentIt);
		if (u.getName().equals(r.getSubmitter().getName())) {
			log.trace("You're logged in as submitter");
			if (ctx.uploadedFile("eventDoc") != null) {
				log.trace("The file isn't null");
				String supportingDoc = r.getRequestId() + "EventDoc";
				UploadedFile supportingDocFile = ctx.uploadedFile("eventDoc");
				S3Util.getInstance().uploadToBucket(supportingDoc, RequestBody.fromInputStream(supportingDocFile.getContent(), supportingDocFile.getSize()));
				r.setEventDoc(supportingDoc);
				rs.uploadDoc(r, supportingDoc, whoSentIt);
			} else {
				//error file is null
				log.trace("The file is null");
			}
		} else {
			log.trace("U R Here.");
			//error you can't submit a doc
		}
	}
	
	public static void uploadGradeProof (Context ctx){
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		Request r = rs.getRequestById(id);
		if (u.equals(r.getSubmitter())) {
			if (ctx.uploadedFile("gradeProof") != null) {
				String gradeProofDoc = r.getRequestId() + "GradeProof";
				UploadedFile gradeProofFile = ctx.uploadedFile("gradeProof");
				S3Util.getInstance().uploadToBucket(gradeProofDoc, RequestBody.fromInputStream(gradeProofFile.getContent(), gradeProofFile.getSize()));
				r.setEventDoc(gradeProofDoc);
			} else {
				//error file is null
			}
		} else {
			//error you can't submit a doc
		}
	}
	
	public static void getDoc (Context ctx) {
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		Request r = rs.getRequestById(id);
		String docType = ctx.pathParam("docType");
		String fileName;
		log.trace(r.getSubmitter().getName());
		log.trace(u.getName());
		log.trace("First if");
			if (docType.equals("EventDoc") || docType.equals("PreapproveDoc") || docType.equals("GradeProof")) {
				log.trace("Second if");
				fileName = id + docType;
				InputStream file = S3Util.getInstance().getObject(fileName);
				ctx.result(file);
			} else {
				//That's not a valid document
			}
			//You're not approved
		}
	
	public static void getRequests (Context ctx) {
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		List<Request> all = rd.loadAllRequestsFromUser(u.getName());
		ctx.json(all);
	}
	
	public static void approveGrade (Context ctx) {
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		Integer id = Integer.parseInt(ctx.pathParam("requestId"));
		Request r = rs.getRequestById(id);
		Boolean pass = Boolean.parseBoolean(ctx.formParam("passed"));
		if (pass) {
			rs.acceptGrade(r);
		}
	}

}
