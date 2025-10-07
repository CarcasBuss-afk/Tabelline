package com.tabelline;

public class ComboResult {
    public int ballsDestroyed;
    public int totalScore;
    public boolean success;

    public ComboResult(boolean success, int ballsDestroyed, int totalScore) {
        this.success = success;
        this.ballsDestroyed = ballsDestroyed;
        this.totalScore = totalScore;
    }
}
