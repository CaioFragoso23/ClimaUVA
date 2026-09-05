package com.example.climauva.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CurrentWeatherDAO {
    @Insert
    void inserir(CurrentWeather currentWeather);

    @Query("SELECT * FROM CurrentWeather")
    List<CurrentWeather> getAll();

    @Query("SELECT * FROM CurrentWeather WHERE date = :date LIMIT 1")
    CurrentWeather getClimaPorData(String date);

    @Query("SELECT * FROM CurrentWeather ORDER BY id DESC LIMIT 1")
    CurrentWeather getUltimoClima();
}
