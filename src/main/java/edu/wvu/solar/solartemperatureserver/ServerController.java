package edu.wvu.solar.solartemperatureserver;

import org.joda.time.DateTime;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {
	
	@RequestMapping("/test")
	public DateTime servletTest(@RequestParam(value="magic") int magic){
		return new DateTime();
	}

}
