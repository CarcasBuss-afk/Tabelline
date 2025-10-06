package com.tabelline;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity
    implements InputManager.InputListener,
               KeyboardView.KeyboardListener,
               GameView.GameOverListener {

    private GameView gameView;
    private TextView inputDisplay;
    private KeyboardView keyboard;
    private InputManager inputManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ottieni dimensioni schermo
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // Layout principale verticale
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#1A1A2E"));

        // 1. GameView (77% altezza)
        int gameViewHeight = (int) (screenHeight * 0.77);
        gameView = new GameView(this, screenWidth, gameViewHeight, this);
        LinearLayout.LayoutParams gameParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            gameViewHeight
        );
        gameView.setLayoutParams(gameParams);
        mainLayout.addView(gameView);

        // 2. Input Display (3% altezza)
        int displayHeight = (int) (screenHeight * 0.03);
        inputDisplay = new TextView(this);
        inputDisplay.setText("_");
        inputDisplay.setTextSize(32);
        inputDisplay.setTextColor(Color.WHITE);
        inputDisplay.setGravity(Gravity.CENTER);
        inputDisplay.setBackgroundColor(Color.parseColor("#16213E"));
        LinearLayout.LayoutParams displayParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            displayHeight
        );
        inputDisplay.setLayoutParams(displayParams);
        mainLayout.addView(inputDisplay);

        // 3. Keyboard (20% altezza rimanente)
        keyboard = new KeyboardView(this, this);
        LinearLayout.LayoutParams keyboardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        keyboard.setLayoutParams(keyboardParams);
        mainLayout.addView(keyboard);

        setContentView(mainLayout);

        // Inizializza InputManager
        inputManager = new InputManager(this);

        // Avvia gioco
        gameView.startGame();
    }

    // InputManager.InputListener
    @Override
    public void onInputChanged(String display) {
        inputDisplay.setText(display);
    }

    @Override
    public void onAnswerSubmitted(int factor1, int factor2) {
        int destroyedCount = gameView.getGameEngine().checkAnswer(factor1, factor2);

        // Feedback visivo (opzionale - puoi rimuovere se rallenta)
        if (destroyedCount > 0) {
            inputDisplay.setBackgroundColor(Color.parseColor("#27AE60")); // Verde
            inputDisplay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    inputDisplay.setBackgroundColor(Color.parseColor("#16213E"));
                }
            }, 300);
        } else {
            inputDisplay.setBackgroundColor(Color.parseColor("#E74C3C")); // Rosso
            inputDisplay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    inputDisplay.setBackgroundColor(Color.parseColor("#16213E"));
                }
            }, 300);
        }
    }

    // KeyboardView.KeyboardListener
    @Override
    public void onNumberPressed(int number) {
        inputManager.addDigit(number);
    }

    @Override
    public void onResetPressed() {
        inputManager.reset();
    }

    // GameView.GameOverListener
    @Override
    public void onGameOver(final int score, final int level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Game Over!")
                    .setMessage("Punteggio: " + score + "\nLivello raggiunto: " + level)
                    .setPositiveButton("Rigioca", (dialog, which) -> {
                        gameView.startGame();
                        inputManager.reset();
                    })
                    .setNegativeButton("Esci", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.stopGame();
        inputManager.cleanup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Il gioco si riavvia automaticamente tramite dialog dopo game over
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameView.stopGame();
        inputManager.cleanup();
    }
}
