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
	
	public LogEntry(double temp, double humidity) {
		super();
		this.temp = temp;
		this.humidity = humidity;
		time = new DateTime().toString(TIME_FORMAT);
	}
	
	public LogEntry(double temp, double humidity, String time){
		this.temp = temp;
		this.humidity = humidity;
		this.time = time;
	}
	
	public LogEntry(String string){
		String[] strings = string.split(DELIMITER);
		time = strings[0];
		temp = Double.parseDouble(strings[1]);
		humidity = Double.parseDouble(strings[2]);
	}

	public double getTemp() {
		return temp;
	}

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
