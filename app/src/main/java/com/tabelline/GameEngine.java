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

    // Calcola intervallo spawn in base al livello (spawn più frequenti ai livelli alti)
    public long getSpawnInterval() {
        if (ballsDestroyed < 18) return 3000;  // Prime 18 palline: ogni 3 secondi
        if (ballsDestroyed < 36) return 2500;  // Palline 19-36: ogni 2.5 secondi
        if (ballsDestroyed < 60) return 2000;  // Palline 37-60: ogni 2 secondi
        return 1500; // Oltre 60: ogni 1.5 secondi
    }

    // Calcola quante palline spawnare contemporaneamente
    public int getSpawnCount() {
        if (ballsDestroyed < 6) return 1;      // Palline 1-6: singole
        if (ballsDestroyed < 12) return 2;     // Palline 7-12: doppie
        if (ballsDestroyed < 18) return 3;     // Palline 13-18: triple
        if (ballsDestroyed < 36) return 4;     // Palline 19-36: quadruple
        return 5; // Oltre 36: quintuple
    }

    // Spawna nuove palline se è passato abbastanza tempo
    public void trySpawnBall(long currentTime) {
        if (currentTime - lastSpawnTime >= getSpawnInterval()) {
            int count = getSpawnCount();

            // Spawna 'count' palline distanziate orizzontalmente
            for (int i = 0; i < count; i++) {
                float randomX = 100 + (float) Math.random() * (screenWidth - 200);
                balls.add(new Ball(randomX, -100, getMaxNumber(), getSpeedMultiplier()));
            }

            lastSpawnTime = currentTime;
        }
    }

    // Moltiplicatore velocità in base alle palline distrutte
    public float getSpeedMultiplier() {
        if (ballsDestroyed < 18) return 1.0f;   // Velocità normale
        if (ballsDestroyed < 36) return 1.2f;   // +20%
        if (ballsDestroyed < 60) return 1.5f;   // +50%
        return 2.0f; // Oltre 60: doppia velocità
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

    // Controlla risposta combo e distrugge palline
    public ComboResult checkAnswer(ArrayList<Integer> factors) {
        // Richiede almeno 2 fattori (una moltiplicazione)
        if (factors.size() < 2) {
            return new ComboResult(false, 0, 0);
        }

        // Calcola risultati progressivi
        ArrayList<Integer> results = new ArrayList<>();
        int currentResult = factors.get(0);
        results.add(currentResult);

        for (int i = 1; i < factors.size(); i++) {
            currentResult = currentResult * factors.get(i);
            results.add(currentResult);
        }

        // Verifica che TUTTE le palline con questi risultati esistano
        ArrayList<Ball> ballsToDestroy = new ArrayList<>();
        for (int result : results) {
            Ball found = null;
            for (Ball ball : balls) {
                if (ball.result == result && !ballsToDestroy.contains(ball)) {
                    found = ball;
                    break;
                }
            }

            if (found == null) {
                // Combo fallita: manca una pallina
                return new ComboResult(false, 0, 0);
            }

            ballsToDestroy.add(found);
        }

        // TUTTE le palline trovate → distruggi in ordine con punteggio esponenziale
        int totalScore = 0;
        for (int i = 0; i < ballsToDestroy.size(); i++) {
            Ball ball = ballsToDestroy.get(i);
            balls.remove(ball);
            ballsDestroyed++;

            // Punteggio: 1ª=10, 2ª=20, 3ª=40, 4ª=80 (formula: 10×2^i)
            int points = 10 * (1 << i); // bit shift = 2^i
            totalScore += points;
            score += points;

            // Level up ogni 10 palline distrutte
            if (ballsDestroyed % 10 == 0) {
                level++;
            }
        }

        return new ComboResult(true, ballsToDestroy.size(), totalScore);
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
