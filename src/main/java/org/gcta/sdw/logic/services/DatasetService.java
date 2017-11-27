package org.gcta.sdw.logic.services;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gcta.sdw.logic.data.DatasetModel;
import org.gcta.sdw.logic.data.GeneralDatasetModel;
import org.gcta.sdw.persistence.entity.Data;
import org.gcta.sdw.persistence.entity.Dataset;
import org.gcta.sdw.persistence.entity.Datavariable;
import org.gcta.sdw.persistence.entity.Metavariableenum;
import org.gcta.sdw.persistence.entity.Predictand;
import org.gcta.sdw.persistence.entity.Predictor;
import org.gcta.sdw.web.viewmodel.data.MyEntry;
import org.zkoss.util.media.Media;

public interface DatasetService {

	public List<Dataset> queryClimateDatasets();

	public List<Dataset> queryReanalysisDatasets();

	public List<Dataset> queryObservationDatasets();

	public void insert(Data data);

	public void insert(Datavariable selectedVariable);

	public void importTimeSerieFromCsv(Datavariable selectedVariable,
			Media media);

	public void getAvailableVariables(List<Dataset> datasets,
			Map<Dataset, List<Metavariableenum>> availableVarsMap,
			Map<Metavariableenum, List<Long>> availableLevelsMap);

	public void getAvailableVariables(Dataset dataset,
			List<Metavariableenum> availableVars,
			Map<Metavariableenum, List<Long>> availableLevelsMap);

	public List<SimpleEntry<Dataset, Boolean>> checkCompatibleGcms(
			Dataset selectedReanalisys,
			List<MyEntry<Metavariableenum, Long>> selectedVars);

	public List<SimpleEntry<Dataset, Boolean>> checkCompatibleObsDatasets(
			Date startDate, Date endDate);

	public List<Metavariableenum> getAvailablePredictandVariables(
			Dataset selectedObservationDataset);

	public List<Data> getAvailableStations(Dataset selectedObservationDataset,
			Metavariableenum selectedPredictandVariable);

	public Map<Data, DatasetModel> loadTrainDataset(Predictor predictor,
			Predictand predictand) throws Exception;

	public Map<Data, GeneralDatasetModel> loadFutureDataset(
			Predictor predictor, Predictand predictand, Dataset gcm, Date from,
			Date to) throws Exception;

	public List<String> loadAnnTransferFcns() throws Exception;

	public List<String> loadAnnDefaultParameters() throws Exception;

	public File exportDatasetToCsv(GeneralDatasetModel generalDatasetModel,
			String name) throws Exception;

	public List<String> loadSvmKernels() throws Exception;

	public List<String> loadSvmDefaultParameters() throws Exception;

}
