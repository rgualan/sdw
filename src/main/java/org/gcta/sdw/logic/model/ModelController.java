package org.gcta.sdw.logic.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gcta.sdw.logic.data.DatasetModel;
import org.gcta.sdw.logic.data.GeneralDatasetEntry;
import org.gcta.sdw.logic.data.GeneralDatasetModel;
import org.gcta.sdw.persistence.entity.Data;
import org.gcta.sdw.persistence.entity.Experiment;
import org.gcta.sdw.persistence.entity.Parameter;
import org.gcta.sdw.util.Constant;
import org.gcta.sdw.util.FormatDates;
import org.gcta.sdw.util.Log;
import org.zkoss.io.Files;

public class ModelController {
	static Logger log = Log.getInstance();

	/**
	 * Creates the folder for the experiment create the input data files for ann
	 * model
	 * 
	 * @throws Exception
	 */
	public static void setTrainTestData(Experiment experiment,
			Data selectedStation, Map<Data, DatasetModel> mapDataset)
			throws Exception {

		log.debug("Creating input files for simulation...");

		// Correctly format train and test datasets
		setInputFiles(experiment, selectedStation, mapDataset);
	}

	private static void setInputFiles(Experiment experiment,
			Data selectedStation, Map<Data, DatasetModel> mapDataset)
			throws Exception {

		// Experiment folder
		File experimentFolder = ModelController.getExperimentFolder(experiment);

		File calibrationFile = new File(experimentFolder,
				"inputs_calibration.txt");
		File validationFile = new File(experimentFolder,
				"inputs_validation.txt");

		DatasetModel dataset = mapDataset.get(selectedStation);

		BufferedWriter writer1 = null;
		BufferedWriter writer2 = null;
		try {
			writer1 = new BufferedWriter(new FileWriter(calibrationFile));
			writer2 = new BufferedWriter(new FileWriter(validationFile));

			Double trainPercentage = experiment.getConfiguration()
					.getTestPercentage();
			int totalCounter = dataset.getEntries().size();
			int calibrationCounter, validationCounter;
			calibrationCounter = (int) ((totalCounter * trainPercentage) / 100);
			validationCounter = totalCounter - calibrationCounter;
			Log.getInstance()
					.debug(String
							.format("Total number of registers for calibration and validation: %d",
									totalCounter));
			Log.getInstance().debug(
					String.format("Number of registers for calibration: %d",
							calibrationCounter));
			Log.getInstance().debug(
					String.format("Number of registers for calibration: %d",
							validationCounter));

			StringBuilder sb;
			for (int i = 0; i < totalCounter; i++) {
				GeneralDatasetEntry entry = dataset.getEntries().get(i);
				sb = new StringBuilder();

				for (Double val : entry.getValues()) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					if (val != null) {
						sb.append(val);
					} else {
						sb.append("");
					}

				}

				if (i < calibrationCounter) {
					writer1.write(sb.toString());
					writer1.newLine();
				} else {
					writer2.write(sb.toString());
					writer2.newLine();
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (writer1 != null) {
				writer1.close();
			}
			if (writer2 != null) {
				writer2.close();
			}
		}
	}

	public static void setInputForDownscaling(Experiment experiment,
			Data selectedStation,
			Map<Data, GeneralDatasetModel> mapFutureDataset) throws Exception {

		// Experiment folder
		File experimentFolder = ModelController.getExperimentFolder(experiment);

		File simulationFile = new File(experimentFolder,
				"inputs_forecasting.txt");

		GeneralDatasetModel dataset = mapFutureDataset.get(selectedStation);

		BufferedWriter writer1 = null;
		try {
			writer1 = new BufferedWriter(new FileWriter(simulationFile));

			StringBuilder sb;

			for (GeneralDatasetEntry entry : dataset.getEntries()) {
				sb = new StringBuilder();

				for (Double val : entry.getValues()) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					if (val != null) {
						sb.append(val);
					} else {
						sb.append("");
					}
				}

				writer1.write(sb.toString());
				writer1.newLine();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (writer1 != null) {
				writer1.close();
			}
		}
	}

	public static void setParameters(Experiment experiment) throws Exception {
		// Experiment folder
		File experimentFolder = ModelController.getExperimentFolder(experiment);

		File parametersFile = new File(experimentFolder, "params_ann.txt");
		if (experiment.getConfiguration().getDownscalingmethod().getName()
				.startsWith("SVM")) {
			parametersFile = new File(experimentFolder, "params_svm.txt");
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(parametersFile));

			// Create map
			Map<String, Parameter> mapParameters = new HashMap<>();
			for (Parameter param : experiment.getConfiguration()
					.getParameterList()) {
				mapParameters.put(param.getParameterPK().getParameterId(),
						param);
			}

			// Set parameters
			String line = "";
			if (experiment.getConfiguration().getDownscalingmethod().getName()
					.startsWith("ANN")) {
				line = String.format("%d,%s,%d,%s,%d",
						mapParameters.get("network.layer1.size").getIntValue(),
						mapParameters.get("network.layer1.transferFcn")
								.getStringValue(),
						mapParameters.get("network.layer2.size").getIntValue(),
						mapParameters.get("network.layer2.transferFcn")
								.getStringValue(),
						mapParameters.get("calibration.iterations")
								.getIntValue());
			} else {
				line = String.format("%s", mapParameters.get("svm.kernel")
						.getStringValue());
			}

			writer.write(line);
		} catch (Exception e) {
			throw e;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

	}

	/**
	 * Get a folder/project in ANN tool
	 */
	public static File getExperimentFolder(Experiment selectedExperiment)
			throws Exception {

		File experimentFolder = null;

		if (selectedExperiment.getConfiguration().getDownscalingmethod()
				.getName().startsWith("ANN")) {
			String home = Constant.ANN_HOME;
			File homeFolder = new File(home);

			// Validations
			if (!(homeFolder.exists() || homeFolder.isDirectory())) {
				throw new RuntimeException(
						String.format(
								"Ann home folder <%s> does not exist or is not a folder",
								home));
			}

			// Copy experiment folder from base experiment folder
			File baseExperimentFolder = new File(homeFolder,
					Constant.ANN_BASE_EXPERIMENT_FOLDER_NAME);
			experimentFolder = new File(homeFolder, ""
					+ selectedExperiment.getExperimentId());

			if (!experimentFolder.exists()) {
				Files.copy(experimentFolder, baseExperimentFolder,
						Files.CP_OVERWRITE);
			}
		} else {
			String home = Constant.SVM_HOME;
			File homeFolder = new File(home);

			// Validations
			if (!(homeFolder.exists() || homeFolder.isDirectory())) {
				throw new RuntimeException(
						String.format(
								"SVM home folder <%s> does not exist or is not a folder",
								home));
			}

			// Copy experiment folder from base experiment folder
			File baseExperimentFolder = new File(homeFolder,
					Constant.SVM_BASE_EXPERIMENT_FOLDER_NAME);
			experimentFolder = new File(homeFolder, ""
					+ selectedExperiment.getExperimentId());

			if (!experimentFolder.exists()) {
				Files.copy(experimentFolder, baseExperimentFolder,
						Files.CP_OVERWRITE);
			}

		}

		return experimentFolder;
	}

	public static void runModel(Experiment experiment, Data selectedStation)
			throws Exception {
		log.info("Running model: "
				+ experiment.getConfiguration().getDownscalingmethod()
						.getName());

		// Start
		Long start = System.currentTimeMillis();

		// Run
		runMatlabModel(experiment, selectedStation);

		// End
		log.info(String.format(
				"Execution time: %s",
				FormatDates.getMinuteFormat().format(
						new Date(System.currentTimeMillis() - start))));
	}

	private static void runMatlabModel(Experiment experiment,
			Data selectedStation) throws Exception {

		String filePath = "";

		if (experiment.getConfiguration().getDownscalingmethod().getName()
				.startsWith("ANN")) {
			filePath = getExperimentFolder(experiment).getAbsolutePath()
					+ "/" + Constant.ANN_SCRIPT_NAME;
		}else{
			filePath = getExperimentFolder(experiment).getAbsolutePath()
					+ "/" + Constant.SVM_SCRIPT_NAME;
		}

		

		String command = "matlab -nodisplay -nosplash -nodesktop -wait -minimize -r \"run('"
				+ filePath + "')\"";

		runWindowsCommand(command);
	}

	public static int runWindowsCommand(String command) throws Exception {
		log.debug("Running: " + command);

		Process p = Runtime.getRuntime().exec(command);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));

