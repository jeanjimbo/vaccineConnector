package com.jean.project;
/*a static wrapper class for an EntityManagerFactory instance*/
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public final class PMF {
	private static final PersistenceManagerFactory pmfInstance =
		JDOHelper.getPersistenceManagerFactory("transactions-optional");
		private PMF() {}
		public static PersistenceManagerFactory get() {
		return pmfInstance;
		}
}
