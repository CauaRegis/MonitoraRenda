package com.example.monitorarenda;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Veículo e custos da pessoa, guardados no celular.
 * Enquanto a tela "Veículo e custos" não existir, valem os valores de exemplo do Figma.
 */
public class config {
    private final SharedPreferences sp;

    public config(Context c) {
        sp = c.getSharedPreferences("cfg", Context.MODE_PRIVATE);
    }

    public double get(String k, double def) {
        return Double.longBitsToDouble(sp.getLong(k, Double.doubleToRawLongBits(def)));
    }

    public void put(String k, double v) {
        sp.edit().putLong(k, Double.doubleToRawLongBits(v)).apply();
    }

    public String nome() { return sp.getString("nome", "João Vitor"); }

    public void setNome(String n) { sp.edit().putString("nome", n).apply(); }

    public double custoKm() {
        return calculo.custoKm(get("kml", 40), get("litro", 6), get("mV", 300), get("mK", 3000),
                get("pV", 480), get("pK", 12000), get("kV", 300), get("kK", 15000));
    }

    public double fixosMes() {
        return get("seg", 120) + get("ipva", 30) + get("dep", 150) + get("cel", 60) + get("mei", 80);
    }

    public double horasSemana() { return get("hs", 38); }

    public double custoHora() { return calculo.custoHora(fixosMes(), horasSemana()); }
}