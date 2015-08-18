package edu.wvu.solar.solartemperatureserver;

/**
 * Represents both temperature and humidity
 * 
 * @author Timothy Scott
 *
 */
public class TempHumidity {
	
	private double temp;
	private double humidity;
	
	public TempHumidity(double temp, double humidity) {
		super();
		this.temp = temp;
		this.humidity = humidity;
	}

	public double getTemp() {
		return temp;
	}

	public double getHumidity() {
		return humidity;
	}

	@Override
	public String toString(){
		return "Temp: " + temp + ", Humid: " + humidity;
	}
}
