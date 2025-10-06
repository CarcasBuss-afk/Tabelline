package com.tabelline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private SurfaceHolder surfaceHolder;
    private boolean playing = false;
    private Paint paint;
    private GameEngine gameEngine;
    private GameOverListener gameOverListener;

    public interface GameOverListener {
        void onGameOver(int score, int level);
    }

    public GameView(Context context, int width, int height, GameOverListener listener) {
        super(context);
        surfaceHolder = getHolder();
        paint = new Paint();
        gameEngine = new GameEngine(width, height);
        gameOverListener = listener;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public void startGame() {
        playing = true;
        gameEngine.reset();
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGame() {
        playing = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        while (playing) {
            long currentTime = System.currentTimeMillis();

            if (!gameEngine.gameOver) {
                update(currentTime);
            }

            draw();
            control();

            // Controlla game over
            if (gameEngine.gameOver && playing) {
                playing = false;
                if (gameOverListener != null) {
                    gameOverListener.onGameOver(gameEngine.score, gameEngine.level);
                }
            }
        }
    }

    private void update(long currentTime) {
        gameEngine.trySpawnBall(currentTime);
        gameEngine.updateBalls();
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();

            // Sfondo
            canvas.drawColor(Color.parseColor("#1A1A2E"));

            // Disegna tutte le palline
            for (Ball ball : gameEngine.getBalls()) {
                ball.draw(canvas, paint);
            }

            // Header info
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Score: " + gameEngine.score, 30, 60, paint);

            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Level: " + gameEngine.level, getWidth() - 30, 60, paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            Thread.sleep(16); // ~60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
