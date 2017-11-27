package org.gcta.sdw.logic.test;

public class Test {

	public Test() {
	}

	public static void main(String[] args) throws Exception {
		Test a = new Test();
		a.test();
	}

	public void test() {
		try {
			// JPManagerFactory.createEntityManagerFactory();

			// List<GeneralEntity> stations = GeneralDao
			// .queryAll(User.class.getName());
			//
			// for (GeneralEntity st : stations) {
			// User user = (User) st;
			// Log.getInstance().info(user.getName());
			// }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// JpManager.close();
			// JPManagerFactory.close();
		}
	}

}