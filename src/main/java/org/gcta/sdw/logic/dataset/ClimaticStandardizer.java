package org.gcta.sdw.logic.dataset;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gcta.sdw.persistence.entity.Metavariableenum;
import org.gcta.sdw.util.Log;

public class ClimaticStandardizer {

	public static Long stdLevel(String level, String levelUnits) {
		Long val = null;
		double factor = getConversionFactor(levelUnits);
		// Log.getInstance().debug("Factor: " + factor);
		// Log.getInstance().debug("Level: " + Long.parseLong(level));
		// Log.getInstance().debug("Product: " + Long.parseLong(level) *
		// factor);
		val = (long) (Long.parseLong(level) * factor);

		return val;
	}

	public static Long stdLevel(Long level, String units) {
		Long val = null;

		double factor = getConversionFactor(units);
		val = (long) (level * factor);

		return val;
	}

	private static double getConversionFactor(String units) {
		double cf = 1;

		if (units.compareToIgnoreCase("pa") == 0) {
			cf = 1.0 / 100.0;
		} else {
			// hPa, mbar
			cf = 1;
		}

		return cf;
	}

	public static List<SimpleEntry<Date, Double>> stdVariable(
			List<SimpleEntry<Date, Double>> originalList,
			Metavariableenum metavariableenum, String units) {
		Log.getInstance().debug(
				"Standardizing a list of variables: "
						+ metavariableenum.getStdVarId());

		List<SimpleEntry<Date, Double>> list = new ArrayList<SimpleEntry<Date, Double>>();

		Double old, neu;
		for (SimpleEntry<Date, Double> oldEntry : originalList) {
			old = oldEntry.getValue();
			neu = stdVariable(old, metavariableenum, units);
			SimpleEntry<Date, Double> newEntry = new SimpleEntry<Date, Double>(
					oldEntry.getKey(), neu);
			list.add(newEntry);
		}

		return list;
	}

	private static Double stdVariable(Double old,
			Metavariableenum metavariableenum, String units) {
		Double val = old;

		// 'hur', 'Relative humidity'
		// 'hus', 'Specific humidity'
		// 'pr', 'Precipitation flux'
		// 'ps', 'Surface Pressure'
		// 'psl', 'Sea Level Pressure'
		// 'ta', 'Air temperature'
		// 'ts', 'Surface temperature'
		// 'ua', 'U velocity'
		// 'uas', 'Superficial U velocity'
		// 'va', 'V velocity'
		// 'vas', 'Superficial V velocity'
		// 'zg', 'Geopotencial'

		String var = metavariableenum.getStdVarId();
		if (var.compareToIgnoreCase("ta") == 0
				|| var.compareToIgnoreCase("ts") == 0) {
			// Temperature
			// Standard unit: degC
			if (units.compareToIgnoreCase("degc") == 0
					|| units.compareToIgnoreCase("degc") == 0) {
				return val;
			} else {
				// Conversions
				if (units.compareToIgnoreCase("k") == 0
						|| units.compareToIgnoreCase("kelvin") == 0) {
					val = old - 273.15;
					return val;
				}
			}
		} else if (var.compareToIgnoreCase("pr") == 0) {
			// Precipitation
			// Standard unit: mm
			if (units.compareToIgnoreCase("mm") == 0) {
				return val;
			} else {

			}
		} else if (var.compareToIgnoreCase("ps") == 0
				|| var.compareToIgnoreCase("psl") == 0) {
			// Pressure
			// Standard unit: mbar
			if (units.compareToIgnoreCase("mbar") == 0
					|| units.compareToIgnoreCase("millibar") == 0
					|| units.compareToIgnoreCase("millibars") == 0
					|| units.compareToIgnoreCase("hpa") == 0) {
				return val;
			} else {
				// Conversions
				if (units.compareToIgnoreCase("pa") == 0) {
					val = old / 100;
					return val;
				}
			}
		} else if (var.compareToIgnoreCase("zg") == 0) {
			// Geopotencial height
			// Standard unit: m
			if (units.compareToIgnoreCase("m") == 0
					|| units.compareToIgnoreCase("meter") == 0
					|| units.compareToIgnoreCase("meters") == 0) {
				return val;
			} else {
				// Conversions
				if (units.compareToIgnoreCase("km") == 0) {
					val = old * 1000;
					return val;
				}
			}
		} else if (var.compareToIgnoreCase("hur") == 0) {
			// Relative humidity
			// Standard unit: 1
			if (units.compareToIgnoreCase("1") == 0) {
				return val;
			} else {
				// Conversions
				if (units.compareToIgnoreCase("%") == 0) {
					val = old / 100;
					return val;
				}
			}
		} else if (var.compareToIgnoreCase("hus") == 0) {
			// Specific humidity
			// Standard unit: Kg Kg-1
			// TODO Check it
			if (units.compareToIgnoreCase("kg/kg") == 0
					|| units.compareToIgnoreCase("kg kg-1") == 0) {
				return val;
			} else {
				// Conversions
				if (units.compareToIgnoreCase("grams/kg") == 0
						|| units.compareToIgnoreCase("grams kg-1") == 0) {
					val = old / 1000;
					return val;
				}
			}
		} else if (var.compareToIgnoreCase("ua") == 0
				|| var.compareToIgnoreCase("uas") == 0
				|| var.compareToIgnoreCase("va") == 0
				|| var.compareToIgnoreCase("vas") == 0) {
			// Wind velocity
			// Standard unit: m s-1
			if (units.compareToIgnoreCase("m s-1") == 0
					|| units.compareToIgnoreCase("m/s") == 0) {
				return val;
			}
		}

		Log.getInstance().warn(
				String.format("Unit not mapped for %s: %s", var, units));

		return val;
	}

	public static String getStdUnit(Metavariableenum metavariableenum) {
		String var = metavariableenum.getStdVarId();
		if (var.compareToIgnoreCase("ta") == 0
				|| var.compareToIgnoreCase("ts") == 0) {
			return "degC";
		} else if (var.compareToIgnoreCase("pr") == 0) {
			return "mm";
		} else if (var.compareToIgnoreCase("ps") == 0
				|| var.compareToIgnoreCase("psl") == 0) {
			return "mbar";
		} else if (var.compareToIgnoreCase("zg") == 0) {
			return "m";
		} else if (var.compareToIgnoreCase("hur") == 0) {
			return "-";
		} else if (var.compareToIgnoreCase("hus") == 0) {
			return "kg kg-1";
		} else if (var.compareToIgnoreCase("ua") == 0
				|| var.compareToIgnoreCase("uas") == 0
				|| var.compareToIgnoreCase("va") == 0
				|| var.compareToIgnoreCase("vas") == 0) {
			return "m s-1";
		}

		Log.getInstance().warn(
				String.format("Standardized unit not mapped for %s"));

		return null;
	}
}
