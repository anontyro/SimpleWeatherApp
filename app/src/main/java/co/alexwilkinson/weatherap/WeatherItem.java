package co.alexwilkinson.weatherap;

/**
 * Created by Alex on 25/10/2016.
 */

public class WeatherItem {
    public String title;
    public String temp;
    public String weatherCond;

    public WeatherItem(String title, String temp, String weatherCond){
        this.title = title;
        this.temp = temp;
        this.weatherCond = weatherCond;

    }
}
