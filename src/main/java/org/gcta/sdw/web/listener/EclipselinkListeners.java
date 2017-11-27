package org.gcta.sdw.web.listener;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppCleanup;
import org.zkoss.zk.ui.util.WebAppInit;

/**
 * Listener for initializing and closing persistence
 * 
 * @author Ronald
 * 
 */
public class EclipselinkListeners implements WebAppInit, WebAppCleanup {

	public void init(WebApp webapp) throws Exception {
		// Load persistence:
//		JPManagerFactory.createEntityManagerFactory();
		// JpManager.createEntityManager();
	}

	public void cleanup(WebApp webapp) throws Exception {
		// JpManager.close();
		// JPManagerFactory.close();
	}
}