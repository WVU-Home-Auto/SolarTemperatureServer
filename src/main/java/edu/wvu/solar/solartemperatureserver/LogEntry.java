package edu.wvu.solar.solartemperatureserver;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents both temperature and humidity
 * 
 * @author Timothy Scott
 *
 */
public class LogEntry {
	
	//public static final String DATE_FORMAT = IsoDateTimeFormat;
	public static final DateTimeFormatter TIME_FORMAT = ISODateTimeFormat.basicDateTimeNoMillis();
	public static final String DELIMITER = " === ";
	
	private double temp;
	private double humidity;
	private String time;
	
	/**
	 * 
	 * @param temp Temperature IN FAHRENHEIT
	 * @param humidity Humidity in percentage (0.0 to 100.0)
	 */
	public LogEntry(double temp, double humidity) {
		super();
		this.temp = temp;
		this.humidity = humidity;
		time = new DateTime().toString(TIME_FORMAT);
	}
	
	/**
	 * 
	 * @param temp Temperature IN FAHRENHEIT
	 * @param humidity Humidity in percentage (0.0 to 100.0)
	 * @param time Time formatted according to LogEntry.TIME_FORMAT
	 */
	public LogEntry(double temp, double humidity, String time){
		this.temp = temp;
		this.humidity = humidity;
		this.time = time;
	}
	
	/**
	 * Constructs a LogEntry object from a String formatted according to LogEntry.toString()
	 * @param string String as stored in Log file, or returned by toString() method of this class. (Which should both be the same thing)
	 */
	public LogEntry(String string){
		String[] strings = string.split(DELIMITER);
		time = strings[0];
		temp = Double.parseDouble(strings[1]);
		humidity = Double.parseDouble(strings[2]);
	}

	/**
	 * @return Temperature in Fahrenheit
	 */
	public double getTemp() {
		return temp;
	}

	/**
	 * @return Relative humidity in percentage (0.0 to 100.0)
	 */
	public double getHumidity() {
		return humidity;
	}
	
	@JsonIgnore
	public DateTime getTimeAsDateTime(){
		return DateTime.parse(time, TIME_FORMAT);
	}
	
	public String getTime(){
		return time;
	}

	@Override
	public String toString(){
		return time + DELIMITER + Double.toString(temp) + DELIMITER + Double.toString(humidity);
	}
}
