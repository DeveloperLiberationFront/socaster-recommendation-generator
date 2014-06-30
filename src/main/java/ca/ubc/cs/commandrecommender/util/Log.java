package ca.ubc.cs.commandrecommender.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

//TODO: Doesn't look like this is useful. We can delete this code if the new logging system
//      does not use this
public class Log {

	private static BufferedWriter log;
	
	private static long timestamp;
	
	static {
		timestamp = System.currentTimeMillis();
	}

	public static void threading(String msg) {
		// print("[log-thrd] "+msg);
	}

	public static void correctness(String msg) {
		print("[log-crct]," + msg, true);
	}

	public static void performance(String msg) {
		print("[log-perf] " + msg);
	}

	public static void progress(String msg) {
		print("[log-prgs] " + msg);
	}

	private synchronized static void print(String msg) {
		print(msg, false);
	}

	private synchronized static void print(String msg, boolean doLog) {
		System.out.println(msg);
		if (doLog) {
			try {
				FileWriter fstream = new FileWriter("log"+timestamp +".csv", true);
				log = new BufferedWriter(fstream);
				log.write(msg);
				log.newLine();
				log.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void openLog(String name) {
		if (name == null)
			name = "log.txt";
		try {
			FileWriter fstream = new FileWriter("log.txt", true);
			log = new BufferedWriter(fstream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void closeLog() {
		try {
			if (log != null)
				log.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
