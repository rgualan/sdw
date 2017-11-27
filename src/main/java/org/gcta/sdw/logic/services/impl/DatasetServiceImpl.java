package org.gcta.sdw.logic.services.impl;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gcta.sdw.logic.data.DatasetModel;
import org.gcta.sdw.logic.data.GeneralDatasetModel;
import org.gcta.sdw.logic.services.DatasetService;
import org.gcta.sdw.persistence.dao.DatasetDao;
import org.gcta.sdw.persistence.entity.Data;
import org.gcta.sdw.persistence.entity.Dataset;
import org.gcta.sdw.persistence.entity.Datavariable;
import org.gcta.sdw.persistence.entity.Metavariableenum;
import org.gcta.sdw.persistence.entity.Predictand;
import org.gcta.sdw.persistence.entity.Predictor;
import org.gcta.sdw.web.viewmodel.data.MyEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.zkoss.util.media.Media;

@Service("datasetService")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	DatasetDao datasetDao;

	@Override
	public List<Dataset> queryClimateDatasets() {
		return datasetDao.queryClimate();
	}

	@Override
	public List<Dataset> queryReanalysisDatasets() {
		return datasetDao.queryReanalysis();
	}

	@Override
	public List<Dataset> queryObservationDatasets() {
		return datasetDao.queryObservations();
	}

	@Override
	public void insert(Data data) {
		datasetDao.insert(data);
	}

	@Override
	public void insert(Datavariable variable) {
		datasetDao.insert(variable);
	}

	@Override
	public void importTimeSerieFromCsv(Datavariable variable, Media media) {
		datasetDao.importTimeSerieFromCsv(variable, media);

	}

	@Override
	public void getAvailableVariables(List<Dataset> datasets,
			Map<Dataset, List<Metavariableenum>> availableVarsMap,
			Map<Metavariableenum, List<Long>> availableLevelsMap) {

		datasetDao.getAvailableVariables(datasets, availableVarsMap,
				availableLevelsMap);
	}

	@Override
	public void getAvailableVariables(Dataset dataset,
			List<Metavariableenum> availableVars,
			Map<Metavariableenum, List<Long>> availableLevelsMap) {
		datasetDao.getAvailableVariables(dataset, availableVars,
				availableLevelsMap);
	}

	@Override
	public List<SimpleEntry<Dataset, Boolean>> checkCompatibleGcms(
			Dataset reanalisys,
			List<MyEntry<Metavariableenum, Long>> selectedVars) {
		return datasetDao.checkCompatibleGcms(reanalisys, selectedVars);
	}

	@Override
	public List<SimpleEntry<Dataset, Boolean>> checkCompatibleObsDatasets(
			Date startDate, Date endDate) {
		return datasetDao.checkCompatibleObsDatasets(startDate, endDate);
	}

	@Override
	public List<Metavariableenum> getAvailablePredictandVariables(
			Dataset selectedObservationDataset) {
		return datasetDao
				.getAvailablePredictandVariables(selectedObservationDataset);
	}

	@Override
	public List<Data> getAvailableStations(Dataset selectedObservationDataset,
			Metavariableenum selectedPredictandVariable) {
		return datasetDao.getAvailableStations(selectedObservationDataset,
				selectedPredictandVariable);
	}

	@Override
	public Map<Data, DatasetModel> loadTrainDataset(Predictor predictor,
			Predictand predictand) throws Exception {
		return datasetDao.loadTrainDataset(predictor, predictand);
	}

	@Override
	public Map<Data, GeneralDatasetModel> loadFutureDataset(
			Predictor predictor, Predictand predictand, Dataset gcm, Date from,
			Date to) throws Exception {
		return datasetDao.loadFutureDataset(predictor, predictand, gcm, from,
				to);

	}

	@Override
	public List<String> loadAnnTransferFcns() throws Exception {
		return datasetDao.loadAnnTransferFcns();
	}

	@Override
	public List<String> loadAnnDefaultParameters() throws Exception {
		return datasetDao.loadAnnDefaultParameters();
	}

	@Override
	public File exportDatasetToCsv(GeneralDatasetModel generalDatasetModel, String name)
			throws Exception {
		return datasetDao.exportDatasetToCsv(generalDatasetModel, name);
	}

	@Override
	public List<String> loadSvmKernels() throws Exception {
		return datasetDao.loadSvmKernels();
	}

	@Override
	public List<String> loadSvmDefaultParameters() throws Exception {
		return datasetDao.loadSvmDefaultParameters();
	}
}
