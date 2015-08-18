package edu.wvu.solar.solartemperatureserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

public class TemperatureLog {

	private static final Logger logger = LogManager.getLogger(TemperatureLog.class);
	public static final String PROPERTIES_FILE = "config.properties";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
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
		String now = new DateTime().toString(DATE_FORMAT);
		String logEntry = now + "|" + Double.toString(temp.getTemp()) + "|" + Double.toString(temp.getHumidity()) + "\n";
		try {
			Files.write(Paths.get(logFile.getAbsolutePath()), logEntry.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("Error appending log entry to file " + logFile.getAbsolutePath(), e);
		}
	}

	public LinkedHashMap<DateTime, TempHumidity> getData(DateTime startTime, DateTime endTime){
		LinkedHashMap<DateTime, TempHumidity> out = new LinkedHashMap<DateTime, TempHumidity>();
		Scanner scanner;
		DateTimeFormatter parser = new DateTimeFormatterBuilder().appendPattern(DATE_FORMAT).toFormatter();
		try {
			scanner = new Scanner(logFile);
		} catch (FileNotFoundException e) {
			logger.error("File not found while reading from log: " + logFile.getAbsolutePath(), e);
			return out;
		}
		String[] lineIn;
		DateTime dateIn;
		do{
			lineIn = scanner.nextLine().split("\\|");
			dateIn = DateTime.parse(lineIn[0], parser);
		}while(dateIn.isBefore(startTime) && scanner.hasNext());

		do{
			out.put(dateIn, new TempHumidity(Double.parseDouble(lineIn[1]), Double.parseDouble(lineIn[2])));
			lineIn = scanner.nextLine().split("\\|");
			dateIn = DateTime.parse(lineIn[0], parser);
		}while(dateIn.isBefore(endTime) && scanner.hasNext());

		scanner.close();
		return out;
	}

	public String getSensorName(){
		return sensorName;
	}

	public static void main(String[] args){
		TemperatureLog log = getLog("indoor2");
		
		/*long start = System.currentTimeMillis();
		for(int i = 0; i < 100; i++){
			log.log(new TempHumidity(10.0, 100.0));

			try {
				Thread.sleep(1001);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		long end = System.currentTimeMillis();
		logger.info("Time taken: " + (end - start) + " millis.");*/
		
		MutableDateTime startMut = new MutableDateTime();
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
		LinkedHashMap<DateTime, TempHumidity> results = log.getData(start, end);
		
		logger.debug(results.size());
		for(DateTime d : results.keySet()){
			logger.debug(results.get(d).toString());
		}
		
		/*DateTimeFormatter parser = new DateTimeFormatterBuilder().appendPattern(DATE_FORMAT).toFormatter();
		DateTime test = DateTime.parse("2015-08-18 16:14:09", parser);
		logger.info(test);*/
	}
}
