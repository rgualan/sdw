package org.gcta.sdw.logic.data;

import java.util.Date;
import java.util.List;

public class PredictorEntry {
	private Date date;
	private List<Double> values;

	public PredictorEntry(Date date, List<Double> values) {
		this.date = date;
		this.values = values;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Double> getValues() {
		return values;
	}

	public void setValues(List<Double> values) {
		this.values = values;
	}
}
