package com.example.climauva;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.climauva.database.AppDatabase;
import com.example.climauva.database.CurrentWeather;
import com.example.climauva.model.OpenMeteoResponse;
import com.example.climauva.service.RetrofitClient;
import com.example.climauva.service.WeatherApiService;
import com.example.climauva.worker.CurrentWeatherWorker;
import com.example.climauva.worker.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    /* Pegando instância do Retrofit (Singleton) */
    private WeatherApiService apiService = RetrofitClient
            .getRetrofitInstance()
            .create(WeatherApiService.class);

    /* Instância do Banco de dados do Clima */
    private AppDatabase db;

    /* Declarando variáveis que vão ser mostradas na activity_main.xml*/
    private TextView tvTemperatura;
    private TextView tvChuva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(getApplicationContext());

        tvTemperatura = findViewById(R.id.tvTemperatura);
        tvChuva       = findViewById(R.id.tvChuva);

        // Garante a criação do canal de notificações
        NotificationHelper.criarCanal(this);

        solicitarPermissoes();
        getAutorizacaoEClima();
        configurarWorkManager();
    }

    private void solicitarPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void configurarWorkManager() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest weatherWorkRequest =
                new PeriodicWorkRequest.Builder(CurrentWeatherWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CurrentWeatherWork",
                ExistingPeriodicWorkPolicy.KEEP,
                weatherWorkRequest
        );
    }

    private void getClimaAPI(double latitude, double longitude, String dataHoje) {
        /* Resgatando os dados de Clima via API*/
        Call<OpenMeteoResponse> call = apiService.getCurrentWeather(
                latitude,
                longitude,
                "temperature_2m,precipitation,rain");

        call.enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call,
                                   Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Sucesso! Acessando os dados mapeados pelos Getters
                    double temp = response.body().getCurrent().getTemperature_2m();
                    double rain = response.body().getCurrent().getRain();

                    if (BuildConfig.DEBUG) {
                        Log.d("DebugApp", "Temperatura atual: " + temp + "°C");
                        Log.d("DebugApp", "Chance de Chuva: " + rain + "°C");
                    }

                    runOnUiThread(() -> {
                        tvTemperatura.setText(temp + "ºC");
                        tvChuva.setText("Precipitação: " + rain + " mm");
                    });

                    CurrentWeather cw = new CurrentWeather();
                    cw.temperature = temp;
                    cw.rain = rain;
                    cw.date = dataHoje;
                    db.currentWeatherDAO().inserir(cw);

                    if (BuildConfig.DEBUG) {
                        Log.d("DebugApp", "Dados salvos no Banco de Dados para a data: " + dataHoje);
                    }
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                if (BuildConfig.DEBUG) {
                    Log.d("DebugApp", "Erro na chamada da API (Sem conexão/Modo Avião): " + t.getMessage());
                }

                // Resgatando os dados locais do Banco de Dados em modo Offline
                CurrentWeather ultimoClima = db.currentWeatherDAO().getUltimoClima();
                if (ultimoClima != null) {
                    if (BuildConfig.DEBUG) {
                        Log.d("DebugApp", "Exibindo dados em Modo Offline (último registro salvo):");
                        Log.d("DebugApp", "Data: " + ultimoClima.date);
                        Log.d("DebugApp", "Temperatura: " + ultimoClima.temperature + "°C");
                        Log.d("DebugApp", "Chance de Chuva: " + ultimoClima.rain + "°C");
                    }
                    runOnUiThread(() -> {
                        tvTemperatura.setText(ultimoClima.temperature + "ºC");
                        tvChuva.setText("Precipitação: " + ultimoClima.rain + " mm");
                    });
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d("DebugApp", "Nenhum dado salvo no banco local para exibir no modo offline.");
                    }
                    runOnUiThread(() -> tvTemperatura.setText("Sem internet e sem dados salvos."));
                }
            }
        });
    }

    public void getAutorizacaoEClima() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Formata a data atual (ex: "2026-09-05")
                        String dataHoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        // Dispara um OneTimeWorkRequest passando a localização atual obtida
                        Data inputData = new Data.Builder()
                                .putDouble("latitude", latitude)
                                .putDouble("longitude", longitude)
                                .build();

                        OneTimeWorkRequest immediateWork = new OneTimeWorkRequest.Builder(CurrentWeatherWorker.class)
                                .setInputData(inputData)
                                .build();

                        WorkManager.getInstance(this).enqueue(immediateWork);

                        // Verifica no banco de dados se já existe clima registrado para hoje
                        CurrentWeather climaSalvo = db.currentWeatherDAO().getClimaPorData(dataHoje);
                        if (climaSalvo != null) {
                            if (BuildConfig.DEBUG) {
                                Log.d("DebugApp", "Dado do dia " + dataHoje + " encontrado no Banco de Dados:");
                                Log.d("DebugApp", "Temperatura: " + climaSalvo.temperature + "°C");
                                Log.d("DebugApp", "Chance de Chuva: " + climaSalvo.rain + "°C");
                            }
                            runOnUiThread(() -> {
                                tvTemperatura.setText(climaSalvo.temperature + "ºC");
                                tvChuva.setText("Precipitação: " + climaSalvo.rain + " mm");
                            });
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.d("DebugApp", "Nenhum dado para o dia " + dataHoje + ". Buscando da API...");
                            }
                            getClimaAPI(latitude, longitude, dataHoje);
                        }
                    }
                });
    }
}
