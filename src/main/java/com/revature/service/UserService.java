package com.revature.service;

import java.util.List;

import com.revature.beans.Request;
import com.revature.beans.User;

public interface UserService {

	Boolean addUser(User u);
	
	User getUserByName (String name);

	Float moneyRemainingThisYear(String userName);

	List<Request> getAllRequestsForUser(String userName);
	
	List<String> getNoticesForUser (String userName);

}