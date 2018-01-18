public class Main {

	/**
	 * main method: will be called by starting the application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MasterController master;
		System.out.println("Test started");

		master = new MasterController();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				master.handleExit();
				System.out.println("Test stopped");
			}
		}, "Shutdown-thread"));
	}
}
