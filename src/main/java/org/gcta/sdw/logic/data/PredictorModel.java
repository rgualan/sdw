package org.gcta.sdw.logic.data;

import java.util.List;

public class PredictorModel {
	// Columns
	// 0: Date
	// 1..n: Variables
	private List<String> columnNames;
	private List<PredictorEntry> registers;

	public PredictorModel(List<String> columnNames,
			List<PredictorEntry> registers) {
		super();
		this.columnNames = columnNames;
		this.registers = registers;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public List<PredictorEntry> getRegisters() {
		return registers;
	}

	public void setRegisters(List<PredictorEntry> registers) {
		this.registers = registers;
	}

}
