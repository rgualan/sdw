package org.gcta.sdw.web.viewmodel;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.gcta.sdw.logic.services.DatasetService;
import org.gcta.sdw.logic.services.EtlService;
import org.gcta.sdw.logic.services.PersistenceService;
import org.gcta.sdw.persistence.entity.Center;
import org.gcta.sdw.persistence.entity.Data;
import org.gcta.sdw.persistence.entity.DataPK;
import org.gcta.sdw.persistence.entity.Dataset;
import org.gcta.sdw.persistence.entity.Datasettype;
import org.gcta.sdw.persistence.entity.Datavariable;
import org.gcta.sdw.persistence.entity.DatavariablePK;
import org.gcta.sdw.persistence.entity.Metavariableenum;
import org.gcta.sdw.persistence.entity.Scenario;
import org.gcta.sdw.util.Log;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ClimateDatasetVM {

	// Services

	@WireVariable
	private PersistenceService persistenceService;

	@WireVariable
	private EtlService etlService;

	@WireVariable
	private DatasetService datasetService;

	// Fields

	private Integer datasetType = 1;

	private List<Dataset> datasets;

	private Dataset selectedDataset;
	private Data selectedData;
	private Datavariable selectedVariable;

	// References

	private List<Scenario> scenarios;
	private List<Center> centers;
	private List<Metavariableenum> standardizedVars;

	// Preloaded Gcms
	private Map<File, List<File>> preRepo;
	private File selectedPreRepo;
	private List<File> selectedPreloadedGcms;

	// Preloaded stations
	private List<Data> preloadedStations;
	private List<Data> selectedPreloadedStations;

	// Dialog controls
	// 0:invisible 1:new 2:edit 3:view
	private int datasetDialogMode = 0;
	private int dataDialogMode = 0;
	private int variableDialogMode = 0;
	private int preloadedGcmsMode = 0;
	private int preloadedStationsMode = 0;

	public ClimateDatasetVM() {
	}

	// -------------------------------------------------------------------
	// Getters & setters
	// -------------------------------------------------------------------

	public Integer getDatasetType() {
		return datasetType;
	}

	public void setDatasetType(Integer datasetType) {
		this.datasetType = datasetType;
	}

	public List<Dataset> getDatasets() {
		return datasets;
	}

	@NotifyChange({ "datasets", "selectedDataset", "selectedData",
			"selectedVariable" })
	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;

		if (selectedDataset != null) {
			int i = datasets.indexOf(selectedDataset);
			if (i >= 0) {
				setSelectedDataset(datasets.get(i));
			} else {
				setSelectedDataset(null);
			}
		}
	}

	public Dataset getSelectedDataset() {
		return selectedDataset;
	}

	@NotifyChange({ "selectedDataset", "selectedData", "selectedVariable" })
	public void setSelectedDataset(Dataset selectedDataset_) {
		this.selectedDataset = selectedDataset_;

		if (selectedDataset != null && selectedData != null
				&& selectedDataset.getDataList() != null) {
			int j = selectedDataset.getDataList().indexOf(selectedData);
			if (j >= 0) {
				setSelectedData(selectedDataset.getDataList().get(j));
			} else {
				setSelectedData(null);
			}
		}
	}

	public Data getSelectedData() {
		return selectedData;
	}

	@NotifyChange({ "selectedData", "selectedVariable" })
	public void setSelectedData(Data selectedData) {
		this.selectedData = selectedData;

		if (selectedDataset != null && selectedData != null
				&& selectedVariable != null
				&& selectedData.getDatavariableList() != null) {
			int k = selectedData.getDatavariableList()
					.indexOf(selectedVariable);
			if (k >= 0) {
				selectedVariable = selectedData.getDatavariableList().get(k);
			} else {
				selectedVariable = null;
			}
		}

	}

	public Datavariable getSelectedVariable() {
		return selectedVariable;
	}

	public void setSelectedVariable(Datavariable selectedVariable) {
		this.selectedVariable = selectedVariable;
	}

	public int getDatasetDialogMode() {
		return datasetDialogMode;
	}

	public void setDatasetDialogMode(int datasetDialogMode) {
		this.datasetDialogMode = datasetDialogMode;
	}

	public int getDataDialogMode() {
		return dataDialogMode;
	}

	public void setDataDialogMode(int dataDialogMode) {
		this.dataDialogMode = dataDialogMode;
	}

	public int getVariableDialogMode() {
		return variableDialogMode;
	}

	public void setVariableDialogMode(int variableDialogMode) {
		this.variableDialogMode = variableDialogMode;
	}

	public List<Scenario> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}

	public List<Center> getCenters() {
		return centers;
	}

	public void setCenters(List<Center> centers) {
		this.centers = centers;
	}

	public List<Metavariableenum> getStandardizedVars() {
		return standardizedVars;
	}

	public void setStandardizedVars(List<Metavariableenum> standardizedVars) {
		this.standardizedVars = standardizedVars;
	}

	// -------------------------------------------------------------------
	// MVVM
	// -------------------------------------------------------------------

	public int getPreloadedGcmsMode() {
		return preloadedGcmsMode;
	}

	public void setPreloadedGcmsMode(int preloadedGcmsMode) {
		this.preloadedGcmsMode = preloadedGcmsMode;
	}

	public List<File> getSelectedPreloadedGcms() {
		return selectedPreloadedGcms;
	}

	public void setSelectedPreloadedGcms(List<File> selectedPreloadedGcms) {
		this.selectedPreloadedGcms = selectedPreloadedGcms;
	}

	public Map<File, List<File>> getPreRepo() {
		return preRepo;
	}

	public void setPreRepo(Map<File, List<File>> preRepo) {
		this.preRepo = preRepo;
	}

	public File getSelectedPreRepo() {
		return selectedPreRepo;
	}

	public void setSelectedPreRepo(File selectedPreRepo) {
		this.selectedPreRepo = selectedPreRepo;
	}

	public List<Data> getPreloadedStations() {
		return preloadedStations;
	}

	public void setPreloadedStations(List<Data> preloadedStations) {
		this.preloadedStations = preloadedStations;
	}

	public List<Data> getSelectedPreloadedStations() {
		return selectedPreloadedStations;
	}

	public void setSelectedPreloadedStations(
			List<Data> selectedPreloadedStations) {
		this.selectedPreloadedStations = selectedPreloadedStations;
	}

	public int getPreloadedStationsMode() {
		return preloadedStationsMode;
	}

	public void setPreloadedStationsMode(int preloadedStationsMode) {
		this.preloadedStationsMode = preloadedStationsMode;
	}

	@Init
	public void init(@ExecutionArgParam("datasetType") Integer type) {
		datasetType = type;
		init();
	}

	@NotifyChange({ "datasets", "selectedDataset", "selectedData",
			"selectedVariable" })
	public void init() {
		// required lists
		scenarios = persistenceService.queryAll(Scenario.class);
		centers = persistenceService.queryAll(Center.class);
		standardizedVars = persistenceService.queryAll(Metavariableenum.class);

		// datasets
		if (datasetType == 1) {
			setDatasets(datasetService.queryClimateDatasets());
		} else if (datasetType == 2) {
			setDatasets(datasetService.queryReanalysisDatasets());
		} else if (datasetType == 3) {
			setDatasets(datasetService.queryObservationDatasets());
		}

	}

	// -------------------------------------------------------------------
	// Dataset commands
	// -------------------------------------------------------------------

	@Command
	@NotifyChange({ "selectedDataset", "datasetDialogMode" })
	public void addDataset() {
		setSelectedDataset(new Dataset());

		Datasettype type = new Datasettype();
		type.setDsTypeId(this.datasetType);

		selectedDataset.setDatasettype(type);
		datasetDialogMode = 1;
	}

	@Command
	@NotifyChange({ "datasetDialogMode" })
	public void editDataset() {
		datasetDialogMode = 2;
	}

	@Command
	@NotifyChange({ "datasets", "selectedDataset" })
	public void deleteDataset() throws Exception {
		persistenceService.delete(selectedDataset);
		init();
	}

	@Command
	@NotifyChange({ "datasets", "selectedDataset" })
	public void refreshDataset() {
		init();
	}

	@Command
	@NotifyChange({ "selectedDataset", "datasetDialogMode", "datasets" })
	public void saveDataset() throws Exception {
		if (datasetDialogMode == 1) {
			persistenceService.insert(selectedDataset);
		} else if (datasetDialogMode == 2) {
			persistenceService.update(selectedDataset);
		}

		datasetDialogMode = 0;

		init();
	}

	@Command
	@NotifyChange({ "datasetDialogMode" })
	public void closeDatasetDialog() {
		datasetDialogMode = 0;
	}

	// -------------------------------------------------------------------
	// Data commands
	// -------------------------------------------------------------------

	@Command
	@NotifyChange({ "dataDialogMode" })
	public void moreDataInfo() {
		dataDialogMode = 3;
	}

	@Command
	@NotifyChange({ "selectedData", "dataDialogMode" })
	public void addData() throws Exception {
		Data newData = new Data();
		DataPK newDataPK = new DataPK();
		newDataPK.setDatasetId(selectedDataset.getDatasetId());
		newData.setDataPK(newDataPK);

		newData.setDataset(selectedDataset);

		setSelectedData(newData);

		dataDialogMode = 1;
	}

	@Command
	@NotifyChange({ "dataDialogMode" })
	public void editData() throws Exception {
		dataDialogMode = 2;
	}

	@Command
	@NotifyChange({ "dataDialogMode", "datasets", "selectedDataset",
			"selectedData", "selectedVariable" })
	public void saveData() throws Exception {
		if (dataDialogMode == 1) {
			datasetService.insert(selectedData);
		} else if (dataDialogMode == 2) {
			persistenceService.update(selectedData);
		}

		dataDialogMode = 0;

		init();
	}

	@Command
	@NotifyChange({ "datasets", "selectedDataset" })
	public void deleteData() throws Exception {
		persistenceService.delete(selectedData);
		init();
	}

	@Command
	@NotifyChange({ "dataDialogMode" })
	public void closeDataDialog() {
		dataDialogMode = 0;
	}

	@Command
	@NotifyChange({ "datasets", "selectedDataset" })
	public void uploadFile(@BindingParam("media") Media media) throws Exception {
		Log.getInstance().debug("Uploaded file - name: " + media.getName());
		Log.getInstance().debug(
				"Uploaded file - content type: " + media.getContentType());
		Log.getInstance().debug("Uploaded file - format: " + media.getFormat());

		etlService.saveNetcdfFile(selectedDataset, media);
		// etlService.saveNetcdfFile(datasets.get(selectedDatasetIndex), media);

		// init(datasetType);
		init();
	}

	// Preloaded GCMs

	@Command
	@NotifyChange({ "preloadedGcmsMode", "preRepo", "selectedPreRepo",
			"selectedPreloadedGcms" })
	public void showPreloaded() throws Exception {
		preRepo = etlService.getPreloadedGcms();
		selectedPreRepo = null;
		selectedPreloadedGcms = null;

		preloadedGcmsMode = 1;
	}

	@Command
	@NotifyChange({ "preloadedGcmsMode", "selectedPreloadedGcms",
			"selectedDataset", "datasets", "selectedDataset" })
	public void loadPreloadedGcms() throws Exception {
		etlService.saveNetcdfFiles(selectedDataset, selectedPreloadedGcms);
		preloadedGcmsMode = 0;

		init();
	}

	@Command
	@NotifyChange({ "preloadedGcmsMode", "selectedPreloadedGcms" })
	public void closePreloadedGcms() throws Exception {
		preloadedGcmsMode = 0;
		selectedPreloadedGcms = null;
	}

	// Preloaded Stations

	@Command
	@NotifyChange({ "preloadedStations", "selectedPreloadedStations",
			"preloadedStationsMode" })
	public void showPreloadedStations() throws Exception {
		preloadedStations = etlService.getPreloadedStations();
		selectedPreloadedStations = preloadedStations;
		preloadedStationsMode = 1;
	}

	@Command
	@NotifyChange({ "selectedDataset", "preloadedStations",
			"selectedPreloadedStations", "preloadedStationsMode", "datasets",
			"selectedData", "selectedVariable" })
	public void loadPreloadedStations() throws Exception {
		Log.getInstance().debug("Inserting preloaded stations...");
		for (Data station : selectedPreloadedStations) {
			Log.getInstance().debug("Station: " + station.getName());
			DataPK pk = new DataPK();
			pk.setDatasetId(selectedDataset.getDatasetId());
			station.setDataPK(pk);
			station.setDataset(selectedDataset);
			datasetService.insert(station);
		}
		preloadedStations = null;
		selectedPreloadedStations = null;
		preloadedStationsMode = 0;
		init();
	}

	@Command
	@NotifyChange({ "preloadedStationsMode", "selectedPreloadedStations" })
	public void closePreloadedStations() throws Exception {
		preloadedStationsMode = 0;
		selectedPreloadedStations = null;
	}

	// -------------------------------------------------------------------
	// Variable commands
	// -------------------------------------------------------------------
	@Command
	@NotifyChange({ "variableDialogMode" })
	public void moreVariable() {
		variableDialogMode = 3;
	}

	@Command
	@NotifyChange({ "variableDialogMode" })
	public void closeVariableDialog() {
		variableDialogMode = 0;
	}

	@Command
	@NotifyChange({ "selectedVariable", "variableDialogMode" })
	public void addVariable() throws Exception {
		Datavariable newVariable = new Datavariable();
		DatavariablePK newDatavariablePK = new DatavariablePK();
		newDatavariablePK.setDatasetId(selectedDataset.getDatasetId());
		newDatavariablePK.setDataId(selectedData.getDataPK().getDataId());
		newVariable.setDatavariablePK(newDatavariablePK);

		newVariable.setData(selectedData);

		setSelectedVariable(newVariable);

		variableDialogMode = 1;
	}

	@Command
	@NotifyChange({ "variableDialogMode" })
	public void editVariable() throws Exception {
		variableDialogMode = 2;
	}

	@Command
	@NotifyChange({ "variableDialogMode", "datasets", "selectedDataset",
			"selectedData", "selectedVariable" })
	public void saveVariable() throws Exception {

		// Saving
		if (variableDialogMode == 1) {
			datasetService.insert(selectedVariable);
		} else if (variableDialogMode == 2) {
			persistenceService.update(selectedVariable);
		}

		variableDialogMode = 0;

		init();
	}

	@Command
	@NotifyChange({ "datasets", "selectedDataset", "selectedVariable" })
	public void deleteVariable() throws Exception {
		persistenceService.delete(selectedVariable);
		init();
	}

	@Command
	@NotifyChange({ "selectedVariable" })
	public void uploadTimeSerieCsv(@BindingParam("media") Media media)
			throws Exception {
		Log.getInstance().debug("Uploaded CSV - name: " + media.getName());
		Log.getInstance().debug(
				"Uploaded file - content type: " + media.getContentType());
		Log.getInstance().debug("Uploaded file - format: " + media.getFormat());

		// Set time serie list
		datasetService.importTimeSerieFromCsv(selectedVariable, media);

		// Set metadata
		etlService.setDatavariableMetadata(selectedVariable);
	}

	// -------------------------------------------------------------------
	// Extras
	// -------------------------------------------------------------------

	@Command
	public String concat(String... strings) {
		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			sb.append(string);
		}

		return sb.toString();
	}
}