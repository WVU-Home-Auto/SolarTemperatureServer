package edu.wvu.solar.solartemperatureserver;

import java.util.LinkedList;
import org.joda.time.DateTime;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {
	
	@RequestMapping("/test")
	public DateTime servletTest(@RequestParam(value="magic") int magic){
		return new DateTime();
	}

	@RequestMapping(value = "/getdata", method = RequestMethod.GET)
	public LinkedList<LogEntry> getData(@RequestParam(value="startTime") String startTime, 
			@RequestParam(value="endTime") String endTime, 
			@RequestParam(value="sensorName") String sensorName){
		DateTime start = DateTime.parse(startTime, LogEntry.TIME_FORMAT);
		DateTime end = DateTime.parse(endTime, LogEntry.TIME_FORMAT);
		return TemperatureLog.getLog(sensorName).getData(start, end);
		//return TemperatureLog.getLog(sensorName).getData(startTime, endTime);
	}
	
	@RequestMapping(value = "/getsensornames", method = RequestMethod.GET)
	public String[] getSensorNames(){
		return TemperatureLog.getSensorNames();
	}
	
	
}
