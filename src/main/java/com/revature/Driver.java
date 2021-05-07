package com.revature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.sql.Timestamp;
import java.time.Instant; 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.beans.*;
import com.revature.data.*;
import com.revature.service.RequestService;
import com.revature.service.RequestServiceImpl;
import com.revature.service.UserService;
import com.revature.service.UserServiceImpl;
import com.revature.utils.*;
import com.revature.controller.*;

import io.javalin.Javalin;

public class Driver {
	
	private static final Logger log = LogManager.getLogger(Driver.class);
	static UserDao ud = new UserDaoImpl();
	static RequestDao rd = new RequestDaoImpl();
	public static UserService us = new UserServiceImpl();
	public static RequestService rs = new RequestServiceImpl();


	public static void main(String[] args) throws Exception {
		
		log.trace("Begin the TRMS application");
		Javalin app = Javalin.create().start(8000);
		
//		dropTables();
//		TimeUnit.SECONDS.sleep(60);
//		usersTable();
//		requestsTable();
//		TimeUnit.SECONDS.sleep(60);
//		populateUsers();
		
		//login
		app.post("/users", UserController::login);
		
		//logout
		app.delete("/users", UserController::logout);
		
		//user check money remaining for this year
		app.get("/users/:name/money", UserController::moneyRemaining);
		
		//request create a new request
		app.post("/requests", RequestController::newRequest);
		
		//cancel a request
		app.put("requests/:requestId/cancel", RequestController::cancelRequest);
		
		//request see old requests
		app.get("users/myRequests", UserController::showMyRequests);
		
		//see the requests you have to review
		app.get("users/reviewRequests", UserController::reviewRequests);
		
		//approve/deny a request
		app.put("requests/:requestId/approve", RequestController::approve);
		
		//request supporting docs
		app.put("requests/:requestId/requestEventDoc", RequestController::requestDoc);
		
		//request preapproved verification
		app.put("requests/:requestId/requestPreappVerification", RequestController::requestVerify);
		
		//verify preapproval 
		app.put("requests/:requestId/Verify", RequestController::verify);
		
		//exceed reimbursement amount
		app.put("requests/:requestId/exceed", RequestController::exceed);
		
		//accept exceeded reimbursement amount
		app.put("requests/:requestId/acceptExceed", RequestController::acceptExceed);
		
		//request supporting docs
		app.post("requests/:requestId/eventDoc", RequestController::uploadEventDoc);
		
		//request upload grade
		app.post("requests/:requestId/gradeProof", RequestController::uploadGradeProof);
		
		//get event doc
		app.get("requests/:requestId/:docType", RequestController::getDoc);
		
		//get all requests made by logged in user
		app.get("/requests/getRequests", RequestController::getRequests);
		
		//approve/deny a request
		app.put("requests/:requestId/approveGrade", RequestController::approveGrade);
		
	}
	
	
	
	/*
	 * From here down is one-time use setup functions
	 */
	
	/*
	 * To create the Keyspace
	 */
	public static void makeKeyspace() {
		DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
		try(CqlSession session = CqlSession.builder()
				.withConfigLoader(loader)
				.build()) {
			StringBuilder sb = new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
					.append("trms with replication = {")
					.append("'class':'SimpleStrategy','replication_factor' : 1};");
				session.execute(sb.toString());
				}
	}
	
	/*
	 * To create the player table
	 */
	public static void usersTable() {
	DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
	try(CqlSession session = CqlSession.builder()
			.withConfigLoader(loader)
			.withKeyspace("trms")
			.build()) {
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
				.append("users (")
				.append("name text PRIMARY KEY,")
				.append("directSupervisor text,")
				.append("department text,")
				.append("isDeptHead boolean,")
				.append("requestsToReviewId list<int>);");
		session.execute(sb.toString());
			}
	}
	
	/*
	 * To create the request table
	 */
	
	public static void requestsTable() {
		DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
		try(CqlSession session = CqlSession.builder()
				.withConfigLoader(loader)
				.withKeyspace("trms")
				.build()) {
			StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
					.append("requests (")
					.append("requestId int PRIMARY KEY,")
					.append("submissionDate bigint,")
					.append("submitter text,")
					.append("status text,")
					.append("eventDate bigint,")
					.append("isUrgent boolean,")
					.append("eventLocation text,")
					.append("cost float,")
					.append("eventType text,")
					.append("justification text,")
					.append("exceedingJustification text,")
					.append("whoHasIt text,")
					.append("projectedReimb float,")
					.append("actualReimb float,")
					.append("payoutAmt float,")
					.append("preapproved boolean,")
					.append("preapproveAccepted boolean,")
					.append("preapproveDoc text,")
					.append("eventDoc text,")
					.append("timeMissed float,")
					.append("notice text,")
					.append("gradeProof text);");
			session.execute(sb.toString());
			rd.saveIdGen(2);
				}
	}
	
	public static void populateUsers() {
		User abby = new User("Abby", "IT", "Brandon", false);
		User brandon = new User("Brandon", "IT", "Cecelia", false);
		User cecelia = new User("Cecelia", "IT", null, true );
		User douglas = new User("Douglas", "BenCo", "Emelia", false);
		User emelie = new User("Emelie", "BenCo", null, true);
		Request r = new Request();
		r.setRequestId(1);
		r.setSubmissionDate(new Timestamp(1609545600000L));  /////
		r.setSubmitter(cecelia);
		r.setStatus(Status.COMPLETED);
		r.setEventDate(new Timestamp(1610409600000L));  /////
		r.setIsUrgent(true);
		r.setEventLocation("USF");
		r.setCost(800f);
		r.setEventType(EventType.UNIVERSITYCOURSE);
		r.setJustification("One semester night class on advanced Java for professional development");
		r.setProjectedReimb(640f);
		r.setActualReimb(640f);
		r.setPayoutAmt(640f);
		r.setPreapproved(false);
		rd.addRequest(r);
		ud.addUser(abby);
		ud.addUser(brandon);
		ud.addUser(cecelia);
		ud.addUser(douglas);
		ud.addUser(emelie);
	}
	
	/*
	 * To initialize IdGen to 1
	 */
	
	public static void initializeIdGen() {
		rd.saveIdGen(2);
	}
	
	public static void dropTables() {
		DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
		try(CqlSession session = CqlSession.builder()
				.withConfigLoader(loader)
				.withKeyspace("trms")
				.build()) {
			String s = "DROP TABLE users;";
			session.execute(s);
			String s2 = "DROP TABLE requests;";
			session.execute(s2);
		}
	}

}
