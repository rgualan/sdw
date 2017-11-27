package org.gcta.sdw.persistence.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.gcta.sdw.logic.data.DatasetModel;
import org.gcta.sdw.logic.data.GeneralDatasetEntry;
import org.gcta.sdw.logic.data.GeneralDatasetModel;
import org.gcta.sdw.logic.dataset.ClimaticStandardizer;
import org.gcta.sdw.logic.dataset.TimeSeriesExtractor;
import org.gcta.sdw.persistence.entity.Data;
import org.gcta.sdw.persistence.entity.Dataset;
import org.gcta.sdw.persistence.entity.Datavariable;
import org.gcta.sdw.persistence.entity.Dimension;
import org.gcta.sdw.persistence.entity.Metavariableenum;
import org.gcta.sdw.persistence.entity.Predictand;
import org.gcta.sdw.persistence.entity.Predictor;
import org.gcta.sdw.persistence.entity.PredictorHasPredictors;
import org.gcta.sdw.persistence.entity.Timeserie;
import org.gcta.sdw.persistence.entity.TimeseriePK;
import org.gcta.sdw.util.Constant;
import org.gcta.sdw.util.FormatDates;
import org.gcta.sdw.util.Log;
import org.gcta.sdw.web.viewmodel.data.MyEntry;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.zkoss.util.media.Media;

@Repository
public class DatasetDao {

	@PersistenceContext
	private EntityManager em;

	private TimeSeriesExtractor extractor = new TimeSeriesExtractor();

	@Transactional
	public void insert(Data data) {
		Log.getInstance().info("Inserting data: " + data);
		// Complete data id
		completeDataId(data);

		// Complete variable id
		if (data.getDatavariableList() != null) {
			for (Datavariable var : data.getDatavariableList()) {
				completeDatavariableId(var);
			}
		}

		// Complete dimension id
		if (data.getDimensionList() != null) {
			for (Dimension dim : data.getDimensionList()) {
				completeDimensionId(data, dim);
			}
		}

		em.persist(data);
	}

	@Transactional(readOnly = true)
	public void completeDataId(Data data) {
		Log.getInstance().info("Completing dataId...");
		Log.getInstance().debug(
				"data.dataPk.datasetId> " + data.getDataPK().getDatasetId());
		Log.getInstance().debug(
				">>>>data.dataPk.dataId> " + data.getDataPK().getDataId());

		Integer nextId = em
				.createQuery(
						"select max(a.dataPK.dataId) from Data a where a.dataPK.datasetId = :datasetId",
						Integer.class)
				.setParameter("datasetId", data.getDataPK().getDatasetId())
				.getSingleResult();

		Log.getInstance().debug("Max id from query: " + nextId);

		if (nextId == null) {
			nextId = 1;
		} else {
			nextId++;
		}

		data.getDataPK().setDataId(nextId);
	}

	@Transactional(readOnly = true)
	public void completeDatavariableId(Datavariable var) {
		Log.getInstance().info("Completing datavariableId...");
		Log.getInstance().debug(">>>>data: " + var.getData());

		Data data = var.getData();

		Integer nextId = em
				.createQuery(
						"select max(a.datavariablePK.variableId) from Datavariable a where a.datavariablePK.datasetId = :datasetId and a.datavariablePK.dataId = :dataId",
						Integer.class)
				.setParameter("datasetId", data.getDataPK().getDatasetId())
				.setParameter("dataId", data.getDataPK().getDataId())
				.getSingleResult();

		if (nextId == null) {
			nextId = 1;
		} else {
			nextId++;
		}
		Log.getInstance().debug("Next id: " + nextId);

		var.setData(data);
		var.getDatavariablePK().setDatasetId(data.getDataPK().getDatasetId());
		var.getDatavariablePK().setDataId(data.getDataPK().getDataId());
		var.getDatavariablePK().setVariableId(nextId);
	}

	public void completeDimensionId(Data data, Dimension dim) {
		Log.getInstance().info("Completing dimensionId...");

		dim.setData(data);
		dim.getDimensionPK().setDatasetId(data.getDataPK().getDatasetId());
		dim.getDimensionPK().setDataId(data.getDataPK().getDataId());
	}

