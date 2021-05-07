package com.revature.service;

import java.io.InputStream;

import com.revature.beans.Request;
import com.revature.beans.User;

public interface RequestService {
	
	void sendNewRequestTo(Request r, String name);

	void sendRequestTo(Request r, String name);

	void sendRequestToWithNote(Request r, String name, String note);

	void sendNewRequest(Request r);

	void sendRequest(Request r);

	void exceedReimb(Request r, Float amt, String justification);

	void infoRequest(Request r, User requestor, String request);

	Request getRequestById(Integer id);

	void createRequest(Request r);

	void requestDoc(Request r);

	void uploadDoc(Request r, String doc, String whoSentIt);

	void sendForGrade(Request r);

	void uploadGrade(Request r, String doc);

	void acceptGrade(Request r);

	void denyRequest(Request r);

	void requestVerify(Request r);

	void verify(Request r);

	InputStream getDoc(Request r, String docType);

}