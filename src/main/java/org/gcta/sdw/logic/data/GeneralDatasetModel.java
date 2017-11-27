package org.gcta.sdw.logic.data;

import java.util.List;

/**
 * General data model for matrix data Defines a List of columns (names)
 * including a date as first column and a list of rows defined by
 * GeneralDatasetEntry
 * 
 * @author Ronald
 *
 */
public class GeneralDatasetModel {
	// Columns
	// 0: Date
	// 1..n: Variables
	private List<String> columnNames;
	private List<GeneralDatasetEntry> entries;

	public GeneralDatasetModel() {
		super();
	}

	public GeneralDatasetModel(List<String> columnNames,
			List<GeneralDatasetEntry> entries) {
		super();
		this.columnNames = columnNames;
		this.entries = entries;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public List<GeneralDatasetEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<GeneralDatasetEntry> entries) {
		this.entries = entries;
	}

	public Object[][] toMatrix() {
		if (entries == null || entries.size() == 0) {
			return null;
		}

		Object[][] matrix = new Object[entries.size()][];
		for (int i = 0; i < entries.size(); i++) {
			GeneralDatasetEntry entry = entries.get(i);
			Object[] row = new Object[entries.get(0).getValues().size() + 1];
			row[0] = entry.getDate();
			for (int j = 0; j < entry.getValues().size(); j++) {
				row[j + 1] = entry.getValues().get(j);
			}
			matrix[i] = row;
		}

		return matrix;
	}

	public void addDataset(GeneralDatasetModel model) {
		// Validations
		if (model == null) {
			throw new RuntimeException("Dataset is null");
		}

		if (model.getEntries() == null) {
			throw new RuntimeException("Dataset entries are null");
		}

		if (model.getEntries().size() != getEntries().size()) {
			throw new RuntimeException("Incompatible dataset sizes");
		}

		// Consolidate columns
		// I'm assuming that modeled dataset has only one column.
		// The column is related with the modeled data
		getColumnNames().addAll(model.getColumnNames());

		// Consolidate entries
		for (int i = 0; i < getEntries().size(); i++) {
			GeneralDatasetEntry entry = getEntries().get(i);
			GeneralDatasetEntry entry2 = model.getEntries().get(i);
			entry.getValues().addAll(entry2.getValues());
		}
	}

}
