package org.gcta.sdw.logic.data;

import java.util.Date;

public class PredictandEntry {
	private Date date;
	private Double value;

	public PredictandEntry(Date date, Double value) {
		super();
		this.date = date;
		this.value = value;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
