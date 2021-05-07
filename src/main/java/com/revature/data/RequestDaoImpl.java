package com.revature.data;

//import java.io.IOException;
import java.io.InputStream;
//import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.sql.Timestamp;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.revature.beans.*;
import com.revature.utils.*;

public class RequestDaoImpl implements RequestDao {
	
	private CqlSession session = CassandraUtil.getInstance().getSession();
	private static final Logger log = LogManager.getLogger(RequestDao.class);
	UserDao ud = new UserDaoImpl();

	//save to bucket
	//get from bucket
	
	@Override
	public Integer getIdGen () {
		InputStream rs = S3Util.getInstance().getObject("idgen");
		Scanner sc = new Scanner(rs);
		int rsInt = sc.nextInt();
		log.trace("Getting idGen from bucket, " + rsInt);
		return rsInt;
	}
	
	@Override
	public void saveIdGen (Integer idGen) {
		log.trace("Saving idGen to bucket, " + idGen);
		byte[] idBytes = idGen.toString().getBytes();
		S3Util.getInstance().uploadToBucket("idgen", idBytes);
	}
	
	@Override
	public void addRequest (Request r) {
		log.trace("Adding request " + r.getRequestId() + " to requests table");
		String query = "Insert into requests ("
				+ "requestId, "
				+ "submissionDate, "
				+ "submitter, "
				+ "status, "
				+ "eventDate, "
				+ "isurgent, "
				+ "eventLocation, "
				+ "cost, "
				+ "eventType, "
				+ "justification, "
				+ "exceedingJustification, "
				+ "whoHasIt, "
				+ "projectedReimb, "
				+ "actualReimb, "
				+ "payoutAmt, "
				+ "preapproved, "
				+ "preapproveAccepted, "
				+ "preapproveDoc, "
				+ "eventDoc, "
				+ "timeMissed, "
				+ "notice, "  /////
				+ "gradeproof) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); ";
		SimpleStatement s = new SimpleStatementBuilder(query)
				.setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s).bind(
				r.getRequestId(), 
				r.getSubmissionDate().getTime(), 
				r.getSubmitter().getName(), 
				r.getStatus().toString(), 
				r.getEventDate().getTime(), 
				r.getIsUrgent(), 
				r.getEventLocation(), 
				r.getCost(), 
				r.getEventType().toString(), 
				r.getJustification(),
				r.getExceedingJustification(), 
				r.getWhoHasIt(), 
				r.getProjectedReimb(), 
				r.getActualReimb(), 
				r.getPayoutAmt(), 
				r.getPreapproved(), 
				r.getPreapproveAccepted(), 
				r.getPreapproveDoc(), 
				r.getEventDoc(), 
				r.getTimeMissed(), 
				r.getNotice(), /////
				r.getGradeProof());
		session.execute(bound);
	}
	
	@Override
	public Request loadRequest (Integer id) {
		log.trace("Loading request " + id + " from requests table");
		Request r = null;
		String query = "Select requestId, "
				+ "submissionDate, "
				+ "submitter, "
				+ "status, "
				+ "eventDate, "
				+ "isUrgent,"
				+ "eventLocation, "
				+ "cost, "
				+ "eventType, "
				+ "justification, "
				+ "exceedingJustification, "
				+ "whoHasIt, "
				+ "projectedReimb, " 
				+ "actualReimb, "
				+ "payoutAmt, "
				+ "preapproved, "
				+ "preapproveAccepted, "
				+ "preapproveDoc, "
				+ "eventDoc, "
				+ "timeMissed, "
				+ "notice, " /////
				+ "gradeproof from requests where requestId = ?;";
		BoundStatement bound = session.prepare(query).bind(id);
		ResultSet rs = session.execute(bound);
		Row data = rs.one();
		if(data != null) {
			r = new Request();
			r.setRequestId((Integer) data.getInt("requestId"));
			r.setSubmissionDate(new Timestamp(data.get("submissionDate", Long.class)));
			r.setSubmitter(ud.getUserByName(data.getString("submitter")));
			r.setStatus(Status.valueOf(data.getString("status")));
			r.setEventDate(new Timestamp(data.get("eventDate", Long.class)));
			/**/
			r.setIsUrgent(data.get("isurgent", Boolean.class)); //come back to this one??
			/**/
			r.setEventLocation(data.getString("eventLocation"));
			r.setCost(data.getFloat("Cost"));
			r.setEventType(EventType.valueOf(data.getString("eventType")));
			r.setJustification(data.getString("Justification"));
			r.setExceedingJustification(data.getString("exceedingJustification"));
			r.setWhoHasIt(data.getString("whoHasIt"));
			r.setProjectedReimb(data.getFloat("projectedReimb"));
			r.setActualReimb(data.getFloat("actualReimb"));
			r.setPayoutAmt(data.getFloat("payoutAmt"));
			r.setPreapproved(data.getBoolean("preapproved"));
			r.setPreapproveAccepted(data.getBoolean("preapproveAccepted"));
			r.setPreapproveDoc(data.getString("PreapproveDoc"));
			r.setEventDoc(data.getString("eventDoc"));
			r.setTimeMissed(data.getFloat("timeMissed"));
			r.setNotice(data.getString("notice"));
			r.setGradeProof(data.getString("gradeProof"));
		}
		return r;
	}
	
	@Override
	public Map <Integer, Request> loadAllRequestsMap (){
		log.trace("Mapping requests by id");
		int reqNo = getIdGen();
		Map <Integer, Request> allRequestsMap = new HashMap <Integer, Request>();
		for (int i = 1; i < reqNo; i++) {
			allRequestsMap.put((Integer) i, loadRequest(i));
		}
		return allRequestsMap;
		
	}
	
	@Override
	public void updateRequest (Request r) {
		log.trace("Updating request " + r.getRequestId() + " in requests table");
		String query = "update requests set submissionDate = ?, "
				+ "submitter = ?, "
				+ "status = ?, "
				+ "eventDate = ?, "
				+ "isUrgent = ?, "
				+ "eventLocation = ?, "
				+ "cost = ?, "
				+ "eventType = ?, "
				+ "justification = ?, "
				+ "exceedingJustification = ?, "
				+ "whoHasIt = ?, "
				+ "projectedReimb = ?, "
				+ "actualReimb = ?, "
				+ "payoutAmt = ?, "
				+ "preapproved = ?, "
				+ "preapproveAccepted = ?, "
				+ "preapproveDoc = ?, "
				+ "eventDoc = ?, "
				+ "timeMissed = ?, "
				+ "notice = ?,"
				+ "gradeproof = ? where requestId = ?";
		SimpleStatement s = new SimpleStatementBuilder(query)
				.setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s).bind(
				r.getSubmissionDate().getTime(), 
				r.getSubmitter().getName(), 
				r.getStatus().toString(), 
				r.getEventDate().getTime(), 
				r.getIsUrgent(), 
				r.getEventLocation(), 
				r.getCost(), 
				r.getEventType().toString(), 
				r.getJustification(), 
				r.getExceedingJustification(),
				r.getWhoHasIt(), 
				r.getProjectedReimb(), 
				r.getActualReimb(), 
				r.getPayoutAmt(), 
				r.getPreapproved(), 
				r.getPreapproveAccepted(), 
				r.getPreapproveDoc(), 
				r.getEventDoc(), 
				r.getTimeMissed(),
				r.getNotice(),
				r.getGradeProof(), r.getRequestId());
		session.execute(bound);
	}
	
	@Override
	public List<Request> loadAllRequestsFromUser (String userName) {
		log.trace("creating list of all requests made by " + userName);
		List <Request> allRequestsFromUser = new ArrayList<Request>();
		Map <Integer, Request> allRequestsMap = loadAllRequestsMap();
		Collection <Request> allRequests = allRequestsMap.values();
		for (Request r : allRequests) {
			if (r !=null && r.getSubmitter().getName().equals(userName)){
				allRequestsFromUser.add(r);
			}
		}
		return allRequestsFromUser;
	}
	//loadRequest
	//saveRequest
	//newRequest
	//updateRequestById
	//getRequestById

}
