package org.gcta.sdw.logic.services.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gcta.sdw.logic.dataset.ClimaticStandardizer;
import org.gcta.sdw.logic.enums.DimensionEnum;
import org.gcta.sdw.logic.enums.TimePeriodicityEnum;
import org.gcta.sdw.logic.services.EtlService;
import org.gcta.sdw.logic.services.PersistenceService;
import org.gcta.sdw.persistence.dao.DatasetDao;
import org.gcta.sdw.persistence.entity.Data;
import org.gcta.sdw.persistence.entity.DataPK;
import org.gcta.sdw.persistence.entity.Dataset;
import org.gcta.sdw.persistence.entity.Datavariable;
import org.gcta.sdw.persistence.entity.DatavariablePK;
import org.gcta.sdw.persistence.entity.DimensionPK;
import org.gcta.sdw.persistence.entity.Metavariableenum;
import org.gcta.sdw.persistence.entity.Timeperiodicity;
import org.gcta.sdw.persistence.entity.Timeserie;
import org.gcta.sdw.util.Constant;
import org.gcta.sdw.util.FormatDates;
import org.gcta.sdw.util.Log;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.Months;
import org.joda.time.PeriodType;
import org.joda.time.ReadablePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.zkoss.util.media.Media;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

@Service("etlService")
@Scope
public class EtlServiceImpl implements EtlService {

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private DatasetDao datasetDao;

	private Logger log = Log.getInstance();

	// private static SimpleDateFormat sdf = FormatDates.getTimeFormat();

	private Map<String, Metavariableenum> metaVariablesMap;

	@Override
	public void saveNetcdfFile(Dataset selectedDataset, Media media)
			throws Exception {
		// Validate that media contains a Netcdf file (.nc)
		validateMedia(media.getName());

		// Copy file to repository
		File gcmFile = saveOnDiskRepository(selectedDataset, media);

		// save on Database
		saveOnDb(selectedDataset, gcmFile);

	}

	public void saveNetcdfFile(Dataset dataset, File file) throws Exception {
		// Copy file to repository
		String subFolder = "" + dataset.getDatasetId();
		File folderFile = new File(Constant.REPOSITORY_FOLDER + "/" + subFolder);
		if (!folderFile.exists()) {
			folderFile.mkdirs();
		}
		File gcmFile = new File(folderFile, file.getName());
		Files.copy(file.toPath(), gcmFile.toPath(),
				StandardCopyOption.REPLACE_EXISTING);

		// save on Database
		saveOnDb(dataset, gcmFile);
	}

	private void validateMedia(String fileName) {
		// = media.getName();

		int i = fileName.lastIndexOf(".");
		log.debug("Extension: " + fileName.substring(i + 1));
		if (!(i > 0
				&& (fileName.substring(i + 1).compareToIgnoreCase("nc") == 0) || (fileName
				.substring(i + 1).compareToIgnoreCase("grib") == 0))) {
			throw new RuntimeException(fileName + " is not a nc file");
		}
		// TODO Do more validations about file type

	}

	private File saveOnDiskRepository(Dataset dataset, Media media)
			throws Exception {
		// Folder
		// String folderPath = PropsUtil.getConfigProperties().getProperty(
		// "repository");

		// TODO loard from config.properties

		String subFolder = "" + dataset.getDatasetId();

		File folderFile = new File(Constant.REPOSITORY_FOLDER + "/" + subFolder);

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

	private void saveOnDb(Dataset dataset, File gcmFile) throws Exception {
		// Create data/datavariables from gcmFile
		Data data = createData(dataset, gcmFile);

		// Set metadata
		setMetadata(dataset, data);

		// Persist data
		datasetDao.insert(data);
		persistenceService.update(dataset);

	}

	private void setMetadata(Dataset dataset, Data data) throws Exception {
		// Dimension map of actual data
		// Map of dimensions by name
		Map<String, org.gcta.sdw.persistence.entity.Dimension> mapDim = new HashMap<String, org.gcta.sdw.persistence.entity.Dimension>();
		for (org.gcta.sdw.persistence.entity.Dimension dim : data
				.getDimensionList()) {
			mapDim.put(dim.getDimensionPK().getName(), dim);
		}

		// Set time resolution
		Date dateA0 = dataset.getDateA();
		Date dateB0 = dataset.getDateB();
		Timeperiodicity timePer0 = dataset.getTimeperiodicity();
		Log.getInstance().debug("DateA0: " + dateA0);
		Log.getInstance().debug("DateB0: " + dateB0);
		Log.getInstance().debug("timePer0: " + timePer0);

		Date dateA1 = mapDim.get(DimensionEnum.TIME.getDimension()).getADate();
		Date dateB1 = mapDim.get(DimensionEnum.TIME.getDimension()).getBDate();
		Timeperiodicity timePer1 = parseTimePeriodicity(
				mapDim.get(DimensionEnum.TIME.getDimension()).getDeltaDate(),
				mapDim.get(DimensionEnum.TIME.getDimension())
						.getUnitsDeltaDate());

		dateA1 = truncDate(dateA1);
		dateB1 = truncDate(dateB1);

		Log.getInstance().debug("DateA1: " + dateA1);
		Log.getInstance().debug("DateB1: " + dateB1);
		Log.getInstance().debug("timePer1: " + timePer1);

		if (dateA0 == null) {
			// Assume first time
			Log.getInstance().info(
					"Setting time resolution in dataset - first time...");
			dataset.setDateA(dateA1);
			dataset.setDateB(dateB1);
			dataset.setTimeperiodicity(timePer1);
		} else {
			// Validate that resolution is the same
			if (dateA0.compareTo(dateA1) == 0 && dateB0.compareTo(dateB1) == 0
					&& timePer0.getTimeId().equals(timePer1.getTimeId())) {
				// It's ok
			} else {
				throw new Exception(
						"The time resolution is not the same of the dataset... ");
			}
		}

		// Set spatial resolution
		// Longitude
		Double longA0 = dataset.getLongA();
		Double longB0 = dataset.getLongB();
		Double longDelta0 = dataset.getLongDelta();
		Double longA1 = mapDim.get(DimensionEnum.LONGITUDE.getDimension())
				.getA();
		Double longB1 = mapDim.get(DimensionEnum.LONGITUDE.getDimension())
				.getB();
		Double longDelta1 = mapDim.get(DimensionEnum.LONGITUDE.getDimension())
				.getDelta();

		if (longA0 == null) {
			// Assume firt time
			dataset.setLongA(longA1);
			dataset.setLongB(longB1);
			dataset.setLongDelta(longDelta1);
		} else {
			// Validate that spatial resolution is the same
			if (longA0.compareTo(longA1) == 0 && longB0.compareTo(longB1) == 0
					&& longDelta0.compareTo(longDelta1) == 0) {
				// It's ok
			} else {
				Log.getInstance().debug("LatA0: " + longA0);
				Log.getInstance().debug("LatB0: " + longB0);
				Log.getInstance().debug("LatDelta0: " + longDelta0);
				Log.getInstance().debug("LatA1: " + longA1);
				Log.getInstance().debug("LatB1: " + longB1);
				Log.getInstance().debug("LatDelta1: " + longDelta1);
				throw new Exception(
						"The spatial resolution is not the same of the dataset... ");
			}
		}

		// Latitude
		Double latA0 = dataset.getLatA();
		Double latB0 = dataset.getLatB();
		Double latDelta0 = dataset.getLatDelta();
		Double latA1 = mapDim.get(DimensionEnum.LATITUDE.getDimension()).getA();
		Double latB1 = mapDim.get(DimensionEnum.LATITUDE.getDimension()).getB();
		Double latDelta1 = mapDim.get(DimensionEnum.LATITUDE.getDimension())
				.getDelta();

		if (latA0 == null) {
			// Assume firt time
			dataset.setLatA(latA1);
			dataset.setLatB(latB1);
			dataset.setLatDelta(latDelta1);
		} else {
			// Validate that spatial resolution is the same
			if (latA0.compareTo(latA1) == 0 && latB0.compareTo(latB1) == 0
					&& latDelta0.compareTo(latDelta1) == 0) {
				// It's ok				
			} else {
				Log.getInstance().debug("LatA0: " + latA0);
				Log.getInstance().debug("LatB0: " + latB0);
				Log.getInstance().debug("LatDelta0: " + latDelta0);
				Log.getInstance().debug("LatA1: " + latA1);
				Log.getInstance().debug("LatB1: " + latB1);
				Log.getInstance().debug("LatDelta1: " + latDelta1);
				throw new Exception(
						"The spatial resolution is not the same of the dataset... ");
			}
		}
	}

	private Date truncDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		// date = cal.getTime();
		return cal.getTime();
	}

	private Map<String, Timeperiodicity> mapTimeperiodicity;

	private void loadTimeperiodicity() {
		mapTimeperiodicity = new HashMap<String, Timeperiodicity>();
		mapTimeperiodicity.put(PeriodType.months().getName(),
				persistenceService.find(Timeperiodicity.class,
						TimePeriodicityEnum.MONTLY.getId()));
		mapTimeperiodicity.put(PeriodType.weeks().getName(),
				persistenceService.find(Timeperiodicity.class,
						TimePeriodicityEnum.WEEKLY.getId()));
		mapTimeperiodicity
				.put(PeriodType.days().getName(), persistenceService.find(
						Timeperiodicity.class,
						TimePeriodicityEnum.DAILY.getId()));
	}

	private Timeperiodicity parseTimePeriodicity(Double deltaDate,
			String unitsDeltaDate) {

		Timeperiodicity tp = null;

		Log.getInstance().debug(
				"Parsing periodicity: " + deltaDate + " " + unitsDeltaDate);

		if (mapTimeperiodicity == null) {
			loadTimeperiodicity();
		}

		if (deltaDate == 1) {
			tp = mapTimeperiodicity.get(unitsDeltaDate);
		} else {
			Log.getInstance().warn("No tiene una periodicidad de 1 periodo");
		}

		return tp;
	}

	private Data createData(Dataset dataset, File gcmFile) throws IOException {
		log.info("Extracting information from netcdf file " + gcmFile.getName()
				+ "...");

		Data data = null;
		NetcdfDataset nds = null;

		try {
			nds = NetcdfDataset.openDataset(gcmFile.getAbsolutePath());

			log.info("Ns info: " + nds.getDetailInfo());
			log.info("Ns info: " + nds.getFileTypeId());
			log.info("Ns info: " + nds.getFileTypeVersion());
			log.info("Ns info: " + nds.getFileTypeDescription());
			log.info("Ns info: " + nds.getId());
			log.info("Ns info: " + nds.getLocation());
			log.info("Ns info: " + nds.getTitle());
			log.info("Ns info: " + nds.getAggregation());
			log.info("Ns info: " + nds.getCoordinateAxes());
			log.info("Ns info: " + nds.getCoordinateSystems());
			log.info("Ns info: " + nds.getDimensions());
			log.info("Ns info: " + nds.getGlobalAttributes());
			log.info("Ns info: " + nds.getRootGroup().getFullName());

			// Parse data
			data = parseData(dataset, nds, gcmFile);

			// Dimensions
			List<org.gcta.sdw.persistence.entity.Dimension> ldbdims = parseDimensions(nds);
			data.setDimensionList(ldbdims);

			// Variables
			List<Datavariable> ldatavars = parseDataVariables(nds, data);
			data.setDatavariableList(ldatavars);

			// Data - name
			String dataName = createDataName(nds, ldatavars);
			data.setName(dataName);
			data.setOriginalFileName(gcmFile.getName());

			// Data - description / cdl
			String dumpInfo = getDumpInfo(nds);
			data.setDumpInfo(dumpInfo);
		} catch (IOException e) {
			throw e;
		} finally {
			if (nds != null) {
				nds.close();
			}
		}

		return data;
	}

	private String getDumpInfo(NetcdfDataset nds) {
		log.info("Description...");

		StringWriter sw = new StringWriter();
		nds.writeCDL(new PrintWriter(sw), false);

		log.info(sw.toString().substring(0, 10) + "...");

		return sw.toString();
	}

	private List<Datavariable> parseDataVariables(NetcdfDataset nds, Data data)
			throws IOException {
		log.info("Variables...");

		List<Variable> lncvars = nds.getVariables();
		log.info("Variables - posible core variables:");

		List<Datavariable> ldatavars = new ArrayList<Datavariable>();
		for (Variable var : lncvars) {
			if (var.getDimensions().size() >= 3) {
				log.info("Short name: " + var.getShortName());
				log.info("Full Name: " + var.getFullName());
				log.info("Dimensions: " + var.getDimensions());
				log.info("Attributes: " + var.getAttributes());
				log.info("Description: " + var.getDescription());
				log.info("Element size: " + var.getElementSize());
				log.info("Size: " + var.getSize());
				log.info("Units: " + var.getUnitsString());
				log.info("DataType: " + var.getDataType());

				log.info("Levels...");
				CoordinateAxis levelCa = nds
						.findCoordinateAxis(AxisType.Pressure);
				Array levelArray = null;
				if (levelCa != null) {
					levelArray = levelCa.read();
					log.info("Size: " + levelArray.getSize());
					log.info("Elements: " + levelArray);
				}

				Datavariable variable = new Datavariable(new DatavariablePK());
				variable.setData(data);
				variable.setShortName(var.getShortName()); // TODO
				variable.setFullName(var.getFullName());
				variable.setDescription(var.getDescription()); // TODO
				variable.setDataType(var.getDataType().name());
				variable.setUnits(var.getUnitsString());
				String levels = null;
				if (levelArray != null) {
					levels = arrayToLevelsString(levelArray);
					variable.setLevels(levels);
					variable.setLevelUnits(levelCa.getUnitsString());
				}

				Metavariableenum standardizedVariable = mapVariable(variable);
				variable.setMetavariableenum(standardizedVariable);

				ldatavars.add(variable);
			}
		}

		return ldatavars;
	}

	private Metavariableenum mapVariable(Datavariable datavariable) {
		Metavariableenum metaVariable = null;

		if (this.metaVariablesMap == null) {
			loadMetaVariablesMap();
		}

		final String varName = datavariable.getShortName();

		if (varName.compareToIgnoreCase("air") == 0
				|| varName.compareToIgnoreCase("ta") == 0) {
			if (datavariable.getLevels() != null) {
				metaVariable = metaVariablesMap.get("ta");
			} else {
				metaVariable = metaVariablesMap.get("ts");
			}
		} else if (varName.compareToIgnoreCase("ts") == 0
				|| varName.toLowerCase().startsWith("temperature_surface")) {
			metaVariable = metaVariablesMap.get("ts");
		} else if (varName.compareToIgnoreCase("hgt") == 0
				|| varName.compareToIgnoreCase("zg") == 0) {
			metaVariable = metaVariablesMap.get("zg");
		} else if (varName.compareToIgnoreCase("tas") == 0) {
			metaVariable = metaVariablesMap.get("tas");
		} else if (varName.compareToIgnoreCase("hgt") == 0
				|| varName.compareToIgnoreCase("zg") == 0
				|| varName.toLowerCase().startsWith("geopotential_height")) {
			metaVariable = metaVariablesMap.get("zg");
		} else if (varName.compareToIgnoreCase("shum") == 0
				|| varName.compareToIgnoreCase("hus") == 0) {
			metaVariable = metaVariablesMap.get("hus");
		} else if (varName.compareToIgnoreCase("shum") == 0
				|| varName.compareToIgnoreCase("hus") == 0) {
			metaVariable = metaVariablesMap.get("hus");
		} else if (varName.compareToIgnoreCase("rhum") == 0
				|| varName.compareToIgnoreCase("hur") == 0
				|| varName.toLowerCase().startsWith("relative_humidity")) {
			metaVariable = metaVariablesMap.get("hur");
		} else if (varName.compareToIgnoreCase("uwnd") == 0
				|| varName.compareToIgnoreCase("ua") == 0) {
			if (datavariable.getLevels() != null) {
				metaVariable = metaVariablesMap.get("ua");
			} else {
				metaVariable = metaVariablesMap.get("uas");
			}
		} else if (varName.compareToIgnoreCase("uas") == 0) {
			metaVariable = metaVariablesMap.get("uas");
		} else if (varName.compareToIgnoreCase("vwnd") == 0
				|| varName.compareToIgnoreCase("va") == 0) {
			if (datavariable.getLevels() != null) {
				metaVariable = metaVariablesMap.get("va");
			} else {
				metaVariable = metaVariablesMap.get("vas");
			}
		} else if (varName.compareToIgnoreCase("vas") == 0) {
			metaVariable = metaVariablesMap.get("vas");
		} else if (varName.compareToIgnoreCase("slp") == 0
				|| varName.compareToIgnoreCase("psl") == 0) {
			metaVariable = metaVariablesMap.get("psl");
		} else if (varName.compareToIgnoreCase("pres") == 0
				|| varName.compareToIgnoreCase("ps") == 0) {
			metaVariable = metaVariablesMap.get("ps");
		} else {
			throw new RuntimeException("Variable not recognized: " + varName);
		}

		if (metaVariable == null) {
			throw new RuntimeException("Variable not mapped: " + varName);
		}

		return metaVariable;
	}

	private void loadMetaVariablesMap() {
		// Load map of MetaVariablesEnum
		List<Metavariableenum> metaVariablesList = persistenceService
				.queryAll(Metavariableenum.class);
		metaVariablesMap = new HashMap<String, Metavariableenum>();
		for (Metavariableenum metavariableenum : metaVariablesList) {
			metaVariablesMap.put(metavariableenum.getStdVarId(),
					metavariableenum);
		}

	}

	private String createDataName(NetcdfDataset nds,
			List<Datavariable> ldatavars) {
		String dataName = "";

		if (ldatavars.size() == 0) {
			throw new RuntimeException("No primary variables were found");
		}

		Datavariable var = ldatavars.get(0);
		dataName = var.getFullName();

		// Levels
		CoordinateAxis pressAxis = nds.findCoordinateAxis(AxisType.Pressure);
		if (pressAxis != null) {
			long levelSize = pressAxis.getSize();

			if (levelSize == 1) {
				String level = var.getLevels();
				Long aLevel = ClimaticStandardizer.stdLevel(level,
						var.getLevelUnits());
				level = "" + aLevel;

				dataName += "-" + level;
			} else if (levelSize > 1 && levelSize < 5) {
				dataName += var.getLevels();
			} else {
				dataName += "-multi-level";
			}
		}

		if (ldatavars.size() > 1) {
			dataName += "...";
		}

		return dataName;
	}

