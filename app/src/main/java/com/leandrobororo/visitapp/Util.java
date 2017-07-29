package com.leandrobororo.visitapp;

import android.support.annotation.NonNull;

/**
 * Created by leandrobororo on 04/03/17.
 */

public class Util {
    public static final String DESEJA_ACOMPANHAR_PLANOS_DE_VISITA_DE_AMIGOS_AO_MESMO_LOCAL = "Deseja acompanhar planos de visita de amigos ao mesmo local?";
    public static final String OCORREU_UM_ERRO_AO_ACESSAR_O_SERVICO = "Ocorreu um erro ao acessar o serviço";
    public static final String NAO_FOI_POSSIVEL_SALVAR_A_VISITA = "Não foi possivel salvar a visita";
    public static final String REGISTRO_CANCELADO = "Registro cancelado!";
    public static final String FORMATO_DATA = "%02d/%02d/%04d";
    public static final String HORA = "%02d:%02d:00";
    public static final String CANCELAR = "Cancelar";
    public static final String SIM = "Sim";
    public static final String NAO = "Não";

    public static String gerarDataVisita(int year, int month, int dayOfMonth) {
        return String.format(FORMATO_DATA, dayOfMonth, month, year);
    }

    public static String somaUmaHoraMeia(String horario) {
        int hora = Integer.parseInt(horario.substring(0, 2)) + 1 + (Integer.parseInt(horario.substring(3, 5)) + 30) / 60;
        int minuto = (Integer.parseInt(horario.substring(3, 5)) + 30) % 60;

        return String.format(HORA, hora, minuto);
    }

    public static String subtraiUmaHoraMeia(String horario) {
        int hora = Integer.parseInt(horario.substring(0, 2)) + 1 + (Integer.parseInt(horario.substring(3, 5)) + 30) / 60;
        int minuto = (Integer.parseInt(horario.substring(3, 5)) + 30) % 60;

        if (hora - 3 < 0) {
            hora = 0;
            minuto = 0;
        } else {
            hora = hora - 3;
        }

        return String.format(HORA, hora, minuto);
    }

    @NonNull
    public static String extrairDiaMesAnoStringDataVisita(String diaVisita) {
        return diaVisita.substring(8, 10) + "/" + diaVisita.substring(5, 7) + "/" + diaVisita.substring(0, 4) + ")";
    }

    public static char getDiaOuNoite(String horarioMedioString) {
        int hora = Integer.parseInt(horarioMedioString.substring(0, 2));
        return hora > 6 && hora < 18 ? 'd' : 'n';
    }
}
