package org.gcta.sdw.logic.model;

/*
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.gcta.logic.dao.ObservationDao;
 import org.gcta.model.system.Configuredforecast;
 import org.gcta.model.system.Model;
 import org.gcta.model.system.Observation;
 import org.gcta.util.FormatDates;
 import org.gcta.util.Log;
 */

public class ModelControler0 {
	/*
	 * 
	 * final static String NAM_PAUTE = "NAM_PAUTE"; final static String
	 * NAM_JUBONES = "NAM_JUBONES"; final static String DBM_JUBONES =
	 * "DBM_JUBONES"; final static String DBM_PAUTE = "DBM_PAUTE"; final static
	 * String ANN_JUBONES = "ANN_JUBONES"; final static String ANN_PAUTE =
	 * "ANN_PAUTE";
	 * 
	 * static Logger log = Log.getInstance();
	 * 
	 * public static void saveDataInputFile(Configuredforecast
	 * configuredForecast, List<List<Observation>> lObservations) throws
	 * Exception { // Set input file path String dataFilePath =
	 * getInputDataPath(configuredForecast.getModel());
	 * log.info("Saving data to file: " + dataFilePath);
	 * 
	 * // Set data according to model String data = "";
	 * 
	 * if (configuredForecast.getModel().getName().startsWith("NAM")) { //
	 * columns: stations, 1 evapotranspiracion // 302 rows. // Get
	 * Evapotranspiration map Map<Integer, Double> mevapo =
	 * ObservationDao.getMevapotranspiration
	 * (configuredForecast.getBasin().getIdBasin());
	 * 
	 * for (int i = 0; i < lObservations.get(0).size(); i++) { String line = "";
	 * for (List<Observation> obs : lObservations) { if (!line.isEmpty()) { line
	 * += "\t"; } String a = "" + obs.get(i).getValue(); line += a; } //
	 * Evapotranspiration !!!! // line += "\t1.225"; // line += "\n"; // data +=
	 * line; //lObservations.get(0).get(i).getId().getDateTime().get; Integer
	 * month = Integer.parseInt(new
	 * SimpleDateFormat("MM").format(lObservations.get
	 * (0).get(i).getId().getDateTime())); Double evapoValue =
	 * mevapo.get(month); line += "\t" + evapoValue + "\n"; data += line;
	 * 
	 * } } else if (configuredForecast.getModel().getName().startsWith("DBM")) {
	 * // 2 columns: mean, caudal // x rows, x>=2 for (int i = 0; i <
	 * lObservations.get(0).size(); i++) { String line = ""; double mean = 0;
	 * double sum = 0; double count = 0; double caudal = 0;
	 * 
	 * for (List<Observation> obs : lObservations) {
	 * 
	 * if (obs.get(i).getTypeofobservation() .getIdTypeOfObservation() == 1) {
	 * sum += obs.get(i).getValue(); count++; } else {
	 * if(obs.get(i).getStation().getIdStation() ==
	 * configuredForecast.getStation().getIdStation() ){ caudal =
	 * obs.get(i).getValue(); } }
	 * 
	 * }
	 * 
	 * mean = sum / count;
	 * 
	 * line += mean;
	 * 
	 * // Caudal line += "\t"; line += caudal; line += "\n"; data += line; } }
	 * else if (configuredForecast.getModel().getName().startsWith("ANN")) { for
	 * (List<Observation> observations : lObservations) { for (Observation
	 * observation : observations) { data += observation.getValue() + "\r\n"; }
	 * } }
	 * 
	 * // Save data FileUtils.writeStringToFile(new File(dataFilePath), data);
	 * log.info("Data saved"); log.debug("Data:"); log.debug(data); }
	 * 
	 * public static Map<Integer, Double> readOutput(Model model) throws
	 * Exception { String filePath = getOutputDataPath(model); String outputData
	 * = FileUtils.readFileToString(new File(filePath));
	 * log.info("Reading output data from: " + filePath);
	 * log.info("Output data: " + outputData);
	 * 
	 * Map<Integer, Double> mOutput = new HashMap<Integer, Double>();
	 * 
	 * if (model.getName().startsWith("NAM")) { String[] lines =
	 * outputData.split("\n"); int i = 1; for (String line : lines) {
	 * mOutput.put(i++, Double.parseDouble(line)); } } else if
	 * (model.getName().startsWith("DBM")) { String[] lines =
	 * outputData.split("\n"); int i = 1; for (String line : lines) {
	 * mOutput.put(i++, Double.parseDouble(line)); } } else if
	 * (model.getName().startsWith("ANN")) { String[] lines =
	 * outputData.split("\n"); int i = 1; for (String line : lines) {
	 * mOutput.put(i++, Double.parseDouble(line)); } }
	 * 
	 * return mOutput; }
	 * 
	 * public static void runModel(Model model) throws Exception {
	 * log.info("Running model: " + model.getFilePath());
	 * 
	 * // Start Long start = System.currentTimeMillis();
	 * 
	 * if (model.getName().startsWith("NAM")) { runPythonModel(model); } else if
	 * (model.getName().startsWith("DBM")) { runPythonModel(model); } else if
	 * (model.getName().startsWith("ANN")) { runMatlabModel(model); }
	 * 
	 * // End log.info("Execution time: " +
	 * FormatDates.getMinuteFormat().format( new Date(System.currentTimeMillis()
	 * - start))); }
	 * 
	 * private static void runPythonModel(Model model) throws Exception { String
	 * command = "python " + model.getFilePath(); runWindowsCommand(command); }
	 * 
	 * private static void runMatlabModel(Model model) throws Exception { String
	 * command =
	 * "matlab -nodisplay -nosplash -nodesktop -wait -minimize -r \"run('" +
	 * model.getFilePath()+ "')\""; runWindowsCommand(command); }
	 * 
	 * public static int runWindowsCommand(String command) throws Exception {
	 * Process p = Runtime.getRuntime().exec(command);
	 * 
	 * BufferedReader stdInput = new BufferedReader(new InputStreamReader(
	 * p.getInputStream()));
	 * 
	 * BufferedReader stdError = new BufferedReader(new InputStreamReader(
	 * p.getErrorStream()));
	 * 
	 * // read the output System.out.println("Output..."); String s; while ((s =
	 * stdInput.readLine()) != null) { System.out.println(s); }
	 * stdInput.close();
	 * 
	 * // read any errors System.out.println("Errors..."); while ((s =
	 * stdError.readLine()) != null) { System.out.println(s); }
	 * stdError.close(); p.waitFor(); System.out.println("Done");
	 * 
	 * log.info("Exit value: " + p.exitValue()); return p.exitValue(); }
	 * 
	 * public static String getInputDataPath(Model model) throws Exception {
	 * String dataFilePath = ""; if (model.getName().startsWith("NAM")) { String
	 * modelPath = model.getFilePath(); // String folderPath =
	 * modelPath.substring(0, // modelPath.lastIndexOf("/") //
	 * >0?modelPath.lastIndexOf("/"):modelPath.lastIndexOf("\\") ); String
	 * folderPath = modelPath.substring(0, modelPath.lastIndexOf("/"));
	 * dataFilePath = folderPath + "/datosNAM.txt"; } else if
	 * (model.getName().startsWith("DBM")) { String modelPath =
	 * model.getFilePath(); String folderPath = modelPath.substring(0,
	 * modelPath.lastIndexOf("/")); dataFilePath = folderPath + "/datosDBM.txt";
	 * } else if (model.getName().startsWith("ANN")) { String modelPath =
	 * model.getFilePath(); String folderPath = modelPath.substring(0,
	 * modelPath.lastIndexOf("/")); dataFilePath = folderPath + "/datosANN.txt";
	 * } else { throw new Exception("Model not supported"); } return
	 * dataFilePath; }
	 * 
	 * public static String getOutputDataPath(Model model) throws Exception {
	 * String dataFilePath = ""; if (model.getName().startsWith("NAM")) { String
	 * modelPath = model.getFilePath(); // String folderPath =
	 * modelPath.substring(0, // modelPath.lastIndexOf("/") //
	 * >0?modelPath.lastIndexOf("/"):modelPath.lastIndexOf("\\") ); String
	 * folderPath = modelPath.substring(0, modelPath.lastIndexOf("/"));
	 * dataFilePath = folderPath + "/resultadoNAM.txt"; } else if
	 * (model.getName().startsWith("DBM")) { String modelPath =
	 * model.getFilePath(); String folderPath = modelPath.substring(0,
	 * modelPath.lastIndexOf("/")); dataFilePath = folderPath +
	 * "/resultadoDBM.txt"; } else if (model.getName().startsWith("ANN")) {
	 * String modelPath = model.getFilePath(); String folderPath =
	 * modelPath.substring(0, modelPath.lastIndexOf("/")); dataFilePath =
	 * folderPath + "/resultadoANN.txt"; } else { throw new
	 * Exception("Model not supported"); } return dataFilePath; }
	 */
}