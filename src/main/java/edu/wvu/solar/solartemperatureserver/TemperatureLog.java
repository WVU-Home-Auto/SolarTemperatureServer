package edu.wvu.solar.solartemperatureserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * This class represents a log of all recorded temperatures.
 * 
 * You should not instantiate this class with the 'new' keyword. Instead, use the static "getLog(String)" method
 * to retrieve the instance associated with the specified sensor.
 * The "getSensorNames" method returns an array of the names of all of the available logs.
 * You should pass one of these names to the "getLog" method.
 * 
 * To change the names of the sensors, go to src/main/resources/config.properties.
 * (You'll also want to change the directory the log files are stored in for testing
 * on your own computer. This is another option in the config file.)
 * @author Timothy Scott
 *
 */
public class TemperatureLog {

	private static final Logger logger = LogManager.getLogger(TemperatureLog.class);
	public static final String PROPERTIES_FILE = "config.properties";

	private static String logFileRoot;
	private static String[] sensorNames;

	private static HashMap<String, TemperatureLog> instances;
	private static HashMap<String, String> sensorUrls;

	/*
	 * Initializes the instances of TemperatureLog
	 */
	static{
		Properties prop = new Properties();
		InputStream is = TemperatureLog.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
		String[] urls;
		try {
			prop.load(is);
			logFileRoot = prop.getProperty("logfileroot");
			sensorNames = prop.getProperty("sensornames").split(",");
			urls = prop.getProperty("sensorUrls").split(",");
		} catch (IOException e) {
			logger.error("Error loading properties file " + PROPERTIES_FILE + ": ", e);
			logger.error("Using default values.");
			logFileRoot = "";
			sensorNames = new String[]{"sensor1", "sensor2", "sensor3"};
			urls = new String[]{"https://agent.electricimp.com/Zkb2-jaOX8lo",
					"https://agent.electricimp.com/LZw17jOvml3C",
					"https://agent.electricimp.com/d5u_M9an89Xf"};
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				logger.error("Error closing inputStream from reading properties file. ", e);
			}
		}

		if(!logFileRoot.endsWith("/") && logFileRoot.length() > 0){
			logFileRoot = logFileRoot + "/";
		}

		instances = new HashMap<String, TemperatureLog>();
		for(String s : sensorNames){
			instances.put(s, new TemperatureLog(s));
		}
		
		sensorUrls = new HashMap<String, String>();
		for(int i = 0; i < sensorNames.length; i++){
			sensorUrls.put(sensorNames[i], urls[i]);
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

	/**
	 * Gives the electric imp agent url for the sensor of the supplied name.
	 * 
	 * @param sensorName Name of the sensor. Must be one of the sensors given in getSensorNames()
	 * @return Url of the electric imp corresponding to that sensor name. (This is taken directly from the properties file)
	 */
	public static String getUrl(String sensorName){
		return sensorUrls.get(sensorName);
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
				logger.fatal("Exception creating new logFile " + logFilePath, e);
			}
		}
	}

	/**
	 * Appends an entry to the log file.
	 * 
	 * @param temp Temperature to be logged.
	 * @param humidity Humidity to be logged
	 */
	public synchronized void log(LogEntry temp){
		try {
			Files.write(Paths.get(logFile.getAbsolutePath()), (temp.toString() + "\n").getBytes(), StandardOpenOption.APPEND);
		} catch (NoSuchFileException e){
			logger.error("Error writing to temperature log file " + logFile.getPath() + ". Was the file deleted while this program was running?", e);
			try {
				logFile.createNewFile();
			} catch (IOException e1) {
				logger.error("Error creating new temperature log file " + logFile.getPath() + " to replace the one that was apparently just deleted.", e1);
			}
		} catch (IOException e) {
			logger.error("Error appending log entry to file " + logFile.getAbsolutePath(), e);
		}
	}

	public LinkedList<LogEntry> getData(DateTime startTime, DateTime endTime){
		LinkedList<LogEntry> out = new LinkedList<LogEntry>();
		Scanner scanner;
		try {
			scanner = new Scanner(logFile);
		} catch (FileNotFoundException e) {
			logger.error("File not found while reading from log: " + logFile.getAbsolutePath(), e);
			return out;
		}
		LogEntry logIn;
		DateTime dateIn;
		do{
			logIn = new LogEntry(scanner.nextLine());
			dateIn = logIn.getTimeAsDateTime();
		}while(dateIn.isBefore(startTime) && scanner.hasNext());

		do{
			out.add(logIn);
			logIn = new LogEntry(scanner.nextLine());
			dateIn = logIn.getTimeAsDateTime();
		}while(dateIn.isBefore(endTime) && scanner.hasNext());

		scanner.close();
		return out;
	}

	public String getSensorName(){
		return sensorName;
	}

	/*
	 * This is just for testing. The final program will not have a main method here.
	 * 
	 * (It might not have one at all, I need to look into that.)
	 */
	public static void main(String[] args){
		TemperatureLog log = getLog("indoor2");
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < 30; i++){
			log.log(new LogEntry(Math.random(), Math.random()));

			try {
				Thread.sleep(1701);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}

		}
		long end = System.currentTimeMillis();
		logger.info("Time taken: " + (end - start) + " millis.");
		
		/*MutableDateTime startMut = new MutableDateTime();
		startMut.setMonthOfYear(8);
		startMut.setDayOfMonth(18);
		startMut.setHourOfDay(16);
		startMut.setMinuteOfHour(14);
		startMut.setSecondOfMinute(6);
		DateTime start = startMut.toDateTime();
		startMut.addSeconds(60);
		DateTime end = startMut.toDateTime();
		
		logger.info(start);
		logger.info(end);
		LinkedList<LogEntry> results = log.getData(start, end);
		
		logger.debug(results.size());
		for(DateTime d : results.keySet()){
			logger.debug(results.get(d).toString());
		}*/
		
		/*DateTimeFormatter parser = new DateTimeFormatterBuilder().appendPattern(DATE_FORMAT).toFormatter();
		DateTime test = DateTime.parse("2015-08-18 16:14:09", parser);
		logger.info(test);*/
	}
}
