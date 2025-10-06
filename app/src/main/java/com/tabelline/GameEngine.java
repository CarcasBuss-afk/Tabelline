package com.tabelline;

import java.util.ArrayList;
import java.util.Iterator;

public class GameEngine {
    public int level = 1;
    public int score = 0;
    public int ballsDestroyed = 0;
    public boolean gameOver = false;

    private ArrayList<Ball> balls;
    private long lastSpawnTime = 0;
    private int screenWidth;
    private int gameAreaHeight;

    public GameEngine(int screenWidth, int gameAreaHeight) {
        this.screenWidth = screenWidth;
        this.gameAreaHeight = gameAreaHeight;
        this.balls = new ArrayList<>();
    }

    public ArrayList<Ball> getBalls() {
        return balls;
    }

    // Calcola numero massimo in base al livello
    public int getMaxNumber() {
        if (level <= 5) return 6;
        if (level <= 10) return 8;
        if (level <= 15) return 12;
        if (level <= 20) return 15;
        return 20;
    }

    // Calcola intervallo spawn in base al livello
    public long getSpawnInterval() {
        if (level <= 5) return 5000;  // 5 secondi
        if (level <= 10) return 4000; // 4 secondi
        if (level <= 15) return 3000; // 3 secondi
        if (level <= 20) return 2000; // 2 secondi
        return 1000; // 1 secondo
    }

    // Spawna nuova pallina se è passato abbastanza tempo
    public void trySpawnBall(long currentTime) {
        if (currentTime - lastSpawnTime >= getSpawnInterval()) {
            float randomX = 100 + (float) Math.random() * (screenWidth - 200);
            balls.add(new Ball(randomX, -100, getMaxNumber()));
            lastSpawnTime = currentTime;
        }
    }

    // Aggiorna posizione palline e controlla collisioni
    public void updateBalls() {
        for (Ball ball : balls) {
            ball.update();

            // Controlla se ha toccato il fondo
            if (ball.reachedBottom(gameAreaHeight)) {
                gameOver = true;
                return;
            }
        }
    }

    // Controlla risposta e distrugge palline
    public int checkAnswer(int inputFactor1, int inputFactor2) {
        int inputResult = inputFactor1 * inputFactor2;
        int destroyedCount = 0;

        Iterator<Ball> iterator = balls.iterator();
        while (iterator.hasNext()) {
            Ball ball = iterator.next();
            if (ball.result == inputResult) {
                iterator.remove();
                destroyedCount++;
                ballsDestroyed++;
                score += 10;

                // Level up ogni 10 palline distrutte
                if (ballsDestroyed % 10 == 0) {
                    level++;
                }
            }
        }

        return destroyedCount;
    }

    public void reset() {
        level = 1;
        score = 0;
        ballsDestroyed = 0;
        gameOver = false;
        balls.clear();
        lastSpawnTime = 0;
    }
}
