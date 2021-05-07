package com.revature.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.service.*;
import com.revature.beans.*;

import java.util.*;

import io.javalin.http.Context;

public class UserController {
	private static Logger log = LogManager.getLogger(UserController.class);
	public static UserService us = new UserServiceImpl();
	public static RequestService rs = new RequestServiceImpl();
	
	
//	public static void register(Context ctx) {
//		log.trace("Attempting to register");
//		User c = ctx.bodyAsClass(User.class);
////		boolean added = us.addCustomer(c);
//		if (added) {
//			ctx.json(c);
//			log.trace("Customer " + c.getName() + " was registered");
//		} else {
//			ctx.status(409);
//			log.warn("Account for " + c.getName() + " already exists");
//		}
//	}

	public static void login(Context ctx) {
		log.trace("Attempting to log in");
		if(ctx.sessionAttribute("User") != null) {
			ctx.status(409);
			log.warn("You are already logged in");
			return;
		}
		log.trace("Can we make it here?");
		String name = ctx.formParam("name");
		log.trace ("Did we get the name? " + name);
		User u = us.getUserByName(name);
		if (u == null) {
			log.trace("No user with name " + name + " exists");
			ctx.status(400);
		} else {
			log.trace(name + " was logged in");
			ctx.sessionAttribute("User", u);
			ctx.json(us.getNoticesForUser(u.getName()));  //This needs to display the notices
		}
	}
	
	public static void logout(Context ctx) {
		log.trace("Logging out");
		ctx.req.getSession().invalidate();
	}
	
	public static void moneyRemaining(Context ctx) {
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		log.trace("Attempting to get money remaining for this year");
		Float money = us.moneyRemainingThisYear(u.getName());
		ctx.json(money);
	}
	
	public static void showMyRequests(Context ctx) {
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		List<Request> reqs = us.getAllRequestsForUser(u.getName());
		ctx.json(reqs);
	}
	
	public static void reviewRequests (Context ctx) {
		if (ctx.sessionAttribute("User").equals(null)) {
			ctx.status(401);
			ctx.result("You are not logged in");
			return;
		}
		User u = ctx.sessionAttribute("User");
		List<Integer> ids = u.getRequestsToReviewById();
		List<Request> reqs = new ArrayList<Request>();
		for (Integer id : ids) {
			reqs.add(rs.getRequestById(id));
		}
		ctx.json(reqs);
	}

}
