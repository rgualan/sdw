package org.gcta.sdw.logic.data;

import java.util.List;

public class PredictandModel {
	// Columns
	// 0: Date
	// 1: Variable
	private String columnName;
	private List<PredictandEntry> registers;

	public PredictandModel(String columnName, List<PredictandEntry> registers) {
		super();
		this.columnName = columnName;
		this.registers = registers;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public List<PredictandEntry> getRegisters() {
		return registers;
	}

	public void setRegisters(List<PredictandEntry> registers) {
		this.registers = registers;
	}

}
