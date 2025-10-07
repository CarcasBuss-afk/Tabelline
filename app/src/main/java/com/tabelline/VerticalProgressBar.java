package com.tabelline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class VerticalProgressBar extends View {
    private int progress = 0;
    private int max = 100;
    private Paint paint;

    public VerticalProgressBar(Context context) {
        super(context);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Sfondo grigio scuro
        paint.setColor(Color.parseColor("#2C2C3E"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paint);

        // Calcola altezza della barra verde basata sul progresso
        float progressHeight = (height * progress) / (float) max;

        // Barra verde che cresce dal BASSO verso l'ALTO
        paint.setColor(Color.parseColor("#27AE60"));
        canvas.drawRect(0, height - progressHeight, width, height, paint);

        // Bordo bianco sottile per definizione
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(1, 1, width - 1, height - 1, paint);
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, max));
        invalidate(); // Ridisegna la view
    }

    public int getProgress() {
        return progress;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    public int getMax() {
        return max;
    }
}
