package com.example.monitorarenda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/** Banco local (SQLite). Turnos e corridas. */
public class Db extends SQLiteOpenHelper {

    public static class Shift {
        public long id, start, end;       // end = 0 -> turno aberto
        public double odoStart, odoEnd;   // -1 -> não informado
        public boolean demo;
    }

    public static class Ride {
        public long id, shiftId, ts;
        public double value, km, min;
    }

    public Db(Context c) { super(c, "monitora.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE shifts(id INTEGER PRIMARY KEY AUTOINCREMENT, t_start INTEGER, "
                + "t_end INTEGER, odo_start REAL, odo_end REAL, demo INTEGER)");
        db.execSQL("CREATE TABLE rides(id INTEGER PRIMARY KEY AUTOINCREMENT, shift_id INTEGER, "
                + "ts INTEGER, amount REAL, km REAL, mins REAL, demo INTEGER)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) { }

    private static final String SC = "SELECT id,t_start,t_end,odo_start,odo_end,demo FROM shifts ";

    private Shift toShift(Cursor c) {
        Shift s = new Shift();
        s.id = c.getLong(0); s.start = c.getLong(1); s.end = c.getLong(2);
        s.odoStart = c.getDouble(3); s.odoEnd = c.getDouble(4); s.demo = c.getInt(5) == 1;
        return s;
    }

    private Shift one(String sql) {
        Cursor c = getReadableDatabase().rawQuery(sql, null);
        try { return c.moveToFirst() ? toShift(c) : null; } finally { c.close(); }
    }

    public Shift openShift() { return one(SC + "WHERE t_end=0 ORDER BY t_start DESC LIMIT 1"); }
    public Shift lastShift() { return one(SC + "WHERE t_end>0 ORDER BY t_start DESC LIMIT 1"); }
    public Shift getShift(long id) { return one(SC + "WHERE id=" + id); }

    public long startShift(double odo) {
        ContentValues v = new ContentValues();
        v.put("t_start", System.currentTimeMillis()); v.put("t_end", 0);
        v.put("odo_start", odo); v.put("odo_end", -1); v.put("demo", 0);
        return getWritableDatabase().insert("shifts", null, v);
    }

    public void endShift(long id, double odoEnd) {
        ContentValues v = new ContentValues();
        v.put("t_end", System.currentTimeMillis()); v.put("odo_end", odoEnd);
        getWritableDatabase().update("shifts", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void addRide(long shiftId, double value, double km, double min) {
        ContentValues v = new ContentValues();
        v.put("shift_id", shiftId); v.put("ts", System.currentTimeMillis());
        v.put("amount", value); v.put("km", km); v.put("mins", min); v.put("demo", 0);
        getWritableDatabase().insert("rides", null, v);
    }

    public List<Ride> rides(long shiftId) {
        List<Ride> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,shift_id,ts,amount,km,mins FROM rides WHERE shift_id=? ORDER BY ts",
                new String[]{String.valueOf(shiftId)});
        try {
            while (c.moveToNext()) {
                Ride r = new Ride();
                r.id = c.getLong(0); r.shiftId = c.getLong(1); r.ts = c.getLong(2);
                r.value = c.getDouble(3); r.km = c.getDouble(4); r.min = c.getDouble(5);
                l.add(r);
            }
        } finally { c.close(); }
        return l;
    }

    public List<Shift> closedShiftsSince(long ts) {
        List<Shift> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                SC + "WHERE t_end>0 AND t_start>=? ORDER BY t_start", new String[]{String.valueOf(ts)});
        try { while (c.moveToNext()) l.add(toShift(c)); } finally { c.close(); }
        return l;
    }

    /** Conta do turno (a espera entra: horas no app × custo/hora). */
    public calculo.Resumo resumo(Shift s, config cfg) {
        List<Ride> rs = rides(s.id);
        double bruto = 0, min = 0, kmR = 0;
        for (Ride r : rs) { bruto += r.value; min += r.min; kmR += r.km; }
        boolean odo = s.end > 0 && s.odoStart >= 0 && s.odoEnd >= s.odoStart;
        double km = odo ? s.odoEnd - s.odoStart : kmR;
        long fim = s.end > 0 ? s.end : System.currentTimeMillis();
        double horas = (fim - s.start) / 3600000.0;
        calculo.Resumo r = calculo.resumo(horas, bruto, min, km, cfg.custoKm(), cfg.custoHora());
        r.corridas = rs.size();
        r.kmDoHodometro = odo;
        return r;
    }

    /** Barras do gráfico da Home: ganho líquido por hora de relógio do turno (até 12 horas). */
    public static class Barras {
        public int inicio;       // hora do relógio da primeira barra
        public double[] valores; // R$/h líquido de cada hora (NaN = pouco tempo para medir)
    }

    public Barras barras(Shift s, config cfg) {
        calculo.Resumo r = resumo(s, cfg);
        List<Ride> rs = rides(s.id);
        long fim = s.end > 0 ? s.end : System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(s.start);
        c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
        long piso = c.getTimeInMillis();
        List<Double> lista = new ArrayList<>();
        int primeiraHora = c.get(Calendar.HOUR_OF_DAY);
        for (long t = piso; t < fim; t += 3600000L) {
            long a = Math.max(s.start, t), b = Math.min(fim, t + 3600000L);
            double h = (b - a) / 3600000.0;
            double rev = 0;
            for (Ride x : rs) if (x.ts >= t && x.ts < t + 3600000L) rev += x.value;
            double custo = h * cfg.custoHora() + (r.horas > 0 ? r.km * (h / r.horas) * cfg.custoKm() : 0);
            lista.add(h >= 0.25 ? (rev - custo) / h : Double.NaN);
        }
        int n = Math.min(12, lista.size());
        Barras out = new Barras();
        out.inicio = (primeiraHora + (lista.size() - n)) % 24;
        out.valores = new double[n];
        for (int i = 0; i < n; i++) out.valores[i] = lista.get(lista.size() - n + i);
        return out;
    }

    public boolean hasDemo() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM shifts WHERE demo=1", null);
        try { c.moveToFirst(); return c.getInt(0) > 0; } finally { c.close(); }
    }

    public void clearDemo() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("rides", "demo=1", null);
        db.delete("shifts", "demo=1", null);
    }

    /** DADOS SINTÉTICOS para demonstração: 28 dias de turnos. Não são dados reais. */
    public void seedDemo() {
        clearDemo();
        SQLiteDatabase db = getWritableDatabase();
        Random r = new Random(42);
        int[][][] pat = {{{11, 18}}, {{18, 23}}, {{11, 15}, {19, 23}}, {{7, 11}, {17, 22}}};
        // qualidade da demanda [turno][dia seg..dom] (1.0 = média)
        double[][] q = {
                {0.8, 0.85, 0.8, 0.75, 0.9, 1.0, 0.5},
                {1.2, 1.3, 1.25, 1.15, 1.35, 1.5, 1.2},
                {0.9, 0.8, 0.12, 0.75, 1.0, 1.1, 0.95},
                {1.2, 1.3, 1.25, 1.3, 1.5, 1.7, 1.1}};
        double odo = 12000;
        db.beginTransaction();
        try {
            for (int d = 28; d >= 1; d--) {
                if (r.nextInt(10) == 0) continue;
                Calendar day = Calendar.getInstance();
                day.set(Calendar.HOUR_OF_DAY, 0); day.set(Calendar.MINUTE, 0);
                day.set(Calendar.SECOND, 0); day.set(Calendar.MILLISECOND, 0);
                day.add(Calendar.DAY_OF_YEAR, -d);
                int dow = calculo.diaIdx(day.get(Calendar.DAY_OF_WEEK));
                int[][] wins = pat[r.nextInt(pat.length)];
                for (int[] w : wins) {
                    long t0 = day.getTimeInMillis() + w[0] * 3600000L + r.nextInt(20) * 60000L;
                    long t1 = day.getTimeInMillis() + w[1] * 3600000L + r.nextInt(20) * 60000L;
                    ContentValues sv = new ContentValues();
                    sv.put("t_start", t0); sv.put("t_end", t1);
                    sv.put("odo_start", odo); sv.put("odo_end", -1); sv.put("demo", 1);
                    long sid = db.insert("shifts", null, sv);
                    long t = t0;
                    double kmSum = 0;
                    while (true) {
                        Calendar cc = Calendar.getInstance();
                        cc.setTimeInMillis(t);
                        double qq = q[calculo.turno(cc.get(Calendar.HOUR_OF_DAY))][dow];
                        t += (long) ((8 + r.nextDouble() * 8) / qq * 60000.0);
                        double mins = 12 + r.nextDouble() * 13;
                        if (t + (long) (mins * 60000.0) > t1) break;
                        double km = 3 + r.nextDouble() * 4;
                        double val = (9 + r.nextDouble() * 9) * (0.8 + 0.2 * qq);
                        ContentValues rv = new ContentValues();
                        rv.put("shift_id", sid); rv.put("ts", t);
                        rv.put("amount", Math.round(val * 100) / 100.0);
                        rv.put("km", Math.round(km * 10) / 10.0);
                        rv.put("mins", (double) Math.round(mins)); rv.put("demo", 1);
                        db.insert("rides", null, rv);
                        kmSum += km;
                        t += (long) (mins * 60000.0);
                    }
                    double odoEnd = odo + Math.round(kmSum * 1.25);  // inclui km vazio
                    ContentValues up = new ContentValues();
                    up.put("odo_end", odoEnd);
                    db.update("shifts", up, "id=?", new String[]{String.valueOf(sid)});
                    odo = odoEnd;
                }
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
}