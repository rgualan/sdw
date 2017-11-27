package org.gcta.sdw.logic.services;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.gcta.sdw.persistence.entity.Data;
import org.gcta.sdw.persistence.entity.Dataset;
import org.gcta.sdw.persistence.entity.Datavariable;
import org.zkoss.util.media.Media;

public interface EtlService {

	public static String LEVEL_SEPARATOR = ",";

	void saveNetcdfFile(Dataset selectedDataset, Media media) throws Exception;

	public Map<File, List<File>> getPreloadedGcms();

	void saveNetcdfFiles(Dataset selectedDataset,
			List<File> selectedPreloadedGcms) throws Exception;

	public List<Data> getPreloadedStations() throws Exception;

	void saveStations(Dataset selectedDataset,
			List<Data> selectedPreloadedStations) throws Exception;

	// For observations dataset
	public void setDatavariableMetadata(Datavariable selectedVariable)
			throws Exception;

	public void setDatasetMetadataFromDatavariable(Dataset selectedDataset,
			Datavariable selectedVariable) throws Exception;


}
