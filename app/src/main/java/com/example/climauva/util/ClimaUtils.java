package com.example.climauva.util;

import com.example.climauva.model.Current;

public class ClimaUtils {
    public static String formatarMensagemNotificacao(Current dadosOffline) {
        if (dadosOffline == null) {
            return "Abra o app para atualizar o clima.";
        }
        return "Última temperatura salva: " + dadosOffline.getTemperature_2m() + "°C";
    }
}
