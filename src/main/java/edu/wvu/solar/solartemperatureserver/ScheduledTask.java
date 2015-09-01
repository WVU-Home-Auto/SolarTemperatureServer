package edu.wvu.solar.solartemperatureserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

@Component
@PropertySource("classpath:config.properties")
public class ScheduledTask {
	
	private static final Logger logger = LogManager.getLogger(ScheduledTask.class);

	/**
	 * This method will run according to the cron set in the config file.
	 * (It will probably run every minute or every 5 minutes or something.)
	 * TODO: Add code to:
	 *   -Read temperature from each sensor and log it
	 *   -Compare current temperature with set temperature and outside temperature
	 *   -Determine whether to turn the Nest thermostat on or off
	 */
	@Scheduled(cron = "${tempCheckCron}")
	public void update(){
		String[] sensors = TemperatureLog.getSensorNames();
		for(String s : sensors){
			Unirest.get(TemperatureLog.getUrl(s)).asStringAsync(new DataCallback(s));
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
