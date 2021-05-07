package com.revature.beans;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class User {
	private String name = null;
	private String directSupervisor = null;
	private String department = null;
	private Boolean isDeptHead = null; 
	private List<Integer> requestsToReviewId = new ArrayList<Integer>();
	
	private static final Logger log = LogManager.getLogger(User.class);
	
	public User(String username, String dept, String sup, Boolean head) {
		super();
		name = username;
		department = dept;
		if (dept == "benCo") {
			directSupervisor = null;
			isDeptHead = false;
		}else {
			isDeptHead = head;
			directSupervisor = sup;
		}
		log.trace("New User created, " + this.name);
	}
	
	public User() {
		super();
		log.trace("User created");
	}
	
	/*
	 * Getters and setters from here on out
	 */
	
	public String getName() {
		return name;
	}

	public void setName(String username) {
		this.name = username;
	}

	public String getDirectSupervisor() {
		return directSupervisor;
	}

	public void setDirectSupervisor(String directSupervisor) {
		this.directSupervisor = directSupervisor;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Boolean getIsDeptHead() {
		return isDeptHead;
	}

	public void setIsDeptHead(Boolean isDeptHead) {
		this.isDeptHead = isDeptHead;
	}

	public List<Integer> getRequestsToReviewById() {
		return requestsToReviewId;
	}

	public void setRequestsToReviewById(List<Integer> requestsToReviewById) {
		this.requestsToReviewId = requestsToReviewById;
	}
	
	public void addRequestToReviewById(Integer id) {
		this.requestsToReviewId.add(id);
	}
}