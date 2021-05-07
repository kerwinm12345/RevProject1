package com.revature.data;

import java.util.List;
import java.util.Map;

import com.revature.beans.Request;

public interface RequestDao {

	Integer getIdGen();

	void saveIdGen(Integer idGen);

	void addRequest(Request r);

	Request loadRequest(Integer id);

	void updateRequest(Request r);

	Map<Integer, Request> loadAllRequestsMap();

	List<Request> loadAllRequestsFromUser(String userName);

}