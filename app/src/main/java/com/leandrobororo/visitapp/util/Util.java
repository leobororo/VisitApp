package com.leandrobororo.visitapp.util;

import android.support.annotation.NonNull;

import com.leandrobororo.visitapp.R;

import java.util.Locale;

/**
 * Created by leandrobororo on 04/03/17.
 */
public class Util {
    private static final String TEXTO_TIME_BOX = "Entre %02d:%02d e %02d:%02d horas";
    private static final String FORMATO_DATA = "%02d/%02d/%04d";
    private static final String HORA = "%02d:%02d:00";
    private static final String DATA = "%4d-%02d-%02d";

    public static boolean isTimeBoxValid(int startHour, int startMinute, int endHour, int endMinute)
    {
        boolean finalHourGreaterInitialHour = endHour > startHour;
        boolean initialHourEqualsFinalHourAndInitialMinuteLessFinalMinute = startHour == endHour && startMinute < endMinute;
        return isTimeParametersValid(startHour, startMinute, endHour, endMinute) && (finalHourGreaterInitialHour || initialHourEqualsFinalHourAndInitialMinuteLessFinalMinute);
    }

    public static String getHorarioMedioString(int startHour, int startMinute, int endHour, int endMinute) {
        int mediaHora = endHour + startHour + (endMinute + startMinute) / 60;
        int mediaMinutos = (endMinute + startMinute)%60;

        return String.format(HORA, mediaHora / 2, mediaMinutos / 2 + ((int)(((double)mediaHora%2) / 2 * 60)));
    }

    public static String formatDateToCalculate(String date) {
        return String.format(DATA,
                Integer.parseInt(date.substring(6, 10)),
                Integer.parseInt(date.substring(3, 5)),
                Integer.parseInt(date.substring(0, 2)));
    }

    public static String formatTimeBox(int startHour, int startMinute, int endHour, int endMinute) {
        return String.format(Locale.getDefault() ,TEXTO_TIME_BOX,
                startHour,
                startMinute,
                endHour,
                endMinute);
    }

    public static String gerarDataVisita(int year, int month, int dayOfMonth) {
        return String.format(FORMATO_DATA, dayOfMonth, month, year);
    }

    public static String addOneHourAndHalf(String horario) {
        int hora = Integer.parseInt(horario.substring(0, 2)) + 1 + (Integer.parseInt(horario.substring(3, 5)) + 30) / 60;
        int minuto = (Integer.parseInt(horario.substring(3, 5)) + 30) % 60;

        return String.format(HORA, hora, minuto);
    }

    public static String subtractOneHourAndHalf(String horario) {
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

    public static String extractDayMonthYearDateString(String diaVisita) {
        return diaVisita.substring(8, 10) + "/" + diaVisita.substring(5, 7) + "/" + diaVisita.substring(0, 4) + ")";
    }

    public static char getDayNightChar(String horarioMedioString) {
        int hora = Integer.parseInt(horarioMedioString.substring(0, 2));
        return hora > 6 && hora < 18 ? 'd' : 'n';
    }

    private static boolean isTimeParametersValid(int startHour, int startMinute, int endHour, int endMinute)
    {
        return isValidHour(startHour) && isValidHour(endHour) && isValidMinute(endMinute) && isValidMinute(startMinute);
    }

    private static boolean isValidHour(int hour)
    {
        return hour > -1 && hour < 23;
    }

    private static boolean isValidMinute(int minute)
    {
        return minute > -1 && minute < 59;
    }
}