	@Transactional
	public void insert(Datavariable variable) {
		Log.getInstance().info("Inserting variable...");
		completeDatavariableId(variable);

		if (variable.getTimeserieList() != null
				&& variable.getTimeserieList().size() > 0) {
			for (Timeserie ts : variable.getTimeserieList()) {
				if (ts.getDatavariable() == null) {
					ts.setDatavariable(variable);
				}
				if (ts.getTimeseriePK() == null) {
					TimeseriePK pk = new TimeseriePK();
					ts.setTimeseriePK(pk);
				}
				ts.getTimeseriePK().setDatasetId(
						variable.getDatavariablePK().getDatasetId());
				ts.getTimeseriePK().setDataId(
						variable.getDatavariablePK().getDataId());
				ts.getTimeseriePK().setVariableId(
						variable.getDatavariablePK().getVariableId());
			}
		}

		em.persist(variable);
	}

	@Transactional(readOnly = true)
	public List<Dataset> queryClimate() {
		Log.getInstance().info("Quering climate datasets...");

		TypedQuery<Dataset> query = em
				.createQuery(
						"Select a from Dataset a where a.datasettype.dsTypeId=:dsTypeId",
						Dataset.class);
		query.setParameter("dsTypeId", 1);
		return query.getResultList();
	}

	public List<Dataset> queryReanalysis() {
		Log.getInstance().info("Quering reanalysis datasets...");

		TypedQuery<Dataset> query = em
				.createQuery(
						"Select a from Dataset a where a.datasettype.dsTypeId=:dsTypeId",
						Dataset.class);
		query.setParameter("dsTypeId", 2);
		return query.getResultList();
	}

	public List<Dataset> queryObservations() {
		Log.getInstance().info("Quering observations datasets...");

		TypedQuery<Dataset> query = em
				.createQuery(
						"Select a from Dataset a where a.datasettype.dsTypeId=:dsTypeId",
						Dataset.class);
		query.setParameter("dsTypeId", 3);
		return query.getResultList();
	}

