package com.revature.data;

import java.util.List;
import java.util.Map;

import com.revature.beans.User;

public interface UserDao {

	void addUser(User u);

	User getUserByName(String name);
	
	void updateUser(User u);
	
	String getHeadOfDept(String dept);
	
	List<String> getAllUserNames();
	
	Map<String, User> getAllUsersMap();

	String getBencoUser();
	
	//	/*temp*/Map<String, User> loadUsers () {
	//		ArrayList<User> tempUsers = new ArrayList<User>();
	//		tempUsers.add(abby);
	//		tempUsers.add(brandon);
	//		tempUsers.add(cecelia);
	//		tempUsers.add(douglas);
	//		return tempUsers;
	//	}

	

	

}