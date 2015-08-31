package edu.wvu.solar.solartemperatureserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
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
			try {
				HttpResponse<String> response = Unirest.get(TemperatureLog.getUrl(s)).asString();
				System.out.println(response.getBody());
			} catch (UnirestException e) {
				logger.error("Error retrieving temperature from " + s + ": " + e.getMessage());
			}
		}
	}
	
}
