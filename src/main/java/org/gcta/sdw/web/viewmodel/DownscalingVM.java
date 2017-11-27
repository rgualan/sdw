package org.gcta.sdw.web.viewmodel;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.gcta.sdw.logic.data.DatasetModel;
import org.gcta.sdw.logic.data.GeneralDatasetEntry;
import org.gcta.sdw.logic.data.GeneralDatasetModel;
import org.gcta.sdw.logic.model.ModelController;
import org.gcta.sdw.logic.services.DatasetService;
import org.gcta.sdw.logic.services.PersistenceService;
import org.gcta.sdw.persistence.entity.Configuration;
import org.gcta.sdw.persistence.entity.Data;
import org.gcta.sdw.persistence.entity.Dataset;
import org.gcta.sdw.persistence.entity.Downscalingmethod;
import org.gcta.sdw.persistence.entity.Experiment;
import org.gcta.sdw.persistence.entity.Metavariableenum;
import org.gcta.sdw.persistence.entity.Parameter;
import org.gcta.sdw.persistence.entity.ParameterPK;
import org.gcta.sdw.persistence.entity.Predictand;
import org.gcta.sdw.persistence.entity.Predictor;
import org.gcta.sdw.persistence.entity.PredictorHasPredictors;
import org.gcta.sdw.persistence.entity.PredictorHasPredictorsPK;
import org.gcta.sdw.util.Log;
import org.gcta.sdw.web.viewmodel.data.MyEntry;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DownscalingVM {

	// Services
	@WireVariable
	private PersistenceService persistenceService;

	@WireVariable
	private DatasetService datasetService;

	// Fields
	private List<Experiment> experiments;
	private Experiment selectedExperiment;

	private Predictor predictor;
	private Predictand predictand;

	private List<MyEntry<Metavariableenum, Long>> selectedVars;
	private MyEntry<Metavariableenum, Long> selectedVar;

	private List<SimpleEntry<Dataset, Boolean>> compatibleGcms;
	private List<Dataset> availableGcms;

	private List<SimpleEntry<Dataset, Boolean>> compatibleObsDatasets;
	private List<Dataset> availableObsDataset;

	private List<Dataset> reanalisysGcms;
	private List<Dataset> climateGcms;
	private List<Dataset> observationSets;

	private Dataset selectedReanalisys;
	private Dataset selectedObservationDataset;

	private List<Metavariableenum> availableVars;
	private Metavariableenum selectedAvailableVar;

	private Map<Metavariableenum, List<Long>> availableLevelsMap;
	private List<Long> selectedAvailableLevels;

	private List<Metavariableenum> availablePredictandVariables;

	// Predictand fields
	private Metavariableenum selectedPredictandVariable;
	private List<Data> availableStations;
	private List<Data> selectedStations;

	// Configuration - Downscaling method
	private Downscalingmethod selectedDmethod;
	// ANN
	private Configuration configuration;
	private List<Downscalingmethod> downscalingMethods;
	private List<String> transferFcns;
	private Map<String, Parameter> mapParameters;
	// SVM
	private List<String> svmKernels;

	// Downscale
	private Data selectedStationForPreview;
	private Map<Data, DatasetModel> mapTrainDataset;
	private Map<Data, GeneralDatasetModel> mapFutureDataset;
	private Dataset gcmForDownscaling;
	private Date simulationStartDate;
	private Date simulationEndDate;
	private String downscaleLog;

	// Tab control
	private int activatedTabs = 1;
	private int predictorMode = 0;
	private int predictandMode = 0;
	private int configurationMode = 0;

	// Dialog controls (0:invisible 1:new 2:edit 3:view)
	private int experimentDialogMode = 0;

	// -------------------------------------------------------------------
	// Getters & setters
	// -------------------------------------------------------------------
	public List<Experiment> getExperiments() {
		return experiments;
	}

	public void setExperiments(List<Experiment> experiments) {
		this.experiments = experiments;
	}

	public Experiment getSelectedExperiment() {
		return selectedExperiment;
	}

	@Command
	@NotifyChange({ "*" })
	public void setSelectedExperiment(Experiment selectedExperiment)
			throws Exception {
		init();

		Log.getInstance().debug("Actual experiment: " + selectedExperiment);
		this.selectedExperiment = selectedExperiment;

		if (selectedExperiment != null) {
			openExperiment();
		} else {
			clearAll();
		}

	}

	public List<Dataset> getReanalisysGcms() {
		return reanalisysGcms;
	}

	public void setReanalisysGcms(List<Dataset> reanalisysGcms) {
		this.reanalisysGcms = reanalisysGcms;
	}

	public Metavariableenum getSelectedAvailableVar() {
		return selectedAvailableVar;
	}

	public void setSelectedAvailableVar(Metavariableenum selectedAvailableVar) {
		this.selectedAvailableVar = selectedAvailableVar;
		this.selectedAvailableLevels = null;
	}

	public List<Long> getSelectedAvailableLevels() {
		return selectedAvailableLevels;
	}

	public void setSelectedAvailableLevels(List<Long> selectedAvailableLevels) {
		this.selectedAvailableLevels = selectedAvailableLevels;
	}

	public List<MyEntry<Metavariableenum, Long>> getSelectedVars() {
		return selectedVars;
	}

	public void setSelectedVars(
			List<MyEntry<Metavariableenum, Long>> selectedVars) {
		this.selectedVars = selectedVars;
	}

	public MyEntry<Metavariableenum, Long> getSelectedVar() {
		return selectedVar;
	}

	public void setSelectedVar(MyEntry<Metavariableenum, Long> selectedVar) {
		this.selectedVar = selectedVar;
	}

	public List<SimpleEntry<Dataset, Boolean>> getCompatibleGcms() {
		return compatibleGcms;
	}

	@NotifyChange({ "compatibleGcms", "availableGcms" })
	public void setCompatibleGcms(
			List<SimpleEntry<Dataset, Boolean>> compatibleGcms) {
		this.compatibleGcms = compatibleGcms;

		setAvailableGcms(null);

		if (compatibleGcms != null && compatibleGcms.size() > 0) {
			List<Dataset> availables = new ArrayList<Dataset>();
			for (SimpleEntry<Dataset, Boolean> cg : compatibleGcms) {
				if (cg.getValue()) {
					availables.add(cg.getKey());
				}
			}
			setAvailableGcms(availables);
		}

	}

	public List<SimpleEntry<Dataset, Boolean>> getCompatibleObsDatasets() {
		return compatibleObsDatasets;
	}

	@NotifyChange({ "compatibleObsDatasets", "availableObsDataset" })
	public void setCompatibleObsDatasets(
			List<SimpleEntry<Dataset, Boolean>> aCompatibleObsDatasets) {
		this.compatibleObsDatasets = aCompatibleObsDatasets;

		setAvailableObsDataset(null);

		if (compatibleObsDatasets != null && compatibleObsDatasets.size() > 0) {
			List<Dataset> availables = new ArrayList<Dataset>();
			for (SimpleEntry<Dataset, Boolean> co : compatibleObsDatasets) {
				if (co.getValue()) {
					availables.add(co.getKey());
				}
			}
			setAvailableObsDataset(availables);
		}
	}

	public List<Dataset> getAvailableObsDataset() {
		return availableObsDataset;
	}

	public void setAvailableObsDataset(List<Dataset> availableObsDataset) {
		this.availableObsDataset = availableObsDataset;
	}

	public List<Dataset> getObservationSets() {
		return observationSets;
	}

	public void setObservationSets(List<Dataset> observationSets) {
		this.observationSets = observationSets;
	}

	public List<Data> getSelectedStations() {
		return selectedStations;
	}

	public void setSelectedStations(List<Data> selectedStations) {
		this.selectedStations = selectedStations;
	}

	public Predictor getPredictor() {
		// Log.getInstance().debug("Get predictor" + predictor);
		// if (predictor != null) {
		// Log.getInstance().debug(predictor.getDataset());
		// }
		return predictor;
	}

	@NotifyChange({ "predictor", "selectedReanalisys", "electedVars",
			"compatibleGcms", "availableGcms" })
	public void setPredictor(Predictor predictor) {
		this.predictor = predictor;

		// clear
		this.setSelectedReanalisys(null);
		this.setSelectedVars(null);
		this.setCompatibleGcms(null);
		this.setCompatibleObsDatasets(null);

		if (predictor == null) {
			return;
		}

		// set
		this.setSelectedReanalisys(predictor.getDataset());

		if (predictor.getPredictorHasPredictorsList() != null
				&& predictor.getPredictorHasPredictorsList().size() > 0) {
			Log.getInstance().debug("Loading selected variables...");

			// Load the predictor variables in a ordered map by order field
			TreeMap<Integer, MyEntry<Metavariableenum, Long>> mVars = new TreeMap<Integer, MyEntry<Metavariableenum, Long>>();

			for (PredictorHasPredictors php : predictor
					.getPredictorHasPredictorsList()) {
				MyEntry<Metavariableenum, Long> entry = new MyEntry<Metavariableenum, Long>(
						php.getMetavariableenum().getStdVarId() + "-"
								+ php.getPredictorHasPredictorsPK().getLevel(),
						php.getMetavariableenum(), php
								.getPredictorHasPredictorsPK().getLevel());
				mVars.put(php.getOrderNum(), entry);
			}

			// Create an array
			selectedVars = new ArrayList<MyEntry<Metavariableenum, Long>>();
			for (Integer order : mVars.keySet()) {
				MyEntry<Metavariableenum, Long> entry = mVars.get(order);
				selectedVars.add(entry);
			}

			checkCompatibleGcms();
		}

		// Compatible observation datasets
		if (predictor.getStartDate() != null && predictor.getEndDate() != null) {
			setCompatibleObsDatasets(datasetService.checkCompatibleObsDatasets(
					predictor.getStartDate(), predictor.getEndDate()));
		}

	}

	public Predictand getPredictand() {
		return predictand;
	}

	@NotifyChange({ "predictand", "selectedObservationDataset",
			"selectedPredictandVariable", "selectedStations",
			"availablePredictandVariables", "availableStations" })
	public void setPredictand(Predictand predictand) {
		this.predictand = predictand;

		setSelectedObservationDataset(null);
		setSelectedPredictandVariable(null);
		setSelectedStations(null);

		if (this.predictand == null) {
			return;
		}

		setSelectedObservationDataset(predictand.getDataset());
		setSelectedPredictandVariable(predictand.getMetavariableenum());
		setSelectedStations(predictand.getDataList());

	}

	public int getExperimentDialogMode() {
		return experimentDialogMode;
	}

	public void setExperimentDialogMode(int experimentDialogMode) {
		this.experimentDialogMode = experimentDialogMode;
	}

	public List<Dataset> getClimateGcms() {
		return climateGcms;
	}

	public void setClimateGcms(List<Dataset> climateGcms) {
		this.climateGcms = climateGcms;
	}

	public List<Metavariableenum> getAvailableVars() {
		return availableVars;
	}

	public void setAvailableVars(List<Metavariableenum> availableVars) {
		this.availableVars = availableVars;
	}

	public Map<Metavariableenum, List<Long>> getAvailableLevelsMap() {
		return availableLevelsMap;
	}

	public void setAvailableLevelsMap(
			Map<Metavariableenum, List<Long>> availableLevelsMap) {
		this.availableLevelsMap = availableLevelsMap;
	}

	public int getPredictorMode() {
		return predictorMode;
	}

	public void setPredictorMode(int predictorMode) {
		this.predictorMode = predictorMode;
	}

	public Dataset getSelectedReanalisys() {
		return selectedReanalisys;
	}

	@NotifyChange({ "selectedReanalisys", "predictor", "availableVars",
			"availableLevelsMap" })
	public void setSelectedReanalisys(Dataset selectedReanalisys) {
		// Log.getInstance().debug("setSelectedReanalisys" +
		// selectedReanalisys);
		this.selectedReanalisys = selectedReanalisys;

		setAvailableVars(null);
		setAvailableLevelsMap(null);

		if (selectedReanalisys == null) {
			return;
		}

		// Set predictor variables from reanalisys
		// during GUI interaction
		if (predictor != null) {
			// this.predictor.setDataset(this.selectedReanalisys);
			if (predictor.getStartDate() == null
					&& predictor.getEndDate() == null) {
				Log.getInstance().info("Seting dates...");
				predictor.setStartDate(selectedReanalisys.getDateA());
				predictor.setEndDate(selectedReanalisys.getDateB());
			}

			// Set spatial coverage
			predictor.setLongStart(selectedReanalisys.getLongA());
			predictor.setLongEnd(selectedReanalisys.getLongB());
			predictor.setLongRes(selectedReanalisys.getLongDelta());
			predictor.setLatStart(selectedReanalisys.getLatA());
			predictor.setLatEnd(selectedReanalisys.getLatB());
			predictor.setLatRes(selectedReanalisys.getLatDelta());
		}

		Log.getInstance().info("Quering available vars...");
		availableVars = new ArrayList<Metavariableenum>();
		availableLevelsMap = new HashMap<Metavariableenum, List<Long>>();
		datasetService.getAvailableVariables(selectedReanalisys, availableVars,
				availableLevelsMap);

		Log.getInstance().debug("Available variables: ");
		for (Metavariableenum var : availableVars) {
			Log.getInstance().debug(var);
		}
		Log.getInstance().debug("Available levels per variable: ");
		for (Metavariableenum var : availableLevelsMap.keySet()) {
			Log.getInstance().debug(var + " : " + availableLevelsMap.get(var));
		}

	}

	public Dataset getSelectedObservationDataset() {
		return selectedObservationDataset;
	}

	@NotifyChange({ "selectedObservationDataset",
			"availablePredictandVariables", "selectedPredictandVariable",
			"availableStations", "selectedStations" })
	public void setSelectedObservationDataset(Dataset selectedObservationDataset) {
		// Log.getInstance().debug(
		// "setSelectedObservationDataset " + selectedObservationDataset);
		this.selectedObservationDataset = selectedObservationDataset;

		setAvailablePredictandVariables(null);
		setSelectedPredictandVariable(null);
		setAvailableStations(null);
		setSelectedStations(null);

		if (selectedObservationDataset == null) {
			return;
		}

		// Load available predictand variables from selected dataset
		this.availablePredictandVariables = datasetService
				.getAvailablePredictandVariables(this.selectedObservationDataset);

		Log.getInstance().debug("Available predictand variables:");
		Log.getInstance().debug(availablePredictandVariables);
	}

	public int getActivatedTabs() {
		return activatedTabs;
	}

	public void setActivatedTabs(int activatedTabs) {
		this.activatedTabs = activatedTabs;
	}

	public int getPredictandMode() {
		return predictandMode;
	}

	public void setPredictandMode(int predictandMode) {
		this.predictandMode = predictandMode;
	}

	public List<Metavariableenum> getAvailablePredictandVariables() {
		return availablePredictandVariables;
	}

	public void setAvailablePredictandVariables(
			List<Metavariableenum> availablePredictandVariables) {
		this.availablePredictandVariables = availablePredictandVariables;
	}

	public List<Data> getAvailableStations() {
		return availableStations;
	}

	public void setAvailableStations(List<Data> availableStations) {
		this.availableStations = availableStations;
	}

	public Metavariableenum getSelectedPredictandVariable() {
		return selectedPredictandVariable;
	}

	@NotifyChange({ "selectedPredictandVariable", "availableStations",
			"selectedObservationDataset", "selectedStations" })
	public void setSelectedPredictandVariable(
			Metavariableenum selectedPredictandVariable) {
		this.selectedPredictandVariable = selectedPredictandVariable;

		this.setAvailableStations(null);
		this.setSelectedStations(null);

		if (this.selectedPredictandVariable == null) {
			return;
		}

		// Load Available Stations
		if (this.selectedObservationDataset != null
				&& this.selectedPredictandVariable != null) {
			availableStations = datasetService.getAvailableStations(
					this.selectedObservationDataset,
					this.selectedPredictandVariable);
		}
	}

	public List<Downscalingmethod> getDownscalingMethods() {
		return downscalingMethods;
	}

	public void setDownscalingMethods(List<Downscalingmethod> downscalingMethods) {
		this.downscalingMethods = downscalingMethods;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@NotifyChange({ "configuration", "mapParameters", "selectedDmethod" })
	public void setConfiguration(Configuration configuration) throws Exception {
		this.configuration = configuration;

		if (configuration != null
				&& configuration.getDownscalingmethod() != null) {
			this.setSelectedDmethod(configuration.getDownscalingmethod());

			mapParameters = null;

			if (configuration != null
					&& configuration.getParameterList() != null) {
				mapParameters = new HashMap<>();
				for (Parameter param : configuration.getParameterList()) {
					mapParameters.put(param.getParameterPK().getParameterId(),
							param);
				}
			}
		}
	}

	public int getConfigurationMode() {
		return configurationMode;
	}

	public void setConfigurationMode(int configurationMode) {
		this.configurationMode = configurationMode;
	}

	public Data getSelectedStationForPreview() {
		return selectedStationForPreview;
	}

	public void setSelectedStationForPreview(Data selectedStationForPreview) {
		this.selectedStationForPreview = selectedStationForPreview;
	}

	public Map<Data, DatasetModel> getMapTrainDataset() {
		return mapTrainDataset;
	}

	public void setMapTrainDataset(Map<Data, DatasetModel> mapTrainDataset) {
		this.mapTrainDataset = mapTrainDataset;
	}

	public Map<Data, GeneralDatasetModel> getMapFutureDataset() {
		return mapFutureDataset;
	}

	public void setMapFutureDataset(
			Map<Data, GeneralDatasetModel> mapFutureDataset) {
		this.mapFutureDataset = mapFutureDataset;
	}

	public List<Dataset> getAvailableGcms() {
		return availableGcms;
	}

	public void setAvailableGcms(List<Dataset> availableGcms) {
		this.availableGcms = availableGcms;
	}

	public Dataset getGcmForDownscaling() {
		return gcmForDownscaling;
	}

	@NotifyChange({ "gcmForDownscaling", "simulationStartDate",
			"simulationEndDate" })
	public void setGcmForDownscaling(Dataset gcmForDownscaling) {
		this.gcmForDownscaling = gcmForDownscaling;

		if (this.gcmForDownscaling == null) {
			return;
		}

		if (simulationStartDate == null && simulationEndDate == null) {
			setSimulationStartDate(gcmForDownscaling.getDateA());
			setSimulationEndDate(gcmForDownscaling.getDateB());
		}
	}

	public Date getSimulationStartDate() {
		return simulationStartDate;
	}

	public void setSimulationStartDate(Date simulationStartDate) {
		this.simulationStartDate = simulationStartDate;
	}

	public Date getSimulationEndDate() {
		return simulationEndDate;
	}

	public void setSimulationEndDate(Date simulationEndDate) {
		this.simulationEndDate = simulationEndDate;
	}

	public String getDownscaleLog() {
		return downscaleLog;
	}

	public void setDownscaleLog(String downscaleLog) {
		this.downscaleLog = downscaleLog;
	}

	public List<String> getTransferFcns() {
		return transferFcns;
	}

	public void setTransferFcns(List<String> transferFcns) {
		this.transferFcns = transferFcns;
	}

	public Map<String, Parameter> getMapParameters() {
		return mapParameters;
	}

	public void setMapParameters(Map<String, Parameter> mapParameters) {
		this.mapParameters = mapParameters;
	}

	public List<String> getSvmKernels() {
		return svmKernels;
	}

	public void setSvmKernels(List<String> svmKernels) {
		this.svmKernels = svmKernels;
	}

	public Downscalingmethod getSelectedDmethod() {
		return selectedDmethod;
	}

	@NotifyChange({ "selectedDmethod", "configuration", "mapParameters",
			"transferFcns", "svmKernels" })
	public void setSelectedDmethod(Downscalingmethod method) throws Exception {
		this.selectedDmethod = method;
		// this.mapParameters;

		setMapParameters(null);

		// Load default parameters
		if (method != null) {
			if (method.getName() != null
					&& method.getName().toLowerCase().compareTo("ann") == 0) {
				// Load transfer functions from text file
				transferFcns = datasetService.loadAnnTransferFcns();
				// Load default parameters
				List<String> defaultParameters = datasetService
						.loadAnnDefaultParameters();

				Map<String, Parameter> mapParameters = new HashMap<>();
				// 0
				ParameterPK paramPK = new ParameterPK(
						selectedExperiment.getExperimentId(),
						"network.layer1.size");
				Parameter param = new Parameter(paramPK);
				param.setIntValue(Integer.parseInt(defaultParameters.get(0)));
				mapParameters.put(paramPK.getParameterId(), param);
				// 1
				paramPK = new ParameterPK(selectedExperiment.getExperimentId(),
						"network.layer1.transferFcn");
				param = new Parameter(paramPK);
				param.setStringValue(defaultParameters.get(1));
				mapParameters.put(paramPK.getParameterId(), param);
				// 2
				paramPK = new ParameterPK(selectedExperiment.getExperimentId(),
						"network.layer2.size");
				param = new Parameter(paramPK);
				param.setIntValue(Integer.parseInt(defaultParameters.get(2)));
				mapParameters.put(paramPK.getParameterId(), param);
				// 3
				paramPK = new ParameterPK(selectedExperiment.getExperimentId(),
						"network.layer2.transferFcn");
				param = new Parameter(paramPK);
				param.setStringValue(defaultParameters.get(3));
				mapParameters.put(paramPK.getParameterId(), param);
				// 4
				paramPK = new ParameterPK(selectedExperiment.getExperimentId(),
						"calibration.iterations");
				param = new Parameter(paramPK);
				param.setIntValue(Integer.parseInt(defaultParameters.get(4)));
				mapParameters.put(paramPK.getParameterId(), param);

				if (getConfiguration() != null) {
					getConfiguration().setParameterList(
							new ArrayList<Parameter>(mapParameters.values()));
				}
				this.setMapParameters(mapParameters);
			} else if (method.getName() != null
					&& method.getName().toLowerCase().compareTo("svm") == 0) {
				// Load kernels
				svmKernels = datasetService.loadSvmKernels();

				// Load default parameters
				List<String> defaultParameters = datasetService
						.loadSvmDefaultParameters();

				Map<String, Parameter> mapParameters = new HashMap<>();
				// 0
				ParameterPK paramPK = new ParameterPK(
						selectedExperiment.getExperimentId(), "svm.kernel");
				Parameter param = new Parameter(paramPK);
				param.setStringValue(defaultParameters.get(0));
				mapParameters.put(paramPK.getParameterId(), param);

				if (getConfiguration() != null) {
					getConfiguration().setParameterList(
							new ArrayList<Parameter>(mapParameters.values()));
				}
				this.setMapParameters(mapParameters);

			}
		}
	}

	@Init
	public void init() throws Exception {
		Log.getInstance().info("Loading init config...");
		// Experiments
		experiments = persistenceService.queryAll(Experiment.class);

		// references
		reanalisysGcms = datasetService.queryReanalysisDatasets();
		climateGcms = datasetService.queryClimateDatasets();
		observationSets = datasetService.queryObservationDatasets();

		downscalingMethods = persistenceService
				.queryAll(Downscalingmethod.class);

	}

	// -------------------------------------------------------------------
	// MVVM
	// -------------------------------------------------------------------

	// -------------------------------------------------------------------
	// Experiment commands
	// -------------------------------------------------------------------
	@Command
	@NotifyChange({ "experiments" })
	public void refreshExperiments() throws Exception {
		init();
	}

	@Command
	@NotifyChange({ "selectedExperiment", "experimentDialogMode", "predictor",
			"predictand" })
	public void newExperiment() throws Exception {
		selectedExperiment = new Experiment();
		selectedExperiment.setCreationDate(new Date());
		// selectedExperiment.setCreationDate(Calendar.getInstance().getTime());

		experimentDialogMode = 1;
	}

	@Command
	@NotifyChange({ "*" })
	public void saveExperiment() throws Exception {
		// Persist
		persistenceService.insert(selectedExperiment);

		// Init
		init();

		experimentDialogMode = 0;

		// Clear all
		clearAll();

		// Open EMPTY experiment
		// openExperiment();

		// Activated tabs
		activatedTabs = 2;
	}

	@Command
	@NotifyChange({ "*" })
	public void openExperiment() throws Exception {

		// Predictor
		setPredictor(selectedExperiment.getPredictor());

		// Predictand
		setPredictand(selectedExperiment.getPredictand());

		// Configuration
		setConfiguration(selectedExperiment.getConfiguration());
	}

	@Command
	@NotifyChange({ "*" })
	public void clearAll() throws Exception {
		Log.getInstance().info("Clearing all variables...");

		// Predictor
		setPredictor(null);

		// Predictand
		setPredictand(null);

		// Configuration
		setConfiguration(null);

		// Downscaling variables
		setSelectedStationForPreview(null);
		setSimulationStartDate(null);
		setSimulationEndDate(null);
		setMapTrainDataset(null);
		setMapFutureDataset(null);
		setDownscaleLog(null);
	}

	@Command
	@NotifyChange({ "*" })
	public void deleteExperiment() throws Exception {
		persistenceService.delete(selectedExperiment);
		selectedExperiment = null;
		init();
		clearAll();
	}

	@Command
	@NotifyChange({ "experimentDialogMode" })
	public void closeExperimentDialog() throws Exception {
		experimentDialogMode = 0;
	}

	// -------------------------------------------------------------------
	// Predictor
	// -------------------------------------------------------------------
	@Command
	@NotifyChange({ "selectedExperiment", "predictor", "predictorMode" })
	public void createPredictor() throws Exception {
		Log.getInstance().info("Create predictor...");
		Predictor newPredictor = new Predictor(
				selectedExperiment.getExperimentId());

		this.getSelectedExperiment().setPredictor(newPredictor);
		this.setPredictor(newPredictor);

		this.predictorMode = 1;
	}

	@Command
	@NotifyChange({ "selectedExperiment", "predictor", "selectedReanalisys",
			"predictorMode", "selectedVars" })
	public void savePredictor() throws Exception {
		Log.getInstance().info("Save predictor...");

		// Set reanalisys
		predictor.setDataset(selectedReanalisys);

		List<PredictorHasPredictors> lphp = new ArrayList<PredictorHasPredictors>();
		int order = 0;
		for (MyEntry<Metavariableenum, Long> entry : selectedVars) {
			PredictorHasPredictorsPK phpk = new PredictorHasPredictorsPK(
					selectedExperiment.getExperimentId(), entry.getValue0()
							.getStdVarId(), entry.getValue1());
			PredictorHasPredictors php = new PredictorHasPredictors(phpk);
			php.setPredictor(predictor);
			php.setMetavariableenum(entry.getValue0());
			php.setOrderNum(order++);
			lphp.add(php);
		}
		predictor.setPredictorHasPredictorsList(lphp);

		persistenceService.insert(predictor);

		this.setPredictorMode(0);
	}

	@Command
	@NotifyChange({ "selectedAvailableVar", "selectedAvailableLevels",
			"selectedVars", "compatibleGcms", "availableGcms",
			"selectedReanalisys" })
	public void addVariable() throws Exception {

		List<MyEntry<Metavariableenum, Long>> selEntries = new ArrayList<>();

		for (Long level : selectedAvailableLevels) {
			MyEntry<Metavariableenum, Long> selVar = new MyEntry<Metavariableenum, Long>(
					selectedAvailableVar.getStdVarId() + " - " + level,
					selectedAvailableVar, level);
			selEntries.add(selVar);
		}

		if (selectedVars == null) {
			selectedVars = new ArrayList<MyEntry<Metavariableenum, Long>>();
		}

		for (MyEntry<Metavariableenum, Long> selVar : selEntries) {
			if (!selectedVars.contains(selVar)) {
				selectedVars.add(selVar);
			}
		}

		setSelectedAvailableVar(null);
		setSelectedAvailableLevels(null);

		checkCompatibleGcms();
	}

	@Command
	@NotifyChange({ "predictor", "compatibleObsDatasets", "availableObsDataset" })
	public void checkCompatibleObsDatasetCmd() throws Exception {
		if (predictor == null) {
			return;
		}

		checkCompatibleObsDatasets();
	}

	private void checkCompatibleGcms() {
		setCompatibleGcms(datasetService.checkCompatibleGcms(
				selectedReanalisys, selectedVars));
	}

	private void checkCompatibleObsDatasets() {
		setCompatibleObsDatasets(datasetService.checkCompatibleObsDatasets(
				predictor.getStartDate(), predictor.getEndDate()));
	}

	@Command
	@NotifyChange({ "selectedVars", "selectedVar" })
	public void upSelectedVariable() throws Exception {
		int i = selectedVars.indexOf(selectedVar);
		if (i <= 0)
			return;
		selectedVars.remove(selectedVar);
		selectedVars.add(--i, selectedVar);
	}

	@Command
	@NotifyChange({ "selectedVars", "selectedVar" })
	public void downSelectedVariable() throws Exception {
		int i = selectedVars.indexOf(selectedVar);
		if (i < 0 || i == selectedVars.size() - 1)
			return;
		selectedVars.remove(selectedVar);
		selectedVars.add(++i, selectedVar);
	}

	@Command
	@NotifyChange({ "selectedVars", "selectedVar", "compatibleGcms" })
	public void deleteSelectedVariable() throws Exception {
		selectedVars.remove(selectedVar);
		selectedVar = null;

		checkCompatibleGcms();
	}

	// -------------------------------------------------------------------
	// Predictand
	// -------------------------------------------------------------------
	@Command
	@NotifyChange({ "selectedExperiment", "predictand", "predictandMode" })
	public void createPredictand() throws Exception {
		Log.getInstance().debug("Create predictand...");

		Predictand newPredictand = new Predictand(
				selectedExperiment.getExperimentId());

		this.getSelectedExperiment().setPredictand(newPredictand);
		this.setPredictand(newPredictand);

		this.predictandMode = 1;
	}

	@Command
	@NotifyChange({ "selectedExperiment", "predictand",
			"selectedPredictandVariable", "selectedStations", "predictandMode" })
	public void savePredictand() throws Exception {
		Log.getInstance().info("Save predictand...");

		// Set dataset
		predictand.setDataset(selectedObservationDataset);

		// Set predictand variable
		predictand.setMetavariableenum(selectedPredictandVariable);

		// Save predictand's stations
		predictand.setDataList(this.selectedStations);

		// Save predictand
		persistenceService.insert(this.predictand);
		selectedExperiment.setPredictand(predictand);

		// Close editable mode
		this.setPredictandMode(0);
	}

	@Command
	@NotifyChange({ "selectedExperiment", "predictor", "selectedReanalisys",
			"predictorMode", "selectedVars", "predictand",
			"selectedPredictandVariable", "selectedStations", "predictandMode" })
	public void savePredictorPredictand() throws Exception {
		Log.getInstance().info("Save predictor and predictand...");

		savePredictor();

		savePredictand();
	}

	// -------------------------------------------------------------------
	// Configuration
	// -------------------------------------------------------------------
	@Command
	@NotifyChange({ "selectedExperiment", "configuration", "selectedDmethod",
			"configurationMode", "transferFcns", "kernels", "mapParameters" })
	public void createConfiguration() throws Exception {
		Log.getInstance().debug("Create configuration...");

		// Configuration
		Configuration newConfig = new Configuration(
				selectedExperiment.getExperimentId());
		newConfig.setTestPercentage(75.0);
		setConfiguration(newConfig);

		// Default statistical downscaling method: ANN
		Downscalingmethod ann = new Downscalingmethod("1");
		ann.setName("ANN");
		setSelectedDmethod(ann);

		this.configurationMode = 1;
	}

	@Command
	@NotifyChange({ "selectedExperiment", "configuration", "configurationMode",
			"mapParameters" })
	public void debug() throws Exception {
		Log.getInstance().info("Debugging...");

		Log.getInstance().debug(
				mapParameters.get("network.layer1.size").getIntValue());
	}

	@Command
	@NotifyChange({ "selectedExperiment", "configuration", "configurationMode",
			"mapParameters", "selectedDmethod" })
	public void saveConfiguration() throws Exception {
		Log.getInstance().info("Save configuration...");

		getConfiguration().setDownscalingmethod(getSelectedDmethod());

		// Save configuration
		persistenceService.insert(this.configuration);
		selectedExperiment.setConfiguration(this.configuration);

		// Close editable mode
		this.setConfigurationMode(0);
	}

	// -------------------------------------------------------------------
	// Downscale
	// -------------------------------------------------------------------
	@Command
	@NotifyChange({ "mapTrainDataset", "predictor", "predictand" })
	public void loadTrainDataset() throws Exception {
		Log.getInstance().debug("Loading train datasets...");
		mapTrainDataset = datasetService
				.loadTrainDataset(predictor, predictand);

	}

	private GeneralDatasetModel createGeneralDataset(List<Double> output) {
		GeneralDatasetModel model = null;
		List<String> columns = new ArrayList<>();
		List<GeneralDatasetEntry> entries = new ArrayList<>();

		columns.add("simulated");

		for (Double d : output) {
			List<Double> entryVals = new ArrayList<>();
			entryVals.add(d);
			GeneralDatasetEntry entry = new GeneralDatasetEntry(null, entryVals);
			entries.add(entry);
		}

		model = new GeneralDatasetModel(columns, entries);

		return model;
	}

	@Command
	@NotifyChange({ "mapFutureDataset", "predictor", "predictand",
			"gcmForDownscaling", "simulationStartDate", "simulationEndDate" })
	public void loadFutureDataset() throws Exception {
		Log.getInstance().info("Loading future dataset...");

		mapFutureDataset = datasetService.loadFutureDataset(predictor,
				predictand, gcmForDownscaling, simulationStartDate,
				simulationEndDate);

	}

	@Command
	@NotifyChange({ "mapTrainDataset", "selectedStationForPreview" })
	public void exportTrainDataset() throws Exception {
		Log.getInstance().info("Exporting train dataset...");

		String fileName = "TrainTest.csv";

		File csvFile = datasetService.exportDatasetToCsv(
				mapTrainDataset.get(selectedStationForPreview), fileName);

		Filedownload.save(csvFile, "csv");
	}

	@Command
	@NotifyChange({ "mapFutureDataset", "selectedStationForPreview" })
	public void exportFutureDataset() throws Exception {
		Log.getInstance().info("Exporting future dataset...");

		String fileName = "FutureTest.csv";

		File csvFile = datasetService.exportDatasetToCsv(
				mapFutureDataset.get(selectedStationForPreview), fileName);

		Filedownload.save(csvFile, "csv");
	}

	@Command
	@NotifyChange({ "selectedExperiment", "selectedStationForPreview",
			"mapTrainDataset", "mapFutureDataset", "downscaleLog" })
	public void downscale() throws Exception {
		Log.getInstance().debug("Begin training, testing and forcasting...");

		// Set input data for training and testing
		ModelController.setTrainTestData(selectedExperiment,
				selectedStationForPreview, mapTrainDataset);

		// Set input data for downscaling
		ModelController.setInputForDownscaling(selectedExperiment,
				selectedStationForPreview, mapFutureDataset);

		ModelController.setParameters(selectedExperiment);

		// Run ANN model in matlab
		// TODO: Check posible errors
		ModelController.runModel(selectedExperiment, selectedStationForPreview);

		// Get output information
		// training and testing output
		List<Double> trainTestOutput = ModelController.getTrainTestOutput(
				selectedExperiment, selectedStationForPreview);
		if (trainTestOutput.size() != mapTrainDataset
				.get(selectedStationForPreview).getEntries().size()) {
			throw new RuntimeException(
					String.format(
							"Ann output size is not equal than input size (input size:%d, output size:%d)",
							mapTrainDataset.get(selectedStationForPreview)
									.getEntries().size(),
							trainTestOutput.size()));
		}

		GeneralDatasetModel simulatedTrainTestDataset = createGeneralDataset(trainTestOutput);
		mapTrainDataset.get(selectedStationForPreview).addDataset(
				simulatedTrainTestDataset);

		// forecasting output
		List<Double> forecastingOutput = ModelController.getDownscalingOutput(
				selectedExperiment, selectedStationForPreview);

		if (forecastingOutput.size() != mapFutureDataset
				.get(selectedStationForPreview).getEntries().size()) {
			throw new RuntimeException(
					String.format(
							"Ann output size is not equal than input size for future dataset (input size:%d, output size:%d)",
							mapFutureDataset.get(selectedStationForPreview)
									.getEntries().size(),
							forecastingOutput.size()));
		}

		GeneralDatasetModel simulatedDownscalingDataset = createGeneralDataset(forecastingOutput);
		mapFutureDataset.get(selectedStationForPreview).addDataset(
				simulatedDownscalingDataset);

		// Read the log
		downscaleLog = ModelController.readLog(selectedExperiment,
				selectedStationForPreview);

	}

	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days); // minus number would decrement the days
		return cal.getTime();
	}

}