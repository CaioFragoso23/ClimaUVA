package com.example.climauva.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CurrentWeather {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public double temperature;
    public double rain;
    public String date;
}
