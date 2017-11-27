package org.gcta.sdw.logic.enums;

import ucar.nc2.constants.AxisType;

public enum DimensionEnum {
	LATITUDE(AxisType.Lat.name()), 
	LONGITUDE(AxisType.Lon.name()), 
	PRESSURE(AxisType.Pressure.name()), 
	TIME(AxisType.Time.name());

	private final String dimension;

	private DimensionEnum(String name) {
		this.dimension = name;

	}

	public String getDimension() {
		return dimension;
	}

}