	public void importTimeSerieFromCsv(Datavariable variable, Media media) {
		Log.getInstance().info("Import time serie from CSV...");

		validateMedia(media, "csv");

		File file;
		try {
			file = saveOnDiskRepostory(media);
			createTimeSerie(variable, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void validateMedia(Media media, String fileExtension) {
		Log.getInstance().info("Validate media...");
		String fileName = media.getName();

		int i = fileName.lastIndexOf(".");
		Log.getInstance().debug("Extension: " + fileName.substring(i + 1));
		if (!(i > 0 && fileName.substring(i + 1).compareToIgnoreCase(
				fileExtension) == 0)) {
			throw new RuntimeException(fileName + " is not a valid "
					+ fileExtension + "file");
		}
		// TODO Do more validations about file type

	}

	private File saveOnDiskRepostory(Media media) throws IOException {
		String folderPath = Constant.REPOSITORY_FOLDER;
		// TODO loard from config.properties

		File folderFile = new File(folderPath);

		if (!folderFile.exists()) {
			folderFile.mkdirs();
		}

		// File
		String fileName = media.getName();

		File gcmFile = new File(folderFile, fileName);

		// Copy
		Files.copy(media.getStreamData(), gcmFile.toPath(),
				java.nio.file.StandardCopyOption.REPLACE_EXISTING);

		return gcmFile;
	}

	private void createTimeSerie(Datavariable variable, File csvFile) {
		Log.getInstance().info("Creating time serie...");

		ICsvListReader listReader = null;
		try {
			// listReader = new CsvListReader(new FileReader(csvFile),
			// CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			listReader = new CsvListReader(new FileReader(csvFile),
					CsvPreference.STANDARD_PREFERENCE);

			listReader.getHeader(true); // skip the header (can't be used with
										// CsvListReader)
			final CellProcessor[] processors = getProcessors();

			if (variable.getTimeserieList() == null) {
				variable.setTimeserieList(new ArrayList<Timeserie>());
			}

			List<Object> parsedItem;
			while ((parsedItem = listReader.read(processors)) != null) {
				// System.out.format("lineNo=%s, rowNo=%s, Item=%s %n",
				// listReader.getLineNumber(), listReader.getRowNumber(),
				// parsedItem);
				Log.getInstance().debug(
						String.format("lineNo=%s, rowNo=%s, Item=%s",
								listReader.getLineNumber(),
								listReader.getRowNumber(), parsedItem));
				TimeseriePK pk = new TimeseriePK();
				// pk.setDatasetId(variable.getDatavariablePK().getDatasetId());
				// pk.setDataId(variable.getDatavariablePK().getDataId());
				// pk.setVariableId(variable.getDatavariablePK().getVariableId());
				pk.setDateTime((Date) parsedItem.get(0));
				// Timeserie item = new Timeserie(variable.getDatavariablePK()
				// .getDatasetId(), variable.getDatavariablePK()
				// .getDataId(), variable.getDatavariablePK()
				// .getVariableId(), (Date) parsedItem.get(0));
				Timeserie item = new Timeserie(pk);
				item.setValue((Double) parsedItem.get(1));
				item.setDatavariable(variable);
				variable.getTimeserieList().add(item);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (listReader != null) {
				try {
					listReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static CellProcessor[] getProcessors() {

		final CellProcessor[] processors = new CellProcessor[] {
				new ParseDate("yyyy-MM-dd"), // date
				new Optional(new ParseDouble()) // value
		};

		return processors;
	}

	public static void main(String[] args) {
		String filePath = "C:/repository/M040-precipitacion.en.csv";
		Datavariable var = new Datavariable(1, 1, 1);
		File csvFile = new File(filePath);

		new DatasetDao().createTimeSerie(var, csvFile);
	}

	public void getAvailableVariables(List<Dataset> datasets,
			Map<Dataset, List<Metavariableenum>> availableVarsMap,
			Map<Metavariableenum, List<Long>> availableLevelsMap) {

		for (Dataset dataset : datasets) {
			Log.getInstance().debug("Available vars from dataset: " + dataset);
			List<Metavariableenum> vars = new ArrayList<Metavariableenum>();

			for (Data data : dataset.getDataList()) {
				Log.getInstance().debug(data);

				for (Datavariable dataVariable : data.getDatavariableList()) {
					Log.getInstance().debug(dataVariable);
					Log.getInstance().debug(dataVariable.getMetavariableenum());
					// Harvest variable
					vars.add(dataVariable.getMetavariableenum());
					// Harvest levels
					List<Long> levelsList = new ArrayList<Long>();
					String levelsStr = dataVariable.getLevels();
					String[] levels = levelsStr.split(",");
					if (dataVariable.getLevelUnits().compareToIgnoreCase(
							"millibar") == 0
							|| dataVariable.getLevelUnits()
									.compareToIgnoreCase("hPa") == 0) {
						for (String lev : levels) {
							Long levelLong = Long.parseLong(lev);
							levelsList.add(levelLong);
						}
					}

					availableLevelsMap.put(dataVariable.getMetavariableenum(),
							levelsList);
				}
			}

			availableVarsMap.put(dataset, vars);
		}

	}

	public void getAvailableVariables(Dataset dataset,
			List<Metavariableenum> availableVars,
			Map<Metavariableenum, List<Long>> availableLevelsMap) {

		List<Metavariableenum> vars = new ArrayList<Metavariableenum>();

		for (Data data : dataset.getDataList()) {
			for (Datavariable dataVariable : data.getDatavariableList()) {
				// Harvest variable
				if (!vars.contains(dataVariable.getMetavariableenum())) {
					vars.add(dataVariable.getMetavariableenum());
				}
				// Harvest levels
				List<Long> levelsList = availableLevelsMap.get(dataVariable
						.getMetavariableenum());
				if (levelsList == null) {
					levelsList = new ArrayList<Long>();
				}
				String levelsStr = dataVariable.getLevels();
				if (levelsStr != null) {
					String[] levels = levelsStr.split(",");
					for (String lev : levels) {
						Long levelLong = Long.parseLong(lev);
						levelLong = ClimaticStandardizer.stdLevel(levelLong,
								dataVariable.getLevelUnits());

						levelsList.add(levelLong);
					}
				} else {
					levelsList.add(0L);
				}

				availableLevelsMap.put(dataVariable.getMetavariableenum(),
						levelsList);
			}
		}

		// Order variables by name
		TreeMap<String, Metavariableenum> orderedMap = new TreeMap<>();
		for (Metavariableenum metavariableenum : vars) {
			orderedMap.put(metavariableenum.getName(), metavariableenum);
		}
		for (String name : orderedMap.keySet()) {
			// Log.getInstance().debug(
			// "Variable " + name + ">" + orderedMap.get(name));
			availableVars.add(orderedMap.get(name));
		}
	}

	public List<SimpleEntry<Metavariableenum, Long>> getAvailableVariables(
			Dataset dataset) {

		List<SimpleEntry<Metavariableenum, Long>> av = new ArrayList<SimpleEntry<Metavariableenum, Long>>();

		for (Data data : dataset.getDataList()) {
			for (Datavariable dataVariable : data.getDatavariableList()) {
				// Harvest variable
				Metavariableenum metaVariable = dataVariable
						.getMetavariableenum();

				// Harvest levels
				List<Long> levelsList = parseLevels(dataVariable);

				for (Long level : levelsList) {
					SimpleEntry<Metavariableenum, Long> entry = new SimpleEntry<Metavariableenum, Long>(
							metaVariable, level);
					av.add(entry);
				}
			}
		}

		return av;
	}

	public List<Metavariableenum> getAvailablePredictandVariables(
			Dataset dataset) {
		List<Metavariableenum> av = new ArrayList<Metavariableenum>();

		for (Data data : dataset.getDataList()) {
			for (Datavariable dataVariable : data.getDatavariableList()) {
				// Harvest variable
				Metavariableenum metaVariable = dataVariable
						.getMetavariableenum();

				if (!av.contains(metaVariable)) {
					av.add(metaVariable);
				}
			}
		}

		return av;
	}

	public List<Data> getAvailableStations(Dataset dataset,
			Metavariableenum selectedPredictandVariable) {
		List<Data> as = new ArrayList<Data>();

		for (Data data : dataset.getDataList()) {
			for (Datavariable dataVariable : data.getDatavariableList()) {
				if (dataVariable.getMetavariableenum().equals(
						selectedPredictandVariable)
						&& !as.contains(data)) {
					as.add(data);
				}
			}
		}

		return as;
	}

	private List<Long> parseLevels(Datavariable dataVariable) {
		List<Long> levelsList = new ArrayList<Long>();

		String levelsStr = dataVariable.getLevels();
		if (levelsStr != null) {
			String[] levels = levelsStr.split(",");
			for (String lev : levels) {
				Long levelLong = Long.parseLong(lev);
				if (!(dataVariable.getLevelUnits().compareToIgnoreCase(
						"millibar") == 0 || dataVariable.getLevelUnits()
						.compareToIgnoreCase("hPa") == 0)) {
					levelLong = levelLong / 100;
				}
				levelsList.add(levelLong);
			}
		} else {
			levelsList.add(0L);
		}

		return levelsList;

	}

	public List<SimpleEntry<Dataset, Boolean>> checkCompatibleGcms(
			Dataset reanalisys,
			List<MyEntry<Metavariableenum, Long>> selectedVars) {
		List<Dataset> climates = queryClimate();

		List<SimpleEntry<Dataset, Boolean>> compatibles = new ArrayList<SimpleEntry<Dataset, Boolean>>();

		for (Dataset climate : climates) {
			SimpleEntry<Dataset, Boolean> compatibleEntry = new SimpleEntry<Dataset, Boolean>(
					climate, true);
			compatibles.add(compatibleEntry);

			// Time periodicity
			if (climate.getTimeperiodicity() != null
					&& reanalisys.getTimeperiodicity() != null) {
				if (climate.getTimeperiodicity().equals(
						reanalisys.getTimeperiodicity())) {
					compatibleEntry.setValue(false);
					continue;
				}
			}

			// Spatial resolution
			// TODO

			// Variable-Levels
			List<SimpleEntry<Metavariableenum, Long>> av = getAvailableVariables(climate);

			if (!isCompatible(selectedVars, av)) {
				compatibleEntry.setValue(false);
				continue;
			}
		}

		return compatibles;
	}

	private boolean isCompatible(
			List<MyEntry<Metavariableenum, Long>> selectedVars,
			List<SimpleEntry<Metavariableenum, Long>> av) {

		for (MyEntry<Metavariableenum, Long> selectedEntry : selectedVars) {
			SimpleEntry<Metavariableenum, Long> temp = new SimpleEntry<Metavariableenum, Long>(
					selectedEntry.getValue0(), selectedEntry.getValue1());

			if (!av.contains(temp)) {
				return false;
			}
		}

		return true;
	}

	public List<SimpleEntry<Dataset, Boolean>> checkCompatibleObsDatasets(
			Date startDate, Date endDate) {

		List<Dataset> obsDatasets = queryObservations();

		List<SimpleEntry<Dataset, Boolean>> compatibles = new ArrayList<SimpleEntry<Dataset, Boolean>>();

		for (Dataset obsDs : obsDatasets) {
			SimpleEntry<Dataset, Boolean> compatibleEntry = new SimpleEntry<Dataset, Boolean>(
					obsDs, false);
			compatibles.add(compatibleEntry);

			// Stations - variables
			loops: for (Data station : obsDs.getDataList()) {
				for (Datavariable var : station.getDatavariableList()) {
					if (var.getFromDate().compareTo(startDate) <= 0
							&& var.getToDate().compareTo(endDate) >= 0) {
						compatibleEntry.setValue(true);
						break loops;
					}

				}
			}

		}

		return compatibles;
	}

	public Map<Data, DatasetModel> loadTrainDataset(Predictor predictor,
			Predictand predictand) throws Exception {
		Log.getInstance().debug("Predictor: " + predictor);
		Log.getInstance().debug("Predictand: " + predictand);
		Log.getInstance().debug("Stations: " + predictand.getDataList());

		Map<Data, DatasetModel> mapDatasets = new HashMap<Data, DatasetModel>();
		DatasetModel trainDataset = null;

		for (Data station : predictand.getDataList()) {
			// Loading predictor dataset
			// Set columns
			List<String> columnNames = new ArrayList<String>();
			columnNames.add("date");

			for (PredictorHasPredictors php : predictor
					.getPredictorHasPredictorsList()) {
				// zg-300 zg-700 slp ...
				String columnName = php.getPredictorHasPredictorsPK()
						.getStdVarId();
				if (php.getPredictorHasPredictorsPK().getLevel() > 0) { // TODO
					columnName += "-"
							+ php.getPredictorHasPredictorsPK().getLevel();
				}

				columnName += " ["
						+ ClimaticStandardizer.getStdUnit(php
								.getMetavariableenum()) + "]";
				columnNames.add(columnName);
			}

			// Get time series
			List<List<SimpleEntry<Date, Double>>> timeSeries = new ArrayList<List<SimpleEntry<Date, Double>>>();
			Dataset datasetRe = predictor.getDataset();

			for (PredictorHasPredictors php : predictor
					.getPredictorHasPredictorsList()) {
				Data data = findDataByMetavariable(datasetRe,
						php.getMetavariableenum(), null);
				// TODO Do not force null
				// It will fail with other datasets

				// Extract original data
				List<SimpleEntry<Date, Double>> timeSerie0 = extractor
						.getTimeSerie(data.getFilePath(),
								predictor.getStartDate(),
								predictor.getEndDate(), station.getLatitude(),
								station.getLongitude(), data
										.getDatavariableList().get(0)
										.getShortName(), ""
										+ php.getPredictorHasPredictorsPK()
												.getLevel());

				// Standardize data
				List<SimpleEntry<Date, Double>> timeSerie1 = ClimaticStandardizer
						.stdVariable(timeSerie0, data.getDatavariableList()
								.get(0).getMetavariableenum(), data
								.getDatavariableList().get(0).getUnits());

				// Aggregate data - not required

				timeSeries.add(timeSerie1);
			}

			List<GeneralDatasetEntry> aEntries = packDatasetData(timeSeries);

			GeneralDatasetModel predictorDataset = new GeneralDatasetModel(
					columnNames, aEntries);

			// Loading predictand dataset

			// Set columns
			List<String> bcolumnNames = new ArrayList<String>();
			bcolumnNames.add("date");
			bcolumnNames.add(predictand.getMetavariableenum().getName()
					+ " ["
					+ ClimaticStandardizer.getStdUnit(predictand
							.getMetavariableenum()) + "]");

			// Get time serie
			Datavariable variable = findVariableByMetavariable(station,
					predictand.getMetavariableenum());
			// Log.getInstance().debug("Variable>>> " + variable);

			// Get original time serie
			List<SimpleEntry<Date, Double>> obsTimeSerie0 = getObservationTimeSerie(
					variable, predictor.getStartDate(), predictor.getEndDate());

			// Standardize time serie
			List<SimpleEntry<Date, Double>> obsTimeSerie1 = ClimaticStandardizer
					.stdVariable(obsTimeSerie0, variable.getMetavariableenum(),
							variable.getUnits());

			// TODO Maybe agregate

			List<GeneralDatasetEntry> bEntries = packDatasetDataSimple(obsTimeSerie1);
			GeneralDatasetModel predictandDataset = new GeneralDatasetModel(
					bcolumnNames, bEntries);

			// Build train dataset
			Log.getInstance().debug("Actual station: " + station);
			trainDataset = new DatasetModel(predictorDataset, predictandDataset);
			mapDatasets.put(station, trainDataset);
		}

		return mapDatasets;
	}

	public Map<Data, GeneralDatasetModel> loadFutureDataset(
			Predictor predictor, Predictand predictand, Dataset gcm, Date from,
			Date to) throws Exception {

		// A map is used because time series are extracted for several
		// points/stations
		Map<Data, GeneralDatasetModel> mapDatasets = new HashMap<Data, GeneralDatasetModel>();
		GeneralDatasetModel futureDataset = null;

		for (Data station : predictand.getDataList()) {
			// Build dataset
			// Set columns
			// 1) Date
			// 2) Predictor variables
			List<String> columnNames = new ArrayList<String>();
			columnNames.add("date");

			for (PredictorHasPredictors php : predictor
					.getPredictorHasPredictorsList()) {
				// zg-300, zg-700, slp, ...
				String columnName = php.getPredictorHasPredictorsPK()
						.getStdVarId();
				if (php.getPredictorHasPredictorsPK().getLevel() > 0) { // TODO
					columnName += "-"
							+ php.getPredictorHasPredictorsPK().getLevel();
				}
				columnNames.add(columnName);
			}

			// Get time series
			List<List<SimpleEntry<Date, Double>>> timeSeries = new ArrayList<List<SimpleEntry<Date, Double>>>();

			for (PredictorHasPredictors php : predictor
					.getPredictorHasPredictorsList()) {

				Data data = findDataByMetavariable(gcm,
						php.getMetavariableenum(), php
								.getPredictorHasPredictorsPK().getLevel());

				if (data == null) {
					throw new RuntimeException("Data not found for variable: "
							+ php.getMetavariableenum() + "-"
							+ php.getPredictorHasPredictorsPK().getLevel());
				}

				// Extracted data
				List<SimpleEntry<Date, Double>> timeSerie0 = extractor
						.getTimeSerie(data.getFilePath(), from, to,
								station.getLatitude(), station.getLongitude(),
								data.getDatavariableList().get(0)
										.getShortName(), ""
										+ php.getPredictorHasPredictorsPK()
												.getLevel());

				// Standardized data
				List<SimpleEntry<Date, Double>> timeSerie1 = ClimaticStandardizer
						.stdVariable(timeSerie0, data.getDatavariableList()
								.get(0).getMetavariableenum(), data
								.getDatavariableList().get(0).getUnits());

				// Aggregate data
				List<SimpleEntry<Date, Double>> timeSerie = null;
				if (predictor.getDataset().getTimeperiodicity()
						.equals(gcm.getTimeperiodicity())) {
					timeSerie = timeSerie1;
				} else {
					// Aggregate
					// TODO Differentiate aggregation type per variable
					Log.getInstance().debug("Aggregating information...");
					timeSerie = TimeSeriesExtractor.aggregateTimeSerie(
							timeSerie1, predictor.getDataset()
									.getTimeperiodicity());

				}

				timeSeries.add(timeSerie);
			}

			List<GeneralDatasetEntry> aEntries = packDatasetData(timeSeries);

			futureDataset = new GeneralDatasetModel(columnNames, aEntries);

			mapDatasets.put(station, futureDataset);
		}

		return mapDatasets;
	}

	private List<SimpleEntry<Date, Double>> getObservationTimeSerie(
			Datavariable variable, Date startDate, Date endDate) {

		TypedQuery<Timeserie> query = em
				.createQuery(
						"Select a from Timeserie a "
								+ "where a.timeseriePK.datasetId = :datasetId "
								+ "and a.timeseriePK.dataId = :dataId "
								+ "and a.timeseriePK.variableId = :variableId "
								+ "and a.timeseriePK.dateTime >= :dateFrom "
								+ "and a.timeseriePK.dateTime <= :dateTo "
								+ "order by a.timeseriePK.datasetId,a.timeseriePK.dataId,a.timeseriePK.variableId,a.timeseriePK.dateTime",
						Timeserie.class);
		query.setParameter("datasetId", variable.getDatavariablePK()
				.getDatasetId());
		query.setParameter("dataId", variable.getDatavariablePK().getDataId());
		query.setParameter("variableId", variable.getDatavariablePK()
				.getVariableId());
		query.setParameter("dateFrom", startDate);
		query.setParameter("dateTo", endDate);

		List<Timeserie> timeSerie = query.getResultList();
		List<SimpleEntry<Date, Double>> timeSerie2 = new ArrayList<SimpleEntry<Date, Double>>();

		for (Timeserie ts : timeSerie) {
			timeSerie2.add(new SimpleEntry<Date, Double>(ts.getTimeseriePK()
					.getDateTime(), ts.getValue()));
		}

		return timeSerie2;
	}

	private List<GeneralDatasetEntry> packDatasetData(
			List<List<SimpleEntry<Date, Double>>> timeSeries) {

		List<GeneralDatasetEntry> entries = new ArrayList<GeneralDatasetEntry>();

		Log.getInstance().debug(
				"Packing dataset. Number of lists to pack: "
						+ timeSeries.size());

		// Validate sizes
		int size = timeSeries.get(0).size();

		for (List<SimpleEntry<Date, Double>> ts : timeSeries) {
			if (ts.size() != size) {
				throw new RuntimeException(
						"Sizes of time series are note equal");
			}
		}

		// TODO Validate dates

		for (int i = 0; i < size; i++) {
			Date date = timeSeries.get(0).get(i).getKey();
			List<Double> values = new ArrayList<Double>();
			for (List<SimpleEntry<Date, Double>> ts : timeSeries) {
				values.add(ts.get(i).getValue());
			}
			GeneralDatasetEntry entry = new GeneralDatasetEntry(date, values);
			entries.add(entry);
		}

		return entries;
	}

	private List<GeneralDatasetEntry> packDatasetDataSimple(
			List<SimpleEntry<Date, Double>> timeSerie) {

		List<GeneralDatasetEntry> entries = new ArrayList<GeneralDatasetEntry>();

		for (int i = 0; i < timeSerie.size(); i++) {
			Date date = timeSerie.get(i).getKey();
			List<Double> values = new ArrayList<Double>();
			values.add(timeSerie.get(i).getValue());
			GeneralDatasetEntry entry = new GeneralDatasetEntry(date, values);
			entries.add(entry);
		}

		return entries;
	}

	public Data findDataByMetavariable(Dataset dataset,
			Metavariableenum metaVariable, Long level) {
		// Log.getInstance().debug("Finding data by metavariable...");
		// Log.getInstance().debug("Metavariable: " + metaVariable);
		// Log.getInstance().debug("Level: " + level);

		// It is assumed that data names are standardized
		// Two cases
		// 1) A data variable with multiple levels
		// 2) A data variable with only one level

		for (Data d : dataset.getDataList()) {
			for (Datavariable dataVar : d.getDatavariableList()) {
				if (dataVar.getMetavariableenum().equals(metaVariable)) {
					if (level == null || level == 0) {
						return d;
					} else {
						String levelStr = dataVar.getLevels();
						String[] levels = levelStr.split(",");
						for (String l : levels) {
							Long aLevel = ClimaticStandardizer.stdLevel(l,
									dataVar.getLevelUnits());
							// Log.getInstance().debug("Comparing: " + aLevel);
							if (aLevel.compareTo(level) == 0) {
								return d;
							}
						}

					}
				}
			}
		}

		return null;
	}

	public Datavariable findVariableByMetavariable(Data data,
			Metavariableenum metaVariable) {
		// for (Data d : dataset.getDataList()) {
		for (Datavariable dataVar : data.getDatavariableList()) {
			if (dataVar.getMetavariableenum().equals(metaVariable)) {
				return dataVar;
			}
		}
		// }

		return null;
	}

	public List<String> loadAnnTransferFcns() throws Exception {
		List<String> transferFcns = new ArrayList<>();

		File file = new File(Constant.ANN_HOME
				+ "/config/transferFunctions.txt");
		FileReader fr = new FileReader(file);
		BufferedReader br = null;
		try {
			br = new BufferedReader(fr);

			String line = null;
			while ((line = br.readLine()) != null) {
				String field[] = line.split(",");
				if (field.length >= 1) {
					String tf = field[0].trim();
					transferFcns.add(tf);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
		}

		return transferFcns;
	}

	public List<String> loadAnnDefaultParameters() throws Exception {
		List<String> defaultParameters = new ArrayList<>();

		String paramsFile = Constant.ANN_HOME + "/config/params_ann.txt";
		File file = new File(paramsFile);
		FileReader fr = new FileReader(file);
		BufferedReader br = null;
		try {
			br = new BufferedReader(fr);

			String line = br.readLine();
			String field[] = line.split(",");
			if (field.length >= 5) {
				for (String f : field) {
					String param = f.trim();
					defaultParameters.add(param);
				}
			} else {
				throw new RuntimeException("Error reading default parameters: "
						+ paramsFile);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
		}

		return defaultParameters;
	}

	public File exportDatasetToCsv(GeneralDatasetModel generalDatasetModel,
			String name) throws Exception {
		// Temporal folder
		File tempFolder = new File(Constant.ANN_HOME + "/tmp");
		;
		if (!tempFolder.exists()) {
			tempFolder.mkdirs();
		}

		File file = new File(tempFolder, name);

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));

			// Write columns
			String line = "";
			for (String column : generalDatasetModel.getColumnNames()) {
				if (!line.isEmpty()) {
					line += ",";
				}
				line += column;
			}
			writer.write(line);
			writer.newLine();

			// Write registers
			for (GeneralDatasetEntry entry : generalDatasetModel.getEntries()) {
				line = FormatDates.getDateFormat().format(entry.getDate());
				for (Double val : entry.getValues()) {
					line += ",";
					line += val;
				}
				writer.write(line);
				writer.newLine();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

		return file;
	}

	public List<String> loadSvmKernels() throws Exception {
		List<String> kernels = new ArrayList<>();

		File file = new File(Constant.SVM_HOME + "/config/kernels.txt");
		FileReader fr = new FileReader(file);
		BufferedReader br = null;
		try {
			br = new BufferedReader(fr);

			String line = null;
			while ((line = br.readLine()) != null) {
				// String field[] = line.split(",");
				// if (field.length >= 1) {
				// String tf = field[0].trim();
				// kernels.add(tf);
				// }
				kernels.add(line.trim());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
		}

		return kernels;
	}

	public List<String> loadSvmDefaultParameters() throws Exception {
		List<String> defaultParameters = new ArrayList<>();

		String paramsFile = Constant.SVM_HOME + "/config/params_svm.txt";
		File file = new File(paramsFile);
		FileReader fr = new FileReader(file);
		BufferedReader br = null;
		try {
			br = new BufferedReader(fr);

			String line = br.readLine();
			String field[] = line.split(",");
			// if (field.length != 1) {
			for (String f : field) {
				String param = f.trim();
				defaultParameters.add(param);
			}
			// } else {
			// throw new RuntimeException("Error reading default parameters: "
			// + paramsFile);
			// }
		} catch (Exception e) {
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
		}

		return defaultParameters;
	}
}