package com.example.climauva.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.climauva.BuildConfig;
import com.example.climauva.R;

public class NotificationHelper {
    public static final String CHANNEL_ID = "canal_padrao";

    public static void criarCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal de Notificações",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Canal para notificações gerais do app");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void enviarNotificacao(Context context, String titulo, String mensagem) {
        criarCanal(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (BuildConfig.DEBUG) {
                    Log.d("DebugApp", "Permissão POST_NOTIFICATIONS não concedida.");
                }
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        try {
            manager.notify(1, builder.build());
            if (BuildConfig.DEBUG) {
                Log.d("DebugApp", "Notificação enviada: " + titulo + " - " + mensagem);
            }
        } catch (SecurityException e) {
            if (BuildConfig.DEBUG) {
                Log.d("DebugApp", "Erro de segurança ao enviar notificação: " + e.getMessage());
            }
        }
    }
}
