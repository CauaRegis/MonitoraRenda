package com.example.monitorarenda;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Barras por hora do turno.
 * Coral = hora que não pagou o custo, verde = hora forte, cinza = hora média.
 */
public class HourBarsView extends View {

    private double[] values = new double[0];   // R$/h líquido de cada hora (NaN = sem dado)
    private int startHour = 7;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public HourBarsView(Context context) {
        this(context, null);
    }

    public HourBarsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint.setColor(context.getColor(R.color.mr_text_tertiary));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 11, getResources().getDisplayMetrics()));
        if (isInEditMode()) {  // só para aparecer na aba Design
            setData(new double[]{10, 18, 6, -3, 12, 30, 34, 16, 5, -4, 14, 30}, 7);
        }
    }

    /** values[i] = ganho líquido por hora da hora (startHour + i). */
    public void setData(double[] values, int startHour) {
        this.values = values == null ? new double[0] : values;
        this.startHour = startHour;
        invalidate();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = values.length;
        if (n == 0) return;

        float areaH = getHeight() - dp(22);       // sobra espaço embaixo para os rótulos
        float slot = getWidth() / (float) n;
        float barW = Math.min(dp(18), slot * 0.62f);

        double max = 1;
        for (double v : values) {
            if (!Double.isNaN(v)) max = Math.max(max, v);
        }

        int lime = getContext().getColor(R.color.mr_lime);
        int coral = getContext().getColor(R.color.mr_coral);
        int gray = getContext().getColor(R.color.mr_inverse_raised);

        for (int i = 0; i < n; i++) {
            double v = values[i];
            float h;
            int color;
            if (Double.isNaN(v)) {
                h = dp(10);
                color = gray;
            } else if (v < 0) {
                h = dp(8);
                color = coral;
            } else {
                h = (float) (dp(14) + (areaH - dp(14)) * Math.min(1.0, v / max));
                color = v >= 22 ? lime : gray;
            }
            float cx = slot * i + slot / 2f;
            float cy = areaH / 2f;
            rect.set(cx - barW / 2f, cy - h / 2f, cx + barW / 2f, cy + h / 2f);
            barPaint.setColor(color);
            canvas.drawRoundRect(rect, barW / 2f, barW / 2f, barPaint);

            if (i % 3 == 0 || i == n - 1) {
                canvas.drawText((startHour + i) + "H", cx, getHeight() - dp(4), textPaint);
            }
        }
    }
}