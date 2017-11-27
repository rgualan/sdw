package org.gcta.sdw.logic.enums;

public enum TimePeriodicityEnum {
	INSTANT("00"), DAILY("daily"), WEEKLY("weekly"), MONTLY("monthly");

	String id;

	TimePeriodicityEnum(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
