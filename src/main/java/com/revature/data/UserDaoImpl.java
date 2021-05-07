package com.revature.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
//import com.revature.beans.Request;
import com.revature.beans.User;
import com.revature.utils.CassandraUtil;

public class UserDaoImpl implements UserDao {
	
	private CqlSession session = CassandraUtil.getInstance().getSession();
	private static final Logger log = LogManager.getLogger(UserDao.class);

	@Override
	public void addUser(User u){
		log.trace("Adding user to users table");
		String query = "Insert into users (name, directSupervisor, department, isDeptHead, requestsToReviewId) values (?,?,?,?,?); ";
		SimpleStatement s = new SimpleStatementBuilder(query)
				.setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s).bind(u.getName(), u.getDirectSupervisor(), u.getDepartment(), u.getIsDeptHead(), u.getRequestsToReviewById());
		session.execute(bound);
	}
	
	@Override
	public List<String> getAllUserNames(){
		log.trace("Loading all names from table users");
		String query = "select name from users";
		ResultSet rs = session.execute(query);
		List<String> namesList = new ArrayList<String>();
		log.trace("Compiling names into list");
		rs.forEach(data -> {
			namesList.add(data.getString("name"));
		});
		return namesList;
	}
	
	
	@Override
	public User getUserByName(String name) {
		log.trace("Loading user, " + name);
		User u = null;
		String query = "Select name, "
				+ "directSupervisor, "
				+ "department, "
				+ "isDeptHead, "
				+ "requestsToReviewId "
				+ "from users where name = ?;";
		BoundStatement bound = session.prepare(query).bind(name);
		ResultSet rs = session.execute(bound);
		Row data = rs.one();
		if(data != null) {
			log.trace("User " + name + " found");
			u = new User();
			u.setName(data.getString("name"));
			u.setDirectSupervisor(data.getString("directSupervisor"));
			u.setDepartment(data.getString("department"));
			u.setIsDeptHead(data.getBoolean("isDeptHead"));
			u.setRequestsToReviewById(data.getList("requestsToReviewId", Integer.class));
		}
		return u;
	}
	
	@Override
public Map<String,User> getAllUsersMap(){
		log.trace("Mapping users by name");
		Map<String,User> userMap = new HashMap<String,User>();
		List<String> userList = getAllUserNames();
		userList.forEach(element -> {
			User u = getUserByName(element);
			userMap.put(element, u);
		});
		return userMap;
	}
	
	@Override
	public void updateUser(User u) {
		log.trace("Updating user " + u.getName() + " in users table");
		String query = "update users set directSupervisor = ?, department = ?, isDeptHead = ?, requestsToReviewId = ? where name = ?";
		SimpleStatement s = new SimpleStatementBuilder(query)
				.setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s).bind(u.getDirectSupervisor(), u.getDepartment(), u.getIsDeptHead(), u.getRequestsToReviewById(), u.getName());
		session.execute(bound);
	}
	
	@Override
	public String getHeadOfDept (String dept) {
		log.trace("getting department head for " + dept);
		Map<String,User> usersMap = getAllUsersMap();
		log.trace("Got the user map");
		Collection<User> users = usersMap.values();
		log.trace("The collection was created");
		for (User u : users) {
			log.trace("In the for loop");
			log.trace("Checking if " + u.getName() + " is dead of dept, " + dept);
			log.trace(u.getName() + ": isDeptHead = " + u.getIsDeptHead());
			if (u.getIsDeptHead() && u.getDepartment().equals(dept)) {
				log.trace("Checking if " + u.getName() + " is dead of dept, " + dept);
				return u.getName();
			}
		}
		return null; // This should return an error that there is no head of this department!
	}
	
	public String getBencoUser() {
		log.trace("Finding a BenCo case handler");
		Map<String,User> usersMap = getAllUsersMap();
		Collection<User> users = usersMap.values();
		for (User u : users) {
			log.trace("for looping for user " + u.getName());
			if (!u.getIsDeptHead() && u.getDepartment().equals("BenCo")) {
				log.trace("BenCo user found, " + u.getName());
				return u.getName();
			};
		}
		
		return null; // This should return an error that there is no head of this department!
	}
}