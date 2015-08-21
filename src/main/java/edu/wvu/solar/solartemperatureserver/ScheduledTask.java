package edu.wvu.solar.solartemperatureserver;

import org.joda.time.DateTime;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:config.properties")
public class ScheduledTask {
	
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
		System.out.println(new DateTime().toString());
	}
	
}
