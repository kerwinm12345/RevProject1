package com.revature.beans;

public enum EventType {
	UNIVERSITYCOURSE (80), 
	SEMINAR (60), 
	CERTIFICATIONPREP (75), 
	CERTIFICATION (100), 
	TECHNICALTRAINING (90), 
	OTHER(30) ;
	
	private float value;
	
	EventType(float d) {
		this.value = d;
	}
	
	public float getValue() {
		return value;
	}
}