	private List<org.gcta.sdw.persistence.entity.Dimension> parseDimensions(
			NetcdfDataset nds) {
		List<org.gcta.sdw.persistence.entity.Dimension> ldbdims = null;

		log.info("Dimensions...");
		List<Dimension> ldims = nds.getDimensions();
		for (Dimension dim : ldims) {
			log.info("Short name: " + dim.getShortName());
			log.info("Full name: " + dim.getFullName());
			log.info("Length: " + dim.getLength());
		}

		log.info("Temporal coverage...");
		SimpleDateFormat sdf = FormatDates.getTimeFormat();
		CoordinateAxis timeAxis = nds.findCoordinateAxis(AxisType.Time);
		log.info("First: "
				+ sdf.format(getConvertedDate(getFirstElement(timeAxis),
						timeAxis.getUnitsString())));
		log.info("Last: "
				+ sdf.format(getConvertedDate(getLastElement(timeAxis),
						timeAxis.getUnitsString())));
		log.info("Units: " + timeAxis.getUnitsString());
		ReadablePeriod period = getResolution(timeAxis, true);
		log.info("Resolution: " + period.getValue(0) + " "
				+ period.getPeriodType().getName());

		log.info("Spatial coverage...");
		log.info("Latitud...");
		CoordinateAxis lonAxis = nds.findCoordinateAxis(AxisType.Lon);
		log.info("First: " + getFirstElement(lonAxis));
		log.info("Last: " + getLastElement(lonAxis));
		log.info("Units: " + lonAxis.getUnitsString());
		log.info("Resolution: " + getResolution(lonAxis));
		log.info("Longitud...");
		CoordinateAxis latAxis = nds.findCoordinateAxis(AxisType.Lat);
		log.info("First: " + getFirstElement(latAxis));
		log.info("Last: " + getLastElement(latAxis));
		log.info("Units: " + latAxis.getUnitsString());
		log.info("Resolution: " + getResolution(latAxis));
		log.info("Pressure...");

		CoordinateAxis pressAxis = nds.findCoordinateAxis(AxisType.Pressure);
		if (pressAxis != null) {
			log.info("First: " + getFirstElement(pressAxis));
			log.info("Last: " + getLastElement(pressAxis));
			log.info("Units: " + pressAxis.getUnitsString());
			log.info("Resolution: " + getResolution(pressAxis));
		}

		// List of dimensions
		ldbdims = new ArrayList<org.gcta.sdw.persistence.entity.Dimension>();

		// Time
		org.gcta.sdw.persistence.entity.Dimension dbDim = new org.gcta.sdw.persistence.entity.Dimension(
				new DimensionPK());
		dbDim.getDimensionPK().setName(timeAxis.getAxisType().name());
		dbDim.setSize((int) timeAxis.getSize());
		dbDim.setADate(getConvertedDate(getFirstElement(timeAxis),
				timeAxis.getUnitsString()));
		dbDim.setBDate(getConvertedDate(getLastElement(timeAxis),
				timeAxis.getUnitsString()));
		dbDim.setDeltaDate((double) period.getValue(0) > 0 ? period.getValue(0)
				: 1.0); // Possible bad solution
		dbDim.setUnits(timeAxis.getUnitsString());
		dbDim.setUnitsDeltaDate(period.getPeriodType().getName());
		ldbdims.add(dbDim);
		// Longitude
		dbDim = new org.gcta.sdw.persistence.entity.Dimension(new DimensionPK());
		dbDim.getDimensionPK().setName(lonAxis.getAxisType().name());
		dbDim.setSize((int) lonAxis.getSize());
		dbDim.setA(getFirstElement(lonAxis));
		dbDim.setB(getLastElement(lonAxis));
		dbDim.setDelta(getResolution(lonAxis));
		dbDim.setUnits(lonAxis.getUnitsString());
		ldbdims.add(dbDim);
		// Latitude
		dbDim = new org.gcta.sdw.persistence.entity.Dimension(new DimensionPK());
		dbDim.getDimensionPK().setName(latAxis.getAxisType().name());
		dbDim.setSize((int) latAxis.getSize());
		dbDim.setA(getFirstElement(latAxis));
		dbDim.setB(getLastElement(latAxis));
		dbDim.setDelta(getResolution(latAxis));
		dbDim.setUnits(latAxis.getUnitsString());
		ldbdims.add(dbDim);
		// Pressure
		if (pressAxis != null) {
			dbDim = new org.gcta.sdw.persistence.entity.Dimension(
					new DimensionPK());
			dbDim.getDimensionPK().setName(pressAxis.getAxisType().name());
			dbDim.setSize((int) pressAxis.getSize());
			dbDim.setA(getFirstElement(pressAxis));
			dbDim.setB(getLastElement(pressAxis));
			dbDim.setDelta(getResolution(pressAxis));
			dbDim.setUnits(pressAxis.getUnitsString());
			ldbdims.add(dbDim);
		}

		return ldbdims;
	}

	private Data parseData(Dataset dataset, NetcdfDataset nds, File gcmFile)
			throws IOException {
		// Data
		Data data = new Data();
		DataPK dataPK = new DataPK();
		dataPK.setDatasetId(dataset.getDatasetId());
		data.setDataPK(dataPK);
		data.setDataset(dataset);
		data.setFileTypeDescription(nds.getFileTypeDescription());
		data.setLocation(nds.getLocation());

		// Set data fields
		// FileInputStream fis = new FileInputStream(gcmFile);
		// byte[] bFile = new byte[(int) gcmFile.length()]; // TODO
		// java.lang.NegativeArraySizeException for large files
		// fis.read(bFile);
		// data.setFile(bFile);
		// fis.close();
		data.setFilePath(gcmFile.getAbsolutePath());

		return data;
	}

	private String arrayToLevelsString(Array levelArray) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < levelArray.getSize(); i++) {
			sb.append(levelArray.getInt(i));
			if (i < levelArray.getSize() - 1) {
				sb.append(LEVEL_SEPARATOR);
			}
		}

		return sb.toString();
	}

	private static double getFirstElement(Variable var) {
		return getElement(var, 0);

	}

	private static double getLastElement(Variable var) {
		return getElement(var, (int) (var.getSize() - 1));
	}

	private static double getElement(Variable var, int index) {
		double element = 0;

		int i = index;

		try {
			Array array = var.read(i + ":" + i + ":1");
			element = array.getDouble(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return element;
	}

	private static Double getResolution(Variable var) {
		Double resolution = null;

		if (var.getSize() == 1) {
			return 0.0;
		}

		try {
			Array array = var.read("0:1:1");
			// log.debug(array);
			// log.debug(array.getElementType());
			// log.debug(array.getDouble(0));
			double a = array.getDouble(0);
			double b = array.getDouble(1);
			resolution = b - a;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resolution;
	}

	private static ReadablePeriod getResolution(Variable var, boolean isDateTime) {
		try {
			// Read first two elements
			Array array = var.read("0:1:1");

			// log.debug(array);
			// log.debug(array.getElementType());
			// log.debug(array.getDouble(0));

			// Convert in date-time variables
			double a = 0;
			double b = 0;
			org.joda.time.DateTime da2 = null;
			org.joda.time.DateTime db2 = null;

			a = array.getDouble(0);
			b = array.getDouble(1);
			da2 = new org.joda.time.DateTime(getConvertedDate(a,
					var.getUnitsString()));
			db2 = new org.joda.time.DateTime(getConvertedDate(b,
					var.getUnitsString()));

			// Calculate period
			Interval interval = new Interval(da2, db2);

			// Define best period level
			ReadablePeriod period;
			period = Hours.hoursIn(interval);

			// hours
			if (period.getValue(0) < 24) {
				return period;
			} else { // days
				period = Days.daysIn(interval);

				if (period.getValue(0) < 10) {
					return period;
				} else { // months
					period = Months.monthsIn(interval);

					if (period.getValue(0) < 12) {
						return period;
					} else {
						return period;
						// Possible need to drill out
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static ReadablePeriod getTimeResolutionBetweenDates(Date a, Date b) {
		org.joda.time.DateTime da2 = new org.joda.time.DateTime(a);
		org.joda.time.DateTime db2 = new org.joda.time.DateTime(b);

		// Calculate period
		Interval interval = new Interval(da2, db2);

		// Define best period level
		ReadablePeriod period = null;
		period = Hours.hoursIn(interval);

		// hours
		if (period.getValue(0) < 24) {
			return period;
		} else { // days
			period = Days.daysIn(interval);

			if (period.getValue(0) < 10) {
				return period;
			} else { // months
				period = Months.monthsIn(interval);

				if (period.getValue(0) < 12) {
					return period;
				} else {
					return period;
					// Possible need to drill out
				}
			}
		}
	}

	private static Date getConvertedDate(double value, String units) {
		Date date = null;

		if (units.indexOf("since") > 0) {
			String[] parts = units.split("\\s");
			// for (String string : parts) {
			// Log.getInstance().info(string);
			// }

			String unitPart = parts[0];
			String conditionPart = parts[1];
			String datePart = parts[2]
					+ ((parts.length > 3) ? " " + parts[3] : "");
			datePart = convertSpecialCases(datePart);

			// log.info("Date string: " + datePart);
			Date baseDate = autoParseDate(datePart);
			// log.info("Parsed date: " + baseDate);

			// log.info("Value: " + value);
			date = completeDate(baseDate, unitPart, conditionPart, value);
			// log.info("Date: " + date);

		}

		return date;
	}

	private static String convertSpecialCases(String dateTime) {
		String pattern = "(\\d{1}[-/]\\d{1}[-/]\\d{1})(\\s.*)";
		String replacementPattern = "000$1$2";

		String output = dateTime;

		if (output.matches(pattern)) {
			output = output.replaceFirst(pattern, replacementPattern);
		} else if (dateTime.endsWith("Z")) {
			output = dateTime.replaceAll("Z", "");
		}

		return output;
	}

	private static Date completeDate(Date baseDate, String unit,
			String condition, double value) {
		DateTime baseDateAux = new DateTime(baseDate.getTime());
		DateTime completedDate = baseDateAux;

		unit = unit.toLowerCase();

		if (unit.startsWith("hour")) {
			completedDate = baseDateAux.plusHours((int) value);
		} else if (unit.startsWith("min")) {
			completedDate = baseDateAux.plusMinutes((int) value);
		}
		// TODO Complete...

		return completedDate.toDate();
	}

	private static Date autoParseDate(String dateString) {
		// Automatic date parsing, using POJava
		// TimeZone tzUTC = TimeZone.getTimeZone("UTC");
		org.pojava.datetime.DateTime dt = new org.pojava.datetime.DateTime(
				dateString);
		// DateTime dt = new DateTime(dateString);
		return dt.toDate();
	}

	@Override
	public Map<File, List<File>> getPreloadedGcms() {
		File repository = new File(Constant.PRELOADED_GCMS_PATH);

		final FileFilter filterNc = new FileFilter() {
			@Override
			public boolean accept(File file) {
				String[] extensions = new String[] { "nc", "grb" };

				if (file.isFile()) {
					String path = file.getAbsolutePath().toLowerCase();
					for (int i = 0, n = extensions.length; i < n; i++) {
						String extension = extensions[i];
						if ((path.endsWith(extension) && (path.charAt(path
								.length() - extension.length() - 1)) == '.')) {
							return true;
						}
					}
				}
				return false;
			}
		};

		final FileFilter filterOnlyFolders = new FileFilter() {
			@Override
			public boolean accept(File file) {

				if (file.isDirectory()) {
					return true;
				}
				return false;
			}
		};

		File[] repoFolders = repository.listFiles(filterOnlyFolders);
		Map<File, List<File>> repoMap = new HashMap<File, List<File>>();

		if (repoFolders != null) {
			for (File file : repoFolders) {
				File[] ncFiles = file.listFiles(filterNc);
				if (ncFiles != null && ncFiles.length > 0) {
					repoMap.put(file, Arrays.asList(ncFiles));
				}
			}
		}

		return repoMap;
	}

	@Override
	public void saveNetcdfFiles(Dataset selectedDataset,
			List<File> selectedPreloadedGcms) throws Exception {
		for (File file : selectedPreloadedGcms) {
			saveNetcdfFile(selectedDataset, file);
		}

	}

	@Override
	public List<Data> getPreloadedStations() throws Exception {
		List<Data> preloadedStations = null;
		File file = new File(Constant.PRELOADED_STATIONS_INFO_FILE);
		FileReader fr = new FileReader(file);
		BufferedReader br = null;
		try {
			br = new BufferedReader(fr);

			preloadedStations = new ArrayList<Data>();
			String line = null;
			while ((line = br.readLine()) != null) {
				// log.debug("Line: " + line);
				String field[] = line.split(",");
				if (field.length >= 5) {
					Data station = new Data();
					station.setCode(field[0]);
					station.setName(field[1]);
					// NumberFormat df = NumberFormat
					// .getNumberInstance(Locale.ENGLISH);
					Double lon = Double.parseDouble(field[2]);
					Double lat = Double.parseDouble(field[3]);
					Double h = Double.parseDouble(field[4]);
					station.setLongitude(lon);
					station.setLatitude(lat);
					station.setHeight(h);
					preloadedStations.add(station);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
		}
		// for (Data data : preloadedStations) {
		// log.debug(data.getCode() + " " + data.getLatitude() + " " +
		// data.getLongitude() + " " + data.getHeight());
		// }

		return preloadedStations;
	}

	@Override
	public void saveStations(Dataset selectedDataset,
			List<Data> selectedPreloadedStations) throws Exception {
		persistenceService.insertBatch(selectedPreloadedStations);
	}

	public void getTimeSerie() throws Exception {
		// Inputs - Parameters
		// String filePath =
		// "C:/PreloadedGcms/MPEH5/MPEH5_SRA1B_1_D0_hur100_1-36524-box.nc";
		String filePath = "C:/PreloadedGcms/NCEPNCAR Reanalysis 1/rhum.mon.mean.nc";

		Date startDate = FormatDates.getDateFormat().parse("1964-01-01");
		Date endDate = FormatDates.getDateFormat().parse("2000-12-01");

		// Pasaje
		Double latitude = -3.330; // Degrees North
		Double longitude = -79.782; // Degrees East
		Double latitude_ = convertToDegreesNorth(latitude, null);
		Double longitude_ = convertToDegreesEast(longitude, null);
		log.debug(String.format("Station coordinates (%.2f;%.2f)", latitude_,
				longitude_));

		String varName = "rhum";
		String varLevel = "300";

		NetcdfDataset nds = null;

		try {
			nds = NetcdfDataset.openDataset(filePath);

			// Time
			log.debug("Time axis");
			CoordinateAxis timeAxis = nds.findCoordinateAxis(AxisType.Time);
			log.debug("Units: " + timeAxis.getUnitsString());
			log.debug("Data type: " + timeAxis.getDataType());

			Array timeArray = timeAxis.read();
			log.debug(timeArray);

			List<Date> timeList = convertToDate(timeArray,
					timeAxis.getUnitsString());
			// log.debug(timeList);
			// for (Date date : timeList) {
			// log.debug(FormatDates.getTimeFormat().format(date));
			// }

			// Date comparison
			int dateStartIndex = timeList.indexOf(startDate);
			int dateEndIndex = timeList.indexOf(endDate);
			log.debug("Start " + dateStartIndex);
			log.debug("End   " + dateEndIndex);

			// Level
			log.debug("Level axis");
			CoordinateAxis levelAxis = nds
					.findCoordinateAxis(AxisType.Pressure);
			log.debug("Units: " + levelAxis.getUnitsString());
			log.debug("Data type: " + levelAxis.getDataType());
			Array levelArray = levelAxis.read();
			log.debug("Level array: " + levelArray);
			Integer levIndex = null;
			for (int i = 0; i < levelArray.getSize(); i++) {
				double level = levelArray.getDouble(i);
				if (level == Double.parseDouble(varLevel)) {
					levIndex = i;
					log.debug("Lev-index: " + levIndex);
					break;
				}
			}
			if (levIndex == null) {
				throw new RuntimeException("Variable level was not found: "
						+ varLevel);
			}

			// Latitude
			log.debug("Lat axis");
			CoordinateAxis latAxis = nds.findCoordinateAxis(AxisType.Lat);
			log.debug("Units: " + latAxis.getUnitsString());
			log.debug("Data type: " + latAxis.getDataType());
			Array latArray = latAxis.read();
			log.debug(latArray);
			Array stdLatArray = latArray.copy();
			convertToDegreesNorth(stdLatArray, latAxis.getUnitsString());
			log.debug(stdLatArray);

			Integer latIndex = null;
			List<SimpleEntry<Integer, Double>> iterateLatList = createIterationList(stdLatArray);
			boolean reverseOrderLat = isReverseOrder(stdLatArray);
			log.debug("Iteration list: " + iterateLatList);
			for (SimpleEntry<Integer, Double> simpleEntry : iterateLatList) {
				int i = simpleEntry.getKey();
				double lat = simpleEntry.getValue();
				if (lat > latitude_
						&& ((!reverseOrderLat && i > 0) || (reverseOrderLat && i < (latArray
								.getSize() - 1)))) {
					latIndex = i + ((!reverseOrderLat) ? -1 : 0);
					log.debug("Lat-index: " + latIndex);
					break;
				}
			}
			if (latIndex == null) {
				throw new RuntimeException(
						"Station is out of the bounds of GCM (Lat). Station: "
								+ latitude_ + ":" + longitude_);
			}

			// Longitude
			log.debug("Lon axis");
			CoordinateAxis lonAxis = nds.findCoordinateAxis(AxisType.Lon);
			log.debug("Units: " + lonAxis.getUnitsString());
			log.debug("Data type: " + lonAxis.getDataType());
			Array lonArray = lonAxis.read();
			log.debug(lonArray);
			Array stdLonArray = lonArray.copy();
			convertToDegreesEast(stdLonArray, lonAxis.getUnitsString());
			log.debug(stdLonArray);

			Integer lonIndex = null;
			List<SimpleEntry<Integer, Double>> iterateLonList = createIterationList(stdLonArray);
			boolean reverseOrderLon = isReverseOrder(stdLonArray);
			log.debug("Iteration list: " + iterateLonList);
			for (SimpleEntry<Integer, Double> simpleEntry : iterateLonList) {
				int i = simpleEntry.getKey();
				double lon = simpleEntry.getValue();
				if (lon > longitude_
						&& ((!reverseOrderLon && i > 0) || (reverseOrderLon && i < (lonArray
								.getSize() - 1)))) {
					lonIndex = i + ((!reverseOrderLon) ? -1 : 0);
					log.debug("Lon-index: " + lonIndex);
					break;
				}
			}
			if (lonIndex == null) {
				throw new RuntimeException(
						"Station is out of the bounds of GCM (Lon). Station: "
								+ latitude_ + ":" + longitude_);
			}

			Variable var = nds.findVariable(varName);

			// Build a map of dimensions, for storing the index corresponding to
			// each dimension
			List<Dimension> dims = var.getDimensions();
			Map<AxisType, Integer> mapDimensions = new HashMap<AxisType, Integer>();
			log.debug("Dimensions:");
			for (Dimension dimension : dims) {
				log.debug(dimension.getFullName());

			}
			for (int i = 0; i < dims.size(); i++) {
				Dimension dim = dims.get(i);
				if (dim.getFullName().compareTo("time") == 0) {
					mapDimensions.put(AxisType.Time, i);
				} else if (dim.getFullName().startsWith("lev")) {
					mapDimensions.put(AxisType.Pressure, i);
				} else if (dim.getFullName().compareTo("lat") == 0) {
					mapDimensions.put(AxisType.Lat, i);
				} else if (dim.getFullName().compareTo("lon") == 0) {
					mapDimensions.put(AxisType.Lon, i);
				}
			}
			// for (AxisType at : mapDimensions.keySet()) {
			// log.debug(at + ">" + mapDimensions.get(at));
			// }
			log.debug("Map-Dimensions: " + mapDimensions);

			// Harvest data
			int size[] = var.getShape();
			log.debug("Size: " + Arrays.toString(size));

			int timeIndex0 = 0;
			int timeIndex1 = 10;
			// int levIndex = 0;

			int origin[] = new int[4];
			origin[mapDimensions.get(AxisType.Time)] = timeIndex0;
			origin[mapDimensions.get(AxisType.Pressure)] = levIndex;
			origin[mapDimensions.get(AxisType.Lat)] = latIndex;
			origin[mapDimensions.get(AxisType.Lon)] = lonIndex;
			log.debug("origin: " + Arrays.toString(origin));

			int shape[] = new int[4];
			shape[mapDimensions.get(AxisType.Time)] = timeIndex1 - timeIndex0
					+ 1;
			shape[mapDimensions.get(AxisType.Pressure)] = 1;
			shape[mapDimensions.get(AxisType.Lat)] = 2;
			shape[mapDimensions.get(AxisType.Lon)] = 2;
			log.debug("shape: " + Arrays.toString(shape));

			Array varData = var.read(origin, shape);
			log.debug(varData);

			// Check data
			// Index index = varData.getIndex();
			// for (int i = 0; i < varData.getShape()[mapDimensions
			// .get(AxisType.Time)]; i++) {
			// for (int j = 0; j < varData.getShape()[mapDimensions
			// .get(AxisType.Pressure)]; j++) {
			// for (int k = 0; k < varData.getShape()[mapDimensions
			// .get(AxisType.Lat)]; k++) {
			// for (int l = 0; l < varData.getShape()[mapDimensions
			// .get(AxisType.Lon)]; l++) {
			// index.set(i, j, k, l);
			// Double val = varData.getDouble(index);
			// log.debug(index + " : " +String.format("%.3f", val));
			// }
			// }
			// }
			// }

			// Interpolate data
			log.debug("Interpolating data...");
			List<SimpleEntry<Date, Double>> timeSerie = new ArrayList<SimpleEntry<Date, Double>>();
			Index index = varData.getIndex();
			for (int i = 0; i < varData.getShape()[mapDimensions
					.get(AxisType.Time)]; i++) {
				double a = varData.getDouble(index.set(i, 0, 0, 0));
				double b = varData.getDouble(index.set(i, 0, 0, 1));
				double c = varData.getDouble(index.set(i, 0, 1, 0));
				double d = varData.getDouble(index.set(i, 0, 1, 1));
				// log.debug(String.format(
				// "Time: %d - Square: [%.2f; %.2f; %.2f; %.2f]", i, a, b,
				// c, d));

				double d1 = distance(longitude_, latitude_,
						stdLonArray.getDouble(lonIndex),
						stdLatArray.getDouble(latIndex));
				double d2 = distance(longitude_, latitude_,
						stdLonArray.getDouble(lonIndex + 1),
						stdLatArray.getDouble(latIndex));
				double d3 = distance(longitude_, latitude_,
						stdLonArray.getDouble(lonIndex),
						stdLatArray.getDouble(latIndex + 1));
				double d4 = distance(longitude_, latitude_,
						stdLonArray.getDouble(lonIndex + 1),
						stdLatArray.getDouble(latIndex + 1));
				// log.debug(String.format("Distances: [%.2f; %.2f; %.2f; %.2f]",
				// d1, d2, d3, d4));

				Double val = (a / d1 + b / d2 + c / d3 + d / d4)
						/ (1 / d1 + 1 / d2 + 1 / d3 + 1 / d4);
				// log.debug(String.format("Value: %.2f", val));
				timeSerie.add(new SimpleEntry<Date, Double>(timeList
						.get(timeIndex0 + i), val));
			}
			log.debug("Time serie: ");
			for (SimpleEntry<Date, Double> simpleEntry : timeSerie) {
				log.debug(String.format("%s : %.2f", FormatDates
						.getDateFormat().format(simpleEntry.getKey()),
						simpleEntry.getValue()));
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (nds != null) {
				nds.close();
			}
		}

	}

	private double distance(Double x0, Double y0, double x1, double y1) {
		return Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2);
	}

	private boolean isReverseOrder(Array array) {
		int a = 0;
		int b = (int) array.getSize() - 1; // TODO
		if (array.getDouble(a) < array.getDouble(b)) {
			return false;
		} else {
			return true;
		}
	}

	private List<SimpleEntry<Integer, Double>> createIterationList(Array array) {
		List<SimpleEntry<Integer, Double>> iterationList = new ArrayList<SimpleEntry<Integer, Double>>();

		int a = 0;
		int b = (int) array.getSize() - 1; // TODO
		if (array.getDouble(a) < array.getDouble(b)) {
			for (int i = a; i <= b; i++) {
				iterationList.add(new SimpleEntry<Integer, Double>(i, array
						.getDouble(i)));
			}
		} else {
			for (int i = b; i >= a; i--) {
				iterationList.add(new SimpleEntry<Integer, Double>(i, array
						.getDouble(i)));
			}
		}

		return iterationList;
	}

	private List<Date> convertToDate(Array timeData, String unitsString) {
		List<Date> dates = new ArrayList<Date>();

		for (int i = 0; i < timeData.getSize(); i++) {
			Double val = timeData.getDouble(i);
			// log.debug(val);
			Date date = getConvertedDate(val, unitsString);
			if (date == null) {
				throw new RuntimeException("Date conversion exception: " + val
						+ " " + unitsString);
			}
			dates.add(date);
		}

		return dates;
	}

	private void convertToDegreesNorth(Array latArray, String units) {
		IndexIterator iter = latArray.getIndexIterator();
		Index index = latArray.getIndex();
		while (iter.hasNext()) {
			double val = iter.getDoubleNext();
			double val_ = convertToDegreesNorth(val, units);
			index.set(iter.getCurrentCounter());
			latArray.setDouble(index, val_);
		}
	}

	private void convertToDegreesEast(Array lonArray, String units) {
		IndexIterator iter = lonArray.getIndexIterator();
		Index index = lonArray.getIndex();
		while (iter.hasNext()) {
			double val = iter.getDoubleNext();
			double val_ = convertToDegreesEast(val, units);
			index.set(iter.getCurrentCounter());
			lonArray.setDouble(index, val_);
		}
	}

	private Double convertToDegreesNorth(Double latitude, String units) {
		if (units != null && units.compareToIgnoreCase("degrees_south") == 0) {
			latitude = latitude * -1;
		}

		return latitude;
	}

	private Double convertToDegreesEast(Double longitude, String units) {
		if (units != null && units.compareToIgnoreCase("degrees_west") == 0) {
			longitude = longitude * -1;
		}

		if (longitude > 180) {
			return (360 - longitude) * -1;
		}

		return longitude;
	}

	public static void main(String... args) throws Exception {
		new EtlServiceImpl().getPreloadedStations();
		// new EtlServiceImpl().getTimeSerie();
	}

	// For observations dataset
	/**
	 * Sets metadata fields from time series: fromDate, toDate, timePeriodicity
	 * 
	 * @param variable
	 */

	@Override
	public void setDatavariableMetadata(Datavariable variable) throws Exception {
		Log.getInstance()
				.debug("Setting datavariable metadata from time serie");

		Date fromDate = null;
		Date toDate = null;
		ReadablePeriod constantPeriod = null;

		int emptyValuesCount = 0;

		Date lastDate = null;
		Date date = null;
		for (int i = 0; i < variable.getTimeserieList().size(); i++) {
			Timeserie obs = variable.getTimeserieList().get(i);
			date = obs.getTimeseriePK().getDateTime();

			if (i == 0) {
				fromDate = date;
			}
			if (i == variable.getTimeserieList().size() - 1) {
				toDate = date;
			}

			if (i > 0) {
				ReadablePeriod period = getTimeResolutionBetweenDates(lastDate,
						date);
				if (constantPeriod == null) {
					constantPeriod = period;
				} else {
					if (!constantPeriod.equals(period)) {
						Log.getInstance().debug("Actual period: " + constantPeriod);
						Log.getInstance().debug("New period: " + period);
						Log.getInstance().debug("Between dates: " + lastDate + " and " + date);
						String message = String
								.format("The time resolution is not unique. \nResolution A: %d %s \nResolution B: %d %s \non date: %s ",
										constantPeriod.getValue(0),
										constantPeriod.getPeriodType()
												.getName(), period.getValue(0),
										period.getPeriodType().getName(),
										date);
						// throw new RuntimeException(message);
						Log.getInstance().error(message);
					}
				}
			}

			lastDate = date;
		}

		log.info("Date from: " + fromDate);
		log.info("Date to: " + toDate);
		log.info("Resolution: " + constantPeriod.getValue(0) + " "
				+ constantPeriod.getPeriodType().getName());
		variable.setFromDate(fromDate);
		variable.setToDate(toDate);

		Timeperiodicity timePeriodicity = parseTimePeriodicity(
				(double) constantPeriod.getValue(0), constantPeriod
						.getPeriodType().getName());
		variable.setTimeperiodicity(timePeriodicity);
	}

	@Override
	public void setDatasetMetadataFromDatavariable(Dataset dataset,
			Datavariable variable) throws Exception {
		Log.getInstance()
				.debug("Do nothing... Dates and periodicity are managed by variable in observations dataset.");

		// Set time resolution
		// Date dateA0 = dataset.getDateA();
		// Date dateB0 = dataset.getDateB();
		// Timeperiodicity timePer0 = dataset.getTimeperiodicity();
		// Log.getInstance().debug("DateA0: " + dateA0);
		// Log.getInstance().debug("DateB0: " + dateB0);
		// Log.getInstance().debug("TimePer0: " + timePer0);
		// Date dateA1 = variable.getFromDate();
		// Date dateB1 = variable.getToDate();
		// Timeperiodicity timePer1 = variable.getTimeperiodicity();

		// dateA1 = truncDate(dateA1);
		// dateB1 = truncDate(dateB1);
		// Log.getInstance().debug("DateA1: " + dateA1);
		// Log.getInstance().debug("DateB1: " + dateB1);
		// Log.getInstance().debug("TimePer1: " + timePer1);
		// if (dateA0 == null) {
		// // Assume first time
		// Log.getInstance().info(
		// "Setting time resolution in dataset - first time...");
		// dataset.setDateA(dateA1);
		// dataset.setDateB(dateB1);
		// dataset.setTimeperiodicity(timePer1);
		// } else {
		// // Validate that resolution is the same
		// if (dateA0.compareTo(dateA1) == 0 && dateB0.compareTo(dateB1) == 0
		// && timePer0.getTimeId().equals(timePer1.getTimeId())) {
		// // It's ok
		// } else {
		// String message = String.format(
		// "The variable's time resolution is not the same of the dataset. \n"
		// + "Dataset time periodicity: %s \n"
		// + "Varible time periodicity: %s", timePer0,
		// timePer1);
		// throw new Exception(message);
		// }
		// }
	}
}
