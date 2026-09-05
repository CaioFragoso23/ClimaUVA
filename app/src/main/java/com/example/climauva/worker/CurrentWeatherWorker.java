package com.example.climauva.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.climauva.BuildConfig;
import com.example.climauva.database.AppDatabase;
import com.example.climauva.database.CurrentWeather;
import com.example.climauva.model.Current;
import com.example.climauva.model.OpenMeteoResponse;
import com.example.climauva.service.RetrofitClient;
import com.example.climauva.service.WeatherApiService;
import com.example.climauva.util.ClimaUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class CurrentWeatherWorker extends Worker {

    public CurrentWeatherWorker(@NonNull Context context,
                                @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (BuildConfig.DEBUG) {
            Log.d("DebugApp", "Executando CurrentWeatherWorker doWork...");
        }

        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);

        // Coordenadas passadas por argumento ou padrão (ex: Rio de Janeiro)
        double latitude = getInputData().getDouble("latitude", -22.9068);
        double longitude = getInputData().getDouble("longitude", -43.1729);

        WeatherApiService apiService = RetrofitClient
                .getRetrofitInstance()
                .create(WeatherApiService.class);

        Call<OpenMeteoResponse> call = apiService.getCurrentWeather(
                latitude,
                longitude,
                "temperature_2m,precipitation,rain");

        try {
            Response<OpenMeteoResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null && response.body().getCurrent() != null) {
                Current current = response.body().getCurrent();
                double temp = current.getTemperature_2m();
                double rain = current.getRain();

                String dataHoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                CurrentWeather cw = new CurrentWeather();
                cw.temperature = temp;
                cw.rain = rain;
                cw.date = dataHoje;
                db.currentWeatherDAO().inserir(cw);

                String titulo = "Atualização do Clima";
                if (rain > 0) {
                    titulo = "Alerta de Chuva!";
                }
                String mensagem = "Temperatura: " + temp + "°C | Chuva: " + rain + " mm";

                if (BuildConfig.DEBUG) {
                    Log.d("DebugApp", "Sucesso ao buscar clima via Worker. Enviando notificação...");
                }

                NotificationHelper.enviarNotificacao(context, titulo, mensagem);
                return Result.success();
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.d("DebugApp", "Erro na execução do Worker: " + e.getMessage());
            }
        }

        // Caso a chamada síncrona falhe (offline), resgata o último clima do banco local
        CurrentWeather ultimoClima = db.currentWeatherDAO().getUltimoClima();
        Current currentOffline = null;
        if (ultimoClima != null) {
            currentOffline = new Current();
            currentOffline.setTemperature_2m(ultimoClima.temperature);
            currentOffline.setRain(ultimoClima.rain);
        }

        String mensagemOffline = ClimaUtils.formatarMensagemNotificacao(currentOffline);
        if (BuildConfig.DEBUG) {
            Log.d("DebugApp", "Worker exibindo mensagem offline: " + mensagemOffline);
        }

        NotificationHelper.enviarNotificacao(context, "Clima UVA (Offline)", mensagemOffline);

        return Result.success();
    }
}
