package com.revature.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;

public class CassandraUtil {
	private static CassandraUtil instance = null;
	private static final Logger log = LogManager.getLogger(CassandraUtil.class);
	
	private CqlSession session = null;
	
	private CassandraUtil() {
		log.trace("Establishing connection with Cassandra");
		DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
		try {
			this.session = CqlSession.builder().withConfigLoader(loader).withKeyspace("trms").build();
		} catch(Exception e) {
			log.error("Method threw exception: "+e);
			for(StackTraceElement s : e.getStackTrace()) {
				log.warn(s);
			}
			throw e;
		}
	}
	
	public static synchronized CassandraUtil getInstance() {
		if(instance == null) {
			instance = new CassandraUtil();
		}
		return instance;
	}
	
	public CqlSession getSession() {
		return session;
	}
}

