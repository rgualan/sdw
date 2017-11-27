package org.gcta.sdw.logic.services.impl;

import java.util.List;

import org.gcta.sdw.logic.services.PersistenceService;
import org.gcta.sdw.persistence.dao.GeneralDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service("persistenceService")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PersistenceServiceImpl implements PersistenceService {

	@Autowired
	GeneralDao generalDao;

	/* Services */
	public void insert(Object entity) throws Exception {
		generalDao.insert(entity);
	}

	public void update(Object entity) throws Exception {
		generalDao.update(entity);
	}

	public void delete(Object entity) throws Exception {
		generalDao.delete(entity);
	}

	public <T> List<T> queryAll(Class<T> entityClass) {
		return generalDao.queryAll(entityClass);
	}

	@Override
	public void insertBatch(List<? extends Object> objects) throws Exception {
		generalDao.insertBatch(objects);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object pk) {
		return generalDao.find(entityClass, pk);
	}

}
