package org.gcta.sdw.web.viewmodel.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GeneralUtil {

	public static <T> List<T> getOrderedSublist(List<T> list, HashSet<T> sublist) {
		if (sublist == null || sublist.isEmpty() || list == null
				|| list.isEmpty()) {
			return null;
		}

		List<T> ordered = new ArrayList<T>();

		for (T t : list) {
			if (sublist.contains(t)) {
				ordered.add(t);
			}
			if (sublist.size() == ordered.size()) {
				return ordered;
			}
		}

		return ordered;

	}

	// public static ArrayList<? extends Object> getOrderedSublist2(
	// List<? extends Object> list, HashSet<? extends Object> sublist) {
	//
	// if (sublist == null || sublist.isEmpty() || list == null
	// || list.isEmpty()) {
	// return null;
	// }
	//
	// ArrayList<Object> ordered = new ArrayList<Object>();
	//
	// for (Object obj : list) {
	// if (sublist.contains(obj)) {
	// ordered.add(obj);
	// }
	// if (sublist.size() == ordered.size()) {
	// return ordered;
	// }
	// }
	//
	// return ordered;
	// }
}
