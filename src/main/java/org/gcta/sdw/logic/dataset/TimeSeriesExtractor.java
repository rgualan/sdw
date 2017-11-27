package org.gcta.sdw.logic.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import org.gcta.sdw.persistence.entity.Timeperiodicity;
import org.gcta.sdw.util.FormatDates;
import org.gcta.sdw.util.Log;
import org.joda.time.DateTime;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;

public class TimeSeriesExtractor {

	private final static String WORKSPACE = "C:/workspace";
	private final static Logger log = Log.getInstance();

	/*
	 * public void saveCsvTimeSerie(String filePath, Date startDate, Date
	 * endDate, Double latitude, Double longitude, String varName, String
	 * varLevel, String stationCode) throws Exception { getTimeSerie(filePath,
	 * startDate, endDate, latitude, longitude, varName, varLevel, stationCode);
	 * // saveCsvFile(stationCode, var, varLevel, timeSerie); }
	 */

	public void getTimeSerie(String filePath, Date startDate, Date endDate,
			Double platitude, Double plongitude, String varName,
			String varLevel, String stationName) throws Exception {

		List<SimpleEntry<Date, Double>> timeSerie = getTimeSerie(filePath,
				startDate, endDate, platitude, plongitude, varName, varLevel);

		// TODO Create function to save the time serie into a CSV file
		// saveCsvFile(stationName, filePath, varLevel, timeSerie);
	}

	/**
	 * Interpolate the time series from a Netcdf file Some parameters are
	 * required for extraction: A time period A point - coordinates A variable
	 * and level A station name
	 * 
	 * 
	 * @param filePath
	 * @param startDate
	 * @param endDate
	 * @param platitude
	 * @param plongitude
	 * @param varName
	 * @param varLevel
	 * @return
	 * @throws Exception
	 */
	public List<SimpleEntry<Date, Double>> getTimeSerie(String filePath,
			Date startDate, Date endDate, Double platitude, Double plongitude,
			String varName, String varLevel) throws Exception {
		log.debug("Extract time serie from file: " + filePath);

		// Homogenize coordinates
		Double latitude = convertToDegreesNorth(platitude, null);
		Double longitude = convertToDegreesEast(plongitude, null);

		log.debug(String.format("Station coordinates (%.2f;%.2f)", latitude,
				longitude));
		log.debug("File: " + filePath);
		log.debug("Variable to extract: " + varName + "," + varLevel);

		NetcdfDataset nds = null;

		try {
			nds = NetcdfDataset.openDataset(filePath);

			// Time
			log.debug("Time axis");
			CoordinateAxis timeAxis = nds.findCoordinateAxis(AxisType.Time);
			log.debug("Units: " + timeAxis.getUnitsString());
			log.debug("Data type: " + timeAxis.getDataType());

			Array timeArray = timeAxis.read();
			// log.debug(timeArray);

			List<Date> timeList = convertToDate(timeArray,
					timeAxis.getUnitsString());
			timeList = truncDate(timeList);
			// for (Date date : timeList) {
			// log.debug(FormatDates.getTimeFormat().format(date));
			// }

			// Date comparison
			int dateStartIndex = timeList.indexOf(startDate);
			int dateEndIndex = timeList.indexOf(endDate);
			log.debug("Start date index: " + dateStartIndex);
			log.debug("End date index:  " + dateEndIndex);

			// Level
			log.debug("Level axis");
			CoordinateAxis levelAxis = nds
					.findCoordinateAxis(AxisType.Pressure);
			Integer levIndex = null;
			if (levelAxis != null) {
				log.debug("Units: " + levelAxis.getUnitsString());
				log.debug("Data type: " + levelAxis.getDataType());
				Array levelArray = levelAxis.read();
				// log.debug("Level array: " + levelArray);
				if (varLevel != null) {
					for (int i = 0; i < levelArray.getSize(); i++) {
						double level = levelArray.getDouble(i);
						Long sLevel = ClimaticStandardizer.stdLevel(
								(long) level, levelAxis.getUnitsString());

						if (sLevel.compareTo(Long.parseLong(varLevel)) == 0) {
							levIndex = i;
							log.debug("Lev-index: " + levIndex);
							break;
						}
					}
				} else {
					levIndex = 0;
				}

				if (levIndex == null) {
					throw new RuntimeException("Variable level was not found: "
							+ varLevel);
				}
			} else {
				log.warn("Pressure axis not found!");
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
			// log.debug(stdLatArray);

			Integer latIndex = null;
			List<SimpleEntry<Integer, Double>> iterateLatList = createIterationList(stdLatArray);
			boolean reverseOrderLat = isReverseOrder(stdLatArray);
			// log.debug("Iteration list: " + iterateLatList);
			for (SimpleEntry<Integer, Double> simpleEntry : iterateLatList) {
				int i = simpleEntry.getKey();
				double lat = simpleEntry.getValue();
				if (lat > latitude
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
								+ latitude + ":" + longitude);
			}

			// Longitude
			log.debug("Lon axis");
			CoordinateAxis lonAxis = nds.findCoordinateAxis(AxisType.Lon);
			log.debug("Units: " + lonAxis.getUnitsString());
			log.debug("Data type: " + lonAxis.getDataType());
			Array lonArray = lonAxis.read();
			// log.debug(lonArray);
			Array stdLonArray = lonArray.copy();
			convertToDegreesEast(stdLonArray, lonAxis.getUnitsString()); // Homogenize
			// log.debug(stdLonArray);

			Integer lonIndex = null;
			List<SimpleEntry<Integer, Double>> iterateLonList = createIterationList(stdLonArray);
			boolean reverseOrderLon = isReverseOrder(stdLonArray);
			log.debug("Iteration list: " + iterateLonList);
			for (SimpleEntry<Integer, Double> simpleEntry : iterateLonList) {
				int i = simpleEntry.getKey();
				double lon = simpleEntry.getValue();
				if (lon > longitude
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
								+ latitude + ":" + longitude);
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
			log.debug("Variable size: " + Arrays.toString(size));

			int timeIndex0 = dateStartIndex;
			int timeIndex1 = dateEndIndex;

			// Set origin vector
			int origin[] = new int[mapDimensions.size()];
			origin[mapDimensions.get(AxisType.Time)] = timeIndex0;
			if (levIndex != null) {
				origin[mapDimensions.get(AxisType.Pressure)] = levIndex;
			}
			origin[mapDimensions.get(AxisType.Lat)] = latIndex;
			origin[mapDimensions.get(AxisType.Lon)] = lonIndex;
			log.debug("origin: " + Arrays.toString(origin));

			// Set shape vector
			int shape[] = new int[mapDimensions.size()];
			shape[mapDimensions.get(AxisType.Time)] = timeIndex1 - timeIndex0
					+ 1;
			if (levIndex != null) {
				shape[mapDimensions.get(AxisType.Pressure)] = 1;
			}
			shape[mapDimensions.get(AxisType.Lat)] = 2;
			shape[mapDimensions.get(AxisType.Lon)] = 2;
			log.debug("shape: " + Arrays.toString(shape));

			Array varData = var.read(origin, shape);
			// log.debug(varData);

			// Interpolate data
			// List<SimpleEntry<Date, Double>> timeSerie =
			// interpolateData(varData, mapDimensions, levIndex, longitud,
			// latitud, );
			log.debug("Interpolating data...");
			List<SimpleEntry<Date, Double>> timeSerie = new ArrayList<SimpleEntry<Date, Double>>();
			Index index = varData.getIndex();
			for (int i = 0; i < varData.getShape()[mapDimensions
					.get(AxisType.Time)]; i++) {
				Double a, b, c, d;
				if (levIndex != null) {
					a = varData.getDouble(index.set(i, 0, 0, 0));
					b = varData.getDouble(index.set(i, 0, 0, 1));
					c = varData.getDouble(index.set(i, 0, 1, 0));
					d = varData.getDouble(index.set(i, 0, 1, 1));
				} else {
					a = varData.getDouble(index.set(i, 0, 0));
					b = varData.getDouble(index.set(i, 0, 1));
					c = varData.getDouble(index.set(i, 1, 0));
					d = varData.getDouble(index.set(i, 1, 1));
				}
				// log.debug(String.format(
				// "Time: %d - Square: [%.2f; %.2f; %.2f; %.2f]", i, a, b,
				// c, d));

				double d1 = distance(longitude, latitude,
						stdLonArray.getDouble(lonIndex),
						stdLatArray.getDouble(latIndex));
				double d2 = distance(longitude, latitude,
						stdLonArray.getDouble(lonIndex + 1),
						stdLatArray.getDouble(latIndex));
				double d3 = distance(longitude, latitude,
						stdLonArray.getDouble(lonIndex),
						stdLatArray.getDouble(latIndex + 1));
				double d4 = distance(longitude, latitude,
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
			// log.debug("Time serie: ");
			// for (SimpleEntry<Date, Double> simpleEntry : timeSerie) {
			// log.debug(String.format("%s : %.2f", FormatDates
			// .getDateFormat().format(simpleEntry.getKey()),
			// simpleEntry.getValue()));
			// }

			// Missing info
			log.debug("Missing data info: ");
			log.debug("Get fill value is missing: "
					+ NetcdfDataset.getFillValueIsMissing());
			log.debug("Get invalid data is missing: "
					+ NetcdfDataset.getInvalidDataIsMissing());
			log.debug("Get missing data is missing: "
					+ NetcdfDataset.getMissingDataIsMissing());
			VariableDS varDs = new VariableDS(null, var, true);

			log.debug("VarDS has fill value: " + varDs.hasFillValue());
			log.debug("VarDS has invalid data: " + varDs.hasInvalidData());
			log.debug("VarDS has missing!!! " + varDs.hasMissing());
			log.debug("VarDS has missing value: " + varDs.hasMissingValue());
			log.debug("VarDS has scale offset: " + varDs.hasScaleOffset());
			if (varDs.hasMissing()) {
				Array allarray = varDs.read();
				log.debug("Number of values: " + allarray.getSize());
				Array marray = varDs.getMissingDataArray(varDs.getShape());
				log.debug("Number of missing values: " + marray.getSize());
				Index anIndex = marray.getIndex();
				/*
				 * for (int i = 0; i < marray.getShape()[0]; i++) { Double val =
				 * varData.getDouble(anIndex.set(i, 0, 0, 0));
				 * log.debug("Missing value " + i + " " + val);
				 * if(varDs.isMissingValue(val)){
				 * log.debug("Missing value at time index " + i); } }
				 */
			}

			return timeSerie;
		} catch (IOException e) {
			throw e;
		} finally {
			if (nds != null) {
				nds.close();
			}
		}

	}

	private List<Date> truncDate(List<Date> timeList) {
		List<Date> truncatedDatesList = new ArrayList<Date>();

		Calendar cal = Calendar.getInstance();
		Date date2 = null;
		for (Date date : timeList) {
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			date2 = cal.getTime();
			truncatedDatesList.add(date2);
		}

		return truncatedDatesList;
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
		}

		return output;
	}

	private static Date completeDate(Date baseDate, String unit,
			String condition, double value) {
		DateTime baseDateAux = new DateTime(baseDate.getTime());
		DateTime completedDate = baseDateAux;

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
		org.pojava.datetime.DateTime dt = new org.pojava.datetime.DateTime(
				dateString);
		// DateTime dt = new DateTime(dateString);
		return dt.toDate();
	}

	static final ArrayList<String> ORDER = new ArrayList<String>();
	static {
		ORDER.add("shum300");
		ORDER.add("rhum300");
		ORDER.add("rhum500");
		ORDER.add("rhum850");
		ORDER.add("pres");
		ORDER.add("air200");
		ORDER.add("air500");
		ORDER.add("air850");
		ORDER.add("uwnd");
		ORDER.add("uwnd200");
		ORDER.add("uwnd500");
		ORDER.add("uwnd850");
		ORDER.add("vwnd");
		ORDER.add("vwnd200");
		ORDER.add("vwnd500");
		ORDER.add("vwnd850");
		ORDER.add("hgt200");
		ORDER.add("hgt500");
		ORDER.add("hgt850");
	}

	private void consolidateStationsVariables() throws Exception {
		File rootFolder = new File(WORKSPACE);

		// Check stations
		File[] allFiles = rootFolder.listFiles();
		Map<String, List<File>> stationsMap = new HashMap<String, List<File>>();
		for (File file : allFiles) {
			if (file.getName().endsWith("csv")
					&& file.getName().indexOf("-") > 0) {
				String station = file.getName().split("-")[0];
				List<File> stationFiles = stationsMap.get(station);
				if (stationFiles == null) {
					stationFiles = new ArrayList<File>();
				}
				if (!stationFiles.contains(file)) {
					stationFiles.add(file);
				}
				stationsMap.put(station, stationFiles);
			}
		}
		log.debug("Stations map:");
		for (String station : stationsMap.keySet()) {
			log.debug(station + " : " + stationsMap.get(station));
		}

		// Consolidate variables
		for (String station : stationsMap.keySet()) {
			log.debug("Procesing " + station);
			List<File> files = stationsMap.get(station);
			Map<String, List<SimpleEntry<Date, Double>>> variablesMap = new HashMap<String, List<SimpleEntry<Date, Double>>>();
			for (File file : files) {
				String varName = parseVariableName(file);
				List<SimpleEntry<Date, Double>> entries = parseEntries(file);
				variablesMap.put(varName, entries);
			}
			// log.debug(variablesMap);

			// Consolidation structure
			List<String> header = new ArrayList<String>();
			List<SimpleEntry<Date, List<Double>>> entries = new ArrayList<SimpleEntry<Date, List<Double>>>();
			consolidateStation(header, entries, variablesMap);
			saveConsolidateCsv(station, header, entries);
		}
	}

	private void saveConsolidateCsv(String station, List<String> header,
			List<SimpleEntry<Date, List<Double>>> entries) {
		log.debug("Saving consolidate csv... ");

		File rootFolder = new File(WORKSPACE);

		String fileName = station + ".csv";

		File file = new File(rootFolder, fileName);
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();

			for (String head : header) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				if (sb2.length() > 0) {
					sb2.append(",");
				}
				String part[] = head.split("-");
				sb.append(part[0]);
				sb2.append(((part.length > 1) ? part[1] : " "));
			}
			bw.write(sb.toString());
			bw.newLine();
			bw.write(sb2.toString());
			bw.newLine();

			for (SimpleEntry<Date, List<Double>> entry : entries) {
				bw.write(FormatDates.getDateFormat().format(entry.getKey()));
				for (Double val : entry.getValue()) {
					bw.write("," + val);
				}
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private void consolidateStation(List<String> header,
			List<SimpleEntry<Date, List<Double>>> centries,
			Map<String, List<SimpleEntry<Date, Double>>> variablesMap) {

		header.add("date");

		for (String orderVar : ORDER) {
			String varName = null;
			for (String key : variablesMap.keySet()) {
				if (key.split("-")[0].compareToIgnoreCase(orderVar) == 0) {
					varName = key;
					break;
				}
			}
			if (varName == null) {
				throw new RuntimeException("Order Variable note found: "
						+ orderVar);
			}

			List<SimpleEntry<Date, Double>> list = variablesMap.get(varName);
			if (centries.size() == 0) {
				header.add(varName);

				for (SimpleEntry<Date, Double> simpleEntry : list) {
					List<Double> vals = new ArrayList<Double>();
					vals.add(simpleEntry.getValue());
					SimpleEntry<Date, List<Double>> centry = new SimpleEntry<Date, List<Double>>(
							simpleEntry.getKey(), vals);
					centries.add(centry);
				}
			} else {
				List<SimpleEntry<Date, Double>> entries = variablesMap
						.get(varName);
				header.add(varName);
				for (int i = 0; i < centries.size(); i++) {
					SimpleEntry<Date, List<Double>> centry = centries.get(i);
					SimpleEntry<Date, Double> entry = entries.get(i);

					if (centry.getKey().compareTo(entry.getKey()) != 0) {
						throw new RuntimeException("Incompatible dates: " + i);
					}
					centry.getValue().add(entry.getValue());
				}
			}
		}
	}

	private String parseVariableName(File file) {
		String varName = null;

		FileReader fr = null;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String fline = br.readLine();
			varName = fline.split(",")[1];

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return varName;
	}

	private List<SimpleEntry<Date, Double>> parseEntries(File file)
			throws Exception {
		List<SimpleEntry<Date, Double>> entries = null;

		FileReader fr = null;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			entries = new ArrayList<SimpleEntry<Date, Double>>();

			br.readLine(); // Ignore first line

			String line = null;
			while ((line = br.readLine()) != null) {
				String token[] = line.split(",");
				Date date = FormatDates.getDateFormat().parse(token[0]);
				Double val = Double.parseDouble(token[1]);
				SimpleEntry<Date, Double> entry = new SimpleEntry<Date, Double>(
						date, val);
				entries.add(entry);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return entries;
	}

	public void extractTimeSeriesForStations() throws Exception {
		List<SimpleEntry<String, Double[]>> stations = new ArrayList<SimpleEntry<String, Double[]>>();
		// M040 Pasaje
		stations.add(new SimpleEntry<String, Double[]>("M040", new Double[] {
				-3.330, -79.782 }));
		// M067 Cuenca Aeropuerto
		stations.add(new SimpleEntry<String, Double[]>("M067", new Double[] {
				-2.887, -78.983 }));
		// M141 El Labrado
		stations.add(new SimpleEntry<String, Double[]>("M141", new Double[] {
				-2.733, -79.008 }));
		// M142 Saraguro
		stations.add(new SimpleEntry<String, Double[]>("M142", new Double[] {
				-3.621, -79.232 }));
		// M410 Rio Mazar-Rivera
		stations.add(new SimpleEntry<String, Double[]>("M410", new Double[] {
				-2.574, -78.650 }));
		// M419 Girón
		stations.add(new SimpleEntry<String, Double[]>("M419", new Double[] {
				-3.154, -79.149 }));
		stations.add(new SimpleEntry<String, Double[]>("M138", new Double[] {
				-2.778, -78.759 }));
		// M139 Gualaceo
		stations.add(new SimpleEntry<String, Double[]>("M139", new Double[] {
				-2.882, -78.776 }));
		// M600 sevilla
		stations.add(new SimpleEntry<String, Double[]>("M600", new Double[] {
				-2.8008, -78.6552 }));
		// M601 Palmas
		stations.add(new SimpleEntry<String, Double[]>("M601", new Double[] {
				-2.7194, -78.6319 }));
		// M602 Jacarin
		stations.add(new SimpleEntry<String, Double[]>("M602", new Double[] {
				-2.8244, -78.9355 }));
		// M292
		stations.add(new SimpleEntry<String, Double[]>("StaInes", new Double[] {
				-3.287778, -77.901389 }));
		// M185
		stations.add(new SimpleEntry<String, Double[]>("Machala", new Double[] {
				-3.050000, -79.733333 }));

		// Variables
		List<String[]> variables = new ArrayList<String[]>();
		variables
				.add(new String[] { "shum", "300",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/shum.mon.mean.nc" });
		variables
				.add(new String[] { "rhum", "300",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/rhum.mon.mean.nc" });
		variables
				.add(new String[] { "rhum", "500",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/rhum.mon.mean.nc" });
		variables
				.add(new String[] { "rhum", "850",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/rhum.mon.mean.nc" });
		variables
				.add(new String[] { "pres", null,
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/pres.sfc.mon.mean.nc" });
		variables
				.add(new String[] { "air", "200",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/air.mon.mean.nc" });
		variables
				.add(new String[] { "air", "500",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/air.mon.mean.nc" });
		variables
				.add(new String[] { "air", "850",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/air.mon.mean.nc" });
		variables
				.add(new String[] { "uwnd", null,
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/uwnd.sig995.mon.mean.nc" });
		variables
				.add(new String[] { "uwnd", "200",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/uwnd.mon.mean.nc" });
		variables
				.add(new String[] { "uwnd", "500",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/uwnd.mon.mean.nc" });
		variables
				.add(new String[] { "uwnd", "850",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/uwnd.mon.mean.nc" });
		variables
				.add(new String[] { "vwnd", null,
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/vwnd.sig995.mon.mean.nc" });
		variables
				.add(new String[] { "vwnd", "200",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/vwnd.mon.mean.nc" });
		variables
				.add(new String[] { "vwnd", "500",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/vwnd.mon.mean.nc" });
		variables
				.add(new String[] { "vwnd", "850",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/vwnd.mon.mean.nc" });
		variables
				.add(new String[] { "hgt", "200",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/hgt.mon.mean.nc" });
		variables
				.add(new String[] { "hgt", "500",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/hgt.mon.mean.nc" });
		variables
				.add(new String[] { "hgt", "850",
						"C:/Users/Ronald/Desktop/NCEP-NCAR-Reanalysis/hgt.mon.mean.nc" });

		// Dates
		Date startDate = FormatDates.getDateFormat().parse("1964-01-01");
		Date endDate = FormatDates.getDateFormat().parse("2010-12-01");

		for (SimpleEntry<String, Double[]> entry : stations) {
			for (String[] vinfo : variables) {
				getTimeSerie(vinfo[2], startDate, endDate, entry.getValue()[0],
						entry.getValue()[1], vinfo[0], vinfo[1], entry.getKey());
			}
		}

	}

	public static List<SimpleEntry<Date, Double>> aggregateTimeSerie(
			List<SimpleEntry<Date, Double>> timeSerie0,
			Timeperiodicity timeperiodicity) {

		List<SimpleEntry<Date, Double>> agregatedSerie = new ArrayList<SimpleEntry<Date, Double>>();

		int i = 0;
		int j = 0;
		int actualMonth = -1;
		double sum = 0.0;
		Calendar cal = Calendar.getInstance();
		for (SimpleEntry<Date, Double> entry : timeSerie0) {
			Date date = entry.getKey();
			cal.setTime(date);

			if (i == 0) {
				actualMonth = cal.get(Calendar.MONTH);
			}

			if (cal.get(Calendar.MONTH) != actualMonth) {
				// New aggregated value
				cal.set(Calendar.DAY_OF_MONTH, 1);
				// Average
				double avg = sum / j;
				SimpleEntry<Date, Double> aEntry = new SimpleEntry<Date, Double>(
						cal.getTime(), avg);
				agregatedSerie.add(aEntry);
				// Initialize
				actualMonth = cal.get(Calendar.MONTH);
				sum = 0;
				j = 0;
			} else {
				sum += entry.getValue();
				j++;
			}

			i++;
		}

		return agregatedSerie;
	}

	private void saveTimeSerie(List<SimpleEntry<Date, Double>> timeserie,
			String var, String level, String pathOutputFile) {

		File file = new File(pathOutputFile);
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			for (SimpleEntry<Date, Double> entry : timeserie) {
				bw.write(sdf.format(entry.getKey()) + "," + entry.getValue());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	public void extractTimeSeriesForLenin() throws Exception {
		List<SimpleEntry<String, Double[]>> stations = new ArrayList<SimpleEntry<String, Double[]>>();
		/*
		 * 
		 * -79.07305556 -2.732777778 -78.77638889 -2.881944444 -78.76277778 -2.8
		 * -78.9833 -2.8867 -78.62972222 -2.716111111 -78.89166667 -2.708888889
		 */
		stations.add(new SimpleEntry<String, Double[]>("E01", new Double[] {
				-2.732777778, -79.07305556 }));
		stations.add(new SimpleEntry<String, Double[]>("E02", new Double[] {
				-2.881944444, -78.77638889 }));
		stations.add(new SimpleEntry<String, Double[]>("E03", new Double[] {
				-2.8, -78.76277778 }));
		stations.add(new SimpleEntry<String, Double[]>("E04", new Double[] {
				-2.8867, -78.9833 }));
		stations.add(new SimpleEntry<String, Double[]>("E05", new Double[] {
				-2.716111111, -78.62972222 }));
		stations.add(new SimpleEntry<String, Double[]>("E06", new Double[] {
				-2.708888889, -78.89166667 }));

		// Variables
		List<String[]> variables = new ArrayList<String[]>();
		variables.add(new String[] { "rhum", "500",
				"C:/interpolacionLenin/REA_RHUM_500.nc" });
		variables.add(new String[] { "slp", null,
				"C:/interpolacionLenin/REA_SLP.nc" });

		// Dates
		Date startDate = FormatDates.getDateFormat().parse("1981-01-01");
		Date endDate = FormatDates.getDateFormat().parse("2000-12-01");

		Map<String, List<SimpleEntry<String, List<SimpleEntry<Date, Double>>>>> mapTimeSeries = new HashMap<String, List<SimpleEntry<String, List<SimpleEntry<Date, Double>>>>>();

		for (SimpleEntry<String, Double[]> station : stations) {
			List<SimpleEntry<String, List<SimpleEntry<Date, Double>>>> varTs = new ArrayList<SimpleEntry<String,List<SimpleEntry<Date,Double>>>>();

			for (String[] vinfo : variables) {
				List<SimpleEntry<Date, Double>> timeserie = getTimeSerie(
						vinfo[2], startDate, endDate, station.getValue()[0],
						station.getValue()[1], vinfo[0], vinfo[1]);
				varTs.add(new SimpleEntry<String, List<SimpleEntry<Date, Double>>>(
						vinfo[0], timeserie));
			}

			mapTimeSeries.put(station.getKey(), varTs);
		}

		saveTimeSeriesForStationInCsv(mapTimeSeries);

	}

	private void saveTimeSeriesForStationInCsv(
			Map<String, List<SimpleEntry<String, List<SimpleEntry<Date, Double>>>>> mapTimeSeries) {
		String folder = "C:/interpolacionLenin";

		for (String station : mapTimeSeries.keySet()) {
			File file = new File(folder, station + ".csv");
			FileWriter fw = null;
			try {
				fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				List<SimpleEntry<String, List<SimpleEntry<Date, Double>>>> varTs = mapTimeSeries
						.get(station);

				String line = "fecha";
				for (SimpleEntry<String, List<SimpleEntry<Date, Double>>> entry : varTs) {
					if (line.length() != 0) {
						line += ",";
					}
					line += entry.getKey();
				}
				bw.append(line);
				bw.newLine();

				for (int i = 0; i < varTs.get(0).getValue().size(); i++) {
					line = "";
					line += sdf.format(varTs.get(0).getValue().get(i).getKey());

					for (int j = 0; j < varTs.size(); j++) {
						line += ",";
						line += varTs.get(j).getValue().get(i).getValue();
					}
					bw.append(line);
					bw.newLine();
				}

				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}

	}

	public static void main(String[] args) throws Exception {
		/*
		 * TimeSeriesExtractor program = new TimeSeriesExtractor(); String
		 * filePath = "C:/interpolacionLenin/REA_RHUM_500.nc"; String fileOutput
		 * = "C:/interpolacionLenin/REA_RHUM_500.txt";
		 * 
		 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); Date
		 * startDate = sdf.parse("1981-01-01"); Date endDate =
		 * sdf.parse("2000-12-01"); String var = "rhum"; String level = "500";
		 * 
		 * List<SimpleEntry<Date, Double>> timeserie = program.getTimeSerie(
		 * filePath, startDate, endDate, -1.0, 278.0, var, level);
		 * 
		 * program.saveTimeSerie(timeserie, var, level, fileOutput);
		 */

		TimeSeriesExtractor program = new TimeSeriesExtractor();
		program.extractTimeSeriesForLenin();
	}

}
