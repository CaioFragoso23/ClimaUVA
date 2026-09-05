package com.example.climauva;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.example.climauva.model.Current;
import com.example.climauva.util.ClimaUtils;

public class ClimaUtilsTest {

    @Test
    public void testeMensagemComDadosSalvos() {
        // Simula os dados vindos do banco offline
        Current climaMock = new Current();
        climaMock.setTemperature_2m(25.5); // Use o nome exato do seu setter

        String resultado = ClimaUtils.formatarMensagemNotificacao(climaMock);

        // Verifica se o texto gerado é exatamente o esperado
        assertEquals("Última temperatura salva: 25.5°C", resultado);
    }

    @Test
    public void testeMensagemSemDadosSalvos() {
        // Simula o cenário onde o banco de dados está vazio (null)
        String resultado = ClimaUtils.formatarMensagemNotificacao(null);

        assertEquals("Abra o app para atualizar o clima.", resultado);
    }
}