package com.example.climauva.model;
import com.example.climauva.model.Current;
public class OpenMeteoResponse {

    //Response comum da API
    //    {
//            "latitude": -22.952549,
//            "longitude": -43.215027,
//            "generationtime_ms": 0.237345695495605,
//            "utc_offset_seconds": 0,
//            "timezone": "GMT",
//            "timezone_abbreviation": "GMT",
//            "elevation": 12,
//            "current_units": {
//        "time": "iso8601",
//                "interval": "seconds",
//                "temperature_2m": "°C",
//                "precipitation": "mm",
//                "rain": "mm"
//    },

    private Double latitude;
    private Double longitude;
    private Double generationtime_ms;
    private Double utc_offset_seconds;
    private String timezone;
    private String timezone_abbreviation;
    private Double elevation;
    private Current current;
    public OpenMeteoResponse(){}

    //Getters e Setters
/************************************************************
*    Latitude                                               *
*************************************************************/
    public double getLatitude(){ return this.latitude; }
    public void setLatitude(double longitude){this.longitude = longitude;}
/************************************************************
 *    Longitude                                             *
 *************************************************************/
public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    /************************************************************
     *    Timezone                                               *
     *************************************************************/
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    /************************************************************
     *    Current Weather                                       *
     *************************************************************/
    public Current getCurrent() { return current; }
    public void setCurrent(Current current) { this.current = current; }

}