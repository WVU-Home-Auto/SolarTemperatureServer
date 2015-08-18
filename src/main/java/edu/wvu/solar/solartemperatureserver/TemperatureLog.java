package edu.wvu.solar.solartemperatureserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class TemperatureLog {
	
	private static final Logger logger = LogManager.getLogger(TemperatureLog.class);
	public static final String PROPERTIES_FILE = "config.properties";
	private static String logFileRoot;
	private static String[] sensorNames;
	
	private static HashMap<String, TemperatureLog> instances;

	/*
	 * Initializes the instances of TemperatureLog
	 */
	static{
		Properties prop = new Properties();
		InputStream is = TemperatureLog.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
		try {
			prop.load(is);
			logFileRoot = prop.getProperty("logfileroot");
			sensorNames = prop.getProperty("sensornames").split(",");
		} catch (IOException e) {
			logger.error("Error loading properties file " + PROPERTIES_FILE + ": ", e);
			logger.error("Using default values.");
			logFileRoot = "";
			sensorNames = new String[]{"sensor1", "sensor2", "sensor3"};
		}
		
		if(!logFileRoot.endsWith("/") && logFileRoot.length() > 0){
			logFileRoot = logFileRoot + "/";
		}
		
		instances = new HashMap<String, TemperatureLog>();
		for(String s : sensorNames){
			instances.put(s, new TemperatureLog(s));
		}
	}
	
	/**
	 * Use this method to get an instance of a single temperature log.
	 * The names of the sensors and their corresponding logs can be found
	 * by calling TemperatureLog.getSensorNames().
	 * 
	 * @param name Name of the log, as given my getSensorNames()
	 * @return Instance of TemperatureLog corresponsing to that name.
	 */
	public static TemperatureLog getLog(String name){
		return instances.get(name);
	}
	
	/**
	 * Use this method to find what the names of the sensors are.
	 * 
	 * @return Array of the names of the sensors.
	 */
	public static String[] getSensorNames(){
		return instances.keySet().toArray(new String[instances.size()]);
	}
	

	private String sensorName;
	private File logFile;
	
	/*
	 * Private constructor. You should not be instantiating this class.
	 * Use the static method getLog(String)
	 */
	private TemperatureLog(String sensorName){
		this.sensorName = sensorName;
		String nameNoSpaces = sensorName.replace(' ', '_');
		String logFilePath = logFileRoot + nameNoSpaces + ".log";
		logFile = new File(logFilePath);
		if(!logFile.exists()){
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				logger.error("Exception creating new logFile " + logFilePath, e);
			}
		}
	}
	
	/**
	 * Appends an entry to the log file.
	 * 
	 * @param temp Temperature to be logged.
	 * @param humidity Humidity to be logged
	 */
	public synchronized void log(TempHumidity temp){
		String now = new DateTime().toString();
		String logEntry = now + "|" + Double.toString(temp.getTemp()) + "|" + Double.toString(temp.getHumidity()) + "\n";
		try {
			Files.write(Paths.get(logFile.getAbsolutePath()), logEntry.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("Error appending log entry to file " + logFile.getAbsolutePath(), e);
		}
	}
	
	
	
	public String getSensorName(){
		return sensorName;
	}
	
	public static void main(String[] args){
		logger.debug("Log file root: " + logFileRoot);
		TemperatureLog log = getLog("indoor1");
		log.log(new TempHumidity(10f, 1f));
		log.log(new TempHumidity(11f, 99999999f));
	}
}