		// read the output
		System.out.println("Output...");
		String s;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
		stdInput.close();

		// read any errors
		System.out.println("Errors...");
		while ((s = stdError.readLine()) != null) {
			System.out.println(s);
		}
		stdError.close();
		p.waitFor();
		System.out.println("Done");

		log.info("Exit value: " + p.exitValue());
		return p.exitValue();
	}

	public static List<Double> getTrainTestOutput(Experiment experiment,
			Data selectedStationForPreview) throws Exception {
		List<Double> calibrationOutput = new ArrayList<>();
		List<Double> validationOutput = new ArrayList<>();
		List<Double> output = new ArrayList<>();

		// Experiment folder
		File experimentFolder = ModelController.getExperimentFolder(experiment);

		File calibrationOutputFile = new File(experimentFolder,
				"outputs_calibration_sim.txt");
		File validationOutputFile = new File(experimentFolder,
				"outputs_validation_sim.txt");

		BufferedReader reader1 = null;
		BufferedReader reader2 = null;

		try {
			// Read calibration output
			reader1 = new BufferedReader(new FileReader(calibrationOutputFile));
			String line = null;
			Double val = null;
			while ((line = reader1.readLine()) != null) {
				val = Double.parseDouble(line);
				calibrationOutput.add(val);
			}

			// Read validation output
			reader2 = new BufferedReader(new FileReader(validationOutputFile));
			while ((line = reader2.readLine()) != null) {
				val = Double.parseDouble(line);
				validationOutput.add(val);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (reader1 != null) {
				reader1.close();
			}
			if (reader2 != null) {
				reader2.close();
			}
		}

		// Output = calibration data + validation data
		if (calibrationOutput.size() > 0 || validationOutput.size() > 0) {
			output.addAll(calibrationOutput);
			output.addAll(validationOutput);
		}

		return output;
	}

	public static List<Double> getDownscalingOutput(Experiment experiment,
			Data selectedStation) throws Exception {
		List<Double> downscalingOutput = new ArrayList<>();

		// Experiment folder
		File experimentFolder = ModelController.getExperimentFolder(experiment);

		File downscalingOutputFile = new File(experimentFolder,
				"outputs_forcasting_sim.txt");

		BufferedReader reader = null;

		try {
			// Read simulation output
			reader = new BufferedReader(new FileReader(downscalingOutputFile));
			String line = null;
			Double val = null;
			while ((line = reader.readLine()) != null) {
				val = Double.parseDouble(line);
				downscalingOutput.add(val);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return downscalingOutput;
	}

	public static String readLog(Experiment experiment, Data selectedStation)
			throws Exception {
		// Experiment folder
		File experimentFolder = ModelController.getExperimentFolder(experiment);

		
		File logFile = new File(experimentFolder, "ann.log");
		if(experiment.getConfiguration().getDownscalingmethod().getName().startsWith("SVM")){
			logFile = new File(experimentFolder, "svm.log");
		}

		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			// Read log
			reader = new BufferedReader(new FileReader(logFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		String filePath = "C:/sdw-workspace/ANN/base/ann.m";
		String command = "matlab -nodisplay -nosplash -nodesktop -wait -minimize -r \"run('"
				+ filePath + "')\"";

		// Start
		Long start = System.currentTimeMillis();

		runWindowsCommand(command);

		// End
		log.info(String.format(
				"Execution time: %s",
				FormatDates.getMinuteFormat().format(
						new Date(System.currentTimeMillis() - start))));

	}
}