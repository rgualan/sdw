package org.gcta.sdw.logic.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.gcta.sdw.util.Log;

/**
 * It is a more specific dataset model comprised of two associated datasets: a
 * predictor and a predictand Both datasets have the same number of rows and the
 * same dates but they can have different columns
 * 
 * Usually the predictor has several columns while the predictand ony one in
 * addition to the date column
 * 
 * @author Ronald
 * 
 */
public class DatasetModel extends GeneralDatasetModel {
	private GeneralDatasetModel predictor;
	private GeneralDatasetModel predictand;
	private GeneralDatasetModel modeled;

	public DatasetModel(GeneralDatasetModel predictor,
			GeneralDatasetModel predictand) {
		this.predictor = predictor;
		this.predictand = predictand;

		// Validations
		if (predictor == null || predictand == null) {
			throw new RuntimeException("Predictor or predictand is empty");
		}

		if (predictor.getEntries() == null || predictand.getEntries() == null) {
			throw new RuntimeException(
					"Predictor or predictand entries are empty");
		}

		if (predictor.getEntries().size() != predictand.getEntries().size()) {

			Log.getInstance().debug("Predictor");
			Log.getInstance().debug("Columns " + predictor.getColumnNames());
			Log.getInstance().debug("Size " + predictor.getEntries().size());
			Log.getInstance().debug(
					"First " + predictor.getEntries().get(0).getDate() + "-"
							+ predictor.getEntries().get(0).getValues());
			Log.getInstance().debug(
					"Last"
							+ predictor.getEntries()
									.get(predictor.getEntries().size() - 1)
									.getDate()
							+ "-"
							+ predictor.getEntries()
									.get(predictor.getEntries().size() - 1)
									.getValues());

			Log.getInstance().debug("Predictand");
			Log.getInstance().debug("Columns " + predictand.getColumnNames());
			Log.getInstance().debug("Size " + predictand.getEntries().size());
			Log.getInstance().debug(
					"First " + predictand.getEntries().get(0).getDate() + "-"
							+ predictand.getEntries().get(0).getValues());
			Log.getInstance().debug(
					"Last"
							+ predictand.getEntries()
									.get(predictand.getEntries().size() - 1)
									.getDate()
							+ "-"
							+ predictand.getEntries()
									.get(predictand.getEntries().size() - 1)
									.getValues());

			// try {
			// Thread.sleep(60000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			throw new RuntimeException(
					"Predictor size is not equal than predictand size");
		}

		// Consolidate columns
		List<String> columns = new ArrayList<String>();
		if (predictor.getColumnNames() != null
				&& predictand.getColumnNames() != null) {
			// TODO: Actually I'm assuming both datasets (predictor and
			// predictand) have a data column,
			// and the name of the date column is the same in both of them

			if (predictor.getColumnNames().get(0).startsWith("date")
					&& predictand.getColumnNames().get(0).startsWith("date")) {
				columns.addAll(predictor.getColumnNames());
				columns.addAll(predictand.getColumnNames().subList(1,
						predictand.getColumnNames().size()));
			} else {
				columns.addAll(predictor.getColumnNames());
				columns.addAll(predictand.getColumnNames());
			}

		}
		setColumnNames(columns);

		// Consolidate entries
		int size = predictor.getEntries().size();
		List<GeneralDatasetEntry> entries = new ArrayList<GeneralDatasetEntry>();
		for (int i = 0; i < size; i++) {
			Date dateA = predictor.getEntries().get(i).getDate();
			Date dateB = predictand.getEntries().get(i).getDate();

			// TODO Check why is necessary truncate the dates
			dateA = truncDate(dateA);
			dateB = truncDate(dateB);

			if (dateA.compareTo(dateB) != 0) {
				throw new RuntimeException("Uncompatible dates on entry: " + i
						+ " > " + dateA + " <> " + dateB);
			}

			List<Double> entryValues = new ArrayList<Double>();
			entryValues.addAll(predictor.getEntries().get(i).getValues());
			entryValues.addAll(predictand.getEntries().get(i).getValues());
			GeneralDatasetEntry datasetEntry = new GeneralDatasetEntry(dateA,
					entryValues);
			entries.add(datasetEntry);
		}
		setEntries(entries);
	}

	public void addDataset(GeneralDatasetModel modeledData) {
		modeled = modeledData;

		// Validations
		if (modeled == null) {
			throw new RuntimeException("Modeled dataset is null");
		}

		if (modeled.getEntries() == null) {
			throw new RuntimeException("Modeled dataset entries are null");
		}

		if (modeled.getEntries().size() != predictor.getEntries().size()) {
			throw new RuntimeException("Incompatible modeled dataset size");
		}

		// Consolidate columns
		// I'm assuming that modeled dataset has only one column.
		// The column is related with the modeled data
		getColumnNames().addAll(modeled.getColumnNames());

		// Consolidate entries
		for (int i = 0; i < getEntries().size(); i++) {
			GeneralDatasetEntry entry = getEntries().get(i);
			GeneralDatasetEntry entryModeled = modeled.getEntries().get(i);
			entry.getValues().addAll(entryModeled.getValues());
		}
	}

	private Date truncDate(Date date) {
		Calendar cal = Calendar.getInstance(); // locale-specific
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return new Date(cal.getTimeInMillis());
	}

	public GeneralDatasetModel getPredictor() {
		return predictor;
	}

	public void setPredictor(GeneralDatasetModel predictor) {
		this.predictor = predictor;
	}

	public GeneralDatasetModel getPredictand() {
		return predictand;
	}

	public void setPredictand(GeneralDatasetModel predictand) {
		this.predictand = predictand;
	}

	/*
	public Object[][] toMatrix() {
		Object[][] mpredictor = predictor.toMatrix();
		Object[][] mpredictand = predictand.toMatrix();

		Log.getInstance().debug("Matriz predictor: ");
		Log.getInstance().debug("size: " + mpredictor.length);
		for (Object object : mpredictor[0]) {
			System.out.print(object + " ");
		}
		System.out.println();
		for (Object object : mpredictor[mpredictor.length - 1]) {
			System.out.print(object + " ");
		}
		System.out.println();

		Log.getInstance().debug("Matriz predictand: ");
		Log.getInstance().debug("size: " + mpredictand.length);
		for (Object object : mpredictand[0]) {
			System.out.print(object + " ");
		}
		System.out.println();
		for (Object object : mpredictand[mpredictand.length - 1]) {
			System.out.print(object + " ");
		}
		System.out.println();

		if (mpredictor.length != mpredictand.length) {
			throw new RuntimeException(
					"The size of the matrices is not compatible!");
		}

		int cols = mpredictor[0].length + mpredictand[0].length - 1;
		Object[][] matrix = new Object[mpredictor.length][];

		for (int i = 0; i < mpredictor.length; i++) {
			Object[] row = new Object[cols];
			for (int j = 0; j < mpredictor[0].length; j++) {
				row[j] = mpredictor[i][j];
			}

			int m = mpredictor[0].length;
			for (int j = 1; j < mpredictand[0].length; j++) {
				row[m++] = mpredictand[i][j];
			}
			matrix[i] = row;
		}

		Log.getInstance().debug("Union matrix: ");
		Log.getInstance().debug("size: " + matrix.length);
		Log.getInstance().debug("First 5 elements: ");
		for (int i = 0; i < 5; i++) {
			for (Object object : matrix[i]) {
				System.out.print(object + " ");
			}
			System.out.println();
		}
		Log.getInstance().debug("Last 5 elements: ");
		for (int i = matrix.length - 6; i < matrix.length; i++) {
			for (Object object : matrix[i]) {
				System.out.print(object + " ");
			}
			System.out.println();
		}

		return matrix;
	}
	*/
}
