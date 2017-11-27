package org.gcta.sdw.logic.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.gcta.sdw.persistence.entity.Dataset;
import org.gcta.sdw.persistence.entity.Datavariable;
import org.gcta.sdw.persistence.entity.DatavariablePK;
import org.gcta.sdw.persistence.entity.Timeserie;
import org.gcta.sdw.persistence.entity.TimeseriePK;
import org.gcta.sdw.util.Log;

public class TestPersistence {

	public static void simpleTest() {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("sdw");

		EntityManager em = emf.createEntityManager();

		List<Dataset> list = em.createNamedQuery("Dataset.findAll",
				Dataset.class).getResultList();

		for (Dataset dataset : list) {
			Log.getInstance().debug(dataset);
		}

		em.close();
		emf.close();

		Log.getInstance().debug("Test completed!");

	}

	public static void testInsertWithReferences() {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("sdw");

		EntityManager em = emf.createEntityManager();

		DatavariablePK pk = new DatavariablePK(3, 1, 1);
		Datavariable var = new Datavariable(pk);

		TimeseriePK tsPk = new TimeseriePK(var.getDatavariablePK()
				.getDatasetId(), var.getDatavariablePK().getDataId(), var
				.getDatavariablePK().getVariableId(), new Date());
		Timeserie ts = new Timeserie(tsPk);
		ts.setValue(10.0);

		List<Timeserie> listTs = new ArrayList<>();
		listTs.add(ts);
		var.setTimeserieList(listTs);

		em.getTransaction().begin();
		em.persist(var);
		em.getTransaction().commit();

		em.close();
		emf.close();

		Log.getInstance().debug("Test completed!");

	}

	public static void main(String[] args) {
		testInsertWithReferences();
	}
}
