package com.example.monitorarenda;

import java.util.Locale;

/** Motor de custeio. Só matemática, sem tela. */
public class calculo {
    static final Locale BR = Locale.forLanguageTag("pt-BR");
    public static final String[] TURNOS = {"Manhã", "Almoço", "Tarde", "Noite"};
    public static final String[] DIAS = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};

    static double div(double a, double b) { return b <= 0 ? 0 : a / b; }

    /** Custo por km = combustível + manutenção + pneus + kit relação. */
    public static double custoKm(double kml, double litro, double mV, double mK,
                                 double pV, double pK, double kV, double kK) {
        return div(litro, kml) + div(mV, mK) + div(pV, pK) + div(kV, kK);
    }

    /** Custo por hora = custos fixos do mês ÷ (horas por semana × 4,33). */
    public static double custoHora(double fixosMes, double horasSemana) {
        return div(fixosMes, horasSemana * 4.33);
    }

    /** Custo de uma corrida = km × custo/km + minutos × custo/hora. */
    public static double custoCorrida(double km, double min, double cKm, double cHora) {
        return km * cKm + (min / 60.0) * cHora;
    }

    public static class Resumo {
        public double horas, bruto, minCorrida, km, custoKm, custoHora, custos, liquido,
                liquidoHora, esperaH, esperaPct, brutoAparenteHora;
        public int corridas;
        public boolean kmDoHodometro;
    }

    /** Conta de um turno. A espera (tempo no app - tempo em corrida) entra pelo custo/hora. */
    public static Resumo resumo(double horas, double bruto, double minCorrida, double km,
                                double cKm, double cHora) {
        Resumo r = new Resumo();
        r.horas = horas; r.bruto = bruto; r.minCorrida = minCorrida; r.km = km;
        r.custoKm = km * cKm;
        r.custoHora = horas * cHora;
        r.custos = r.custoKm + r.custoHora;
        r.liquido = bruto - r.custos;
        r.liquidoHora = horas > 0 ? r.liquido / horas : 0;
        r.esperaH = Math.max(0, horas - minCorrida / 60.0);
        r.esperaPct = horas > 0 ? 100.0 * r.esperaH / horas : 0;
        r.brutoAparenteHora = minCorrida > 0 ? bruto / (minCorrida / 60.0) : 0;
        return r;
    }

    public static int turno(int hora) {
        if (hora >= 5 && hora < 11) return 0;
        if (hora >= 11 && hora < 15) return 1;
        if (hora >= 15 && hora < 19) return 2;
        return 3;
    }

    /** Calendar.DAY_OF_WEEK (domingo = 1) vira índice: segunda = 0 ... domingo = 6. */
    public static int diaIdx(int calDow) { return (calDow + 5) % 7; }

    public static String money(double v) {
        String sinal = (v < 0 && Math.abs(v) >= 0.005) ? "−" : "";
        return sinal + String.format(BR, "R$ %,.2f", Math.abs(v));
    }

    public static String n2(double v) { return String.format(BR, "%.2f", v); }

    public static String hm(double horas) {
        long m = Math.round(horas * 60);
        return String.format(BR, "%dh%02d", m / 60, m % 60);
    }

    public static double num(String s, double def) {
        if (s == null) return def;
        s = s.trim().replace(",", ".");
        if (s.isEmpty()) return def;
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }

    public static String plain(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
