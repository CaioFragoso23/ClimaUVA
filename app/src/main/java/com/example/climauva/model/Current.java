package com.example.climauva.model;

public class Current {

    private String time;
    private double temperature_2m;
    private double precipitation;
    private double rain;

    //Getters e Setters
    /************************************************************
     *    time                                                  *
     *************************************************************/
        public String getTime(){ return time; }
        public void setTime(String time){ this.time = time;}

    /************************************************************
     *    Temperature                                             *
     *************************************************************/
    public double getTemperature_2m() {
        return temperature_2m;
    }
    public void setTemperature_2m(double temperature_2m){this.temperature_2m = temperature_2m;}
    /************************************************************
     *    Precipitation                                         *
     *************************************************************/
    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
    /************************************************************
     *    Rain                                                  *
     *************************************************************/
    public double getRain() {
        return rain;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }
    //      "current": {
//        "time": "2026-09-05T13:00",
//                "interval": 900,
//                "temperature_2m": 25.1,
//                "precipitation": 0,
//                "rain": 0
//    }
//

}
