package com.revature.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.beans.*;
import com.revature.data.*;

import java.util.*;

public class UserServiceImpl implements UserService {
	UserDao ud = new UserDaoImpl();
	RequestDao rd = new RequestDaoImpl();
	private static Logger log = LogManager.getLogger(UserServiceImpl.class);
	
	@Override
	public Boolean addUser (User u) {
		if (ud.getUserByName(u.getName()) == null) {
			log.trace("Trying to add user");
			ud.addUser(u);
			log.trace("User added");
			return true;
		}else {
			log.warn("User already exists: "+u.getName());
			return false;
		}
	}
	
	public User getUserByName (String name) {
		User u = ud.getUserByName(name);
		if (u == null){
			log.warn("there is no user by that name");
			return u;
		}
		log.trace("User found");
		return u;
	}
	
	@Override
	public Float moneyRemainingThisYear (String userName) {
		Long thisYear = (System.currentTimeMillis()/31536000000L);
//		Integer yearInt = thisYear.intValue() + 1970;
		List<Request> allReqs = rd.loadAllRequestsFromUser(userName);
		Float used = 0F;
		for (Request req : allReqs){
			if (req.getEventDate().getTime()/31536000000L == thisYear || req.getStatus() == Status.COMPLETED) {
				used += req.getPayoutAmt();
			}
		}
		Float remaining = 1000 - used;
		return remaining;
	}
	
	@Override
	public List<Request> getAllRequestsForUser (String userName) {
		List<Request> allReqs = rd.loadAllRequestsFromUser(userName);
		return allReqs;
	}
	//getUserByName (String name);
	//changePosition (User u, String supervisor, String dept, String deptHead)
	
	@Override
	public List<String> getNoticesForUser (String userName) {
		List<String> notices = new ArrayList<String>();
		Collection<Request> reqs = rd.loadAllRequestsMap().values();
		for (Request req : reqs) {
			if (req !=null && req.getWhoHasIt() != null &&req.getWhoHasIt().equals(userName)) {
				notices.add(req.getNotice());
			} else {
				continue;
			}
		}
		return notices;
	}

}
