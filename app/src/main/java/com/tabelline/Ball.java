package com.tabelline;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import java.util.Random;

public class Ball {
    private static Random random = new Random();

    public float x, y;
    public float speed;
    public int result;
    public int factor1, factor2;
    public int radius = 60;
    public int color;

    public Ball(float startX, float startY, int maxNumber) {
        this.x = startX;
        this.y = startY;

        // Genera moltiplicazione casuale (>=2 per escludere moltiplicazioni per 1 e numeri primi)
        this.factor1 = random.nextInt(maxNumber - 1) + 2;
        this.factor2 = random.nextInt(maxNumber - 1) + 2;
        this.result = factor1 * factor2;

        // Velocità casuale
        this.speed = 2 + random.nextFloat() * 2; // 2-4 pixel/frame

        // Colore casuale
        int[] colors = {
            Color.parseColor("#FF5722"), // Rosso
            Color.parseColor("#4CAF50"), // Verde
            Color.parseColor("#2196F3"), // Blu
            Color.parseColor("#FFC107"), // Giallo
            Color.parseColor("#9C27B0"), // Viola
            Color.parseColor("#FF9800")  // Arancione
        };
        this.color = colors[random.nextInt(colors.length)];
    }

    public void update() {
        y += speed;
    }

    public void draw(Canvas canvas, Paint paint) {
        // Disegna cerchio
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radius, paint);

        // Disegna bordo
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(x, y, radius, paint);

        // Disegna numero
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);

        String text = String.valueOf(result);
        float textY = y + 15; // Centra verticalmente
        canvas.drawText(text, x, textY, paint);
    }

    public boolean reachedBottom(int gameAreaHeight) {
        // La parte inferiore della pallina è y + radius
        return y + radius >= gameAreaHeight;
    }
}
