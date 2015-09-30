package edu.wvu.solar.solartemperatureserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

@Component
@PropertySource("classpath:config.properties")
public class ScheduledTask {
	
	private static final Logger logger = LogManager.getLogger(ScheduledTask.class);
	public static final String PROPERTIES_FILE = "config.properties";
	private static Properties properties;
	public static final Firebase firebase = new Firebase("https://developer-api.nest.com");
	
	private static double setTemp;
	private static DataSnapshot data = null;
	private static final Object sync = new Object();
	static {
		InputStream is = TemperatureLog.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
		properties = new Properties();
		try {
			properties.load(is);
			setTemp = Double.parseDouble(properties.getProperty("setTemp"));
		} catch (IOException e) {
			logger.error(e);
			setTemp = 74.0;
		}
		
		String token = properties.getProperty("nestToken");
		firebase.authWithCustomToken(token, new Firebase.AuthResultHandler() {
			
			public void onAuthenticationError(FirebaseError error) {
				logger.error(error.getMessage());
			}
			
			public void onAuthenticated(AuthData auth) {
				firebase.addValueEventListener(new ValueEventListener() {

					public void onCancelled(FirebaseError error) {
						logger.error(error.getMessage());
					}

					public void onDataChange(DataSnapshot newData) {
						synchronized(sync){
							data = newData;
						}
					}
					
				});
			}
		});
	}
	/**
	 * This method will run according to the cron set in the config file.
	 * (It will probably run every minute or every 5 minutes or something.)
	 * TODO: Add code to:
	 *   -Read temperature from each sensor and log it
	 *   -Compare current temperature with set temperature and outside temperature
	 *   -Determine whether to turn the Nest thermostat on or off
	 */
	//@Scheduled(cron = "${tempCheckCron}")
	public void update(){
		String[] sensors = TemperatureLog.getSensorNames();
		for(String s : sensors){
			if(!TemperatureLog.getUrl(s).equals("nest")){
				Unirest.get(TemperatureLog.getUrl(s)).asStringAsync(new DataCallback(s));
			}else if(data != null){
				double temperature, humidity;
				synchronized(sync){
					DataSnapshot thermostat = data.child("devices").child("thermostats").getChildren().iterator().next();
					temperature = Double.parseDouble(thermostat.child("ambient_temperature_f").getValue().toString());
					humidity = Double.parseDouble(thermostat.child("humidity").getValue().toString());
				}
				TemperatureLog.getLog(s).log(new LogEntry(temperature, humidity));
			}
		}
	}
	
	public static class DataCallback implements Callback<String>{

		private String sensorName;
		
		public DataCallback(String sensorName){
			this.sensorName = sensorName;
		}
		
		public void cancelled() {
		}

		public void completed(HttpResponse<String> response) {
			try {
				String[] data = response.getBody().split(",");
				double temp = Double.parseDouble(data[0]);
				double humidity = Double.parseDouble(data[1]);
				temp = (temp * 1.8) + 32;
				TemperatureLog.getLog(sensorName).log(new LogEntry(temp, humidity));
			} catch (NumberFormatException e) {
				logger.error("Number format exception: Could not parse " + response.getBody());
			} catch (ArrayIndexOutOfBoundsException e){
				logger.error("Data returned from temperature sensor was improperly formatted. Got " + response.getBody());
			}
			
		}

		public void failed(UnirestException e) {
			logger.error("Failed to retrieve data from sensor " + sensorName + ": ", e);
		}
		
	}
	
}
