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

        // Calcola altezze precise considerando padding
        int totalPadding = 20; // 10 top + 10 bottom del keyboardContainer
        int availableHeight = screenHeight - totalPadding;

        int displayHeight = (int) (availableHeight * 0.05);    // 5% per display
        int keyboardHeight = (int) (availableHeight * 0.25);   // 25% per tastiera
        int gameViewHeight = availableHeight - displayHeight - keyboardHeight; // Resto per gioco

        // 1. GameView (usa peso per riempire spazio rimanente)
        gameView = new GameView(this, screenWidth, gameViewHeight, this);
        LinearLayout.LayoutParams gameParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            0.70f  // Peso 70%
        );
        gameView.setLayoutParams(gameParams);
        mainLayout.addView(gameView);

        // 2. Input Display
        inputDisplay = new TextView(this);
        inputDisplay.setText("_");
        inputDisplay.setTextSize(0, displayHeight * 0.6f);
        inputDisplay.setTextColor(Color.WHITE);
        inputDisplay.setGravity(Gravity.CENTER);
        inputDisplay.setBackgroundColor(Color.parseColor("#16213E"));
        LinearLayout.LayoutParams displayParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            0.05f  // Peso 5%
        );
        inputDisplay.setLayoutParams(displayParams);
        mainLayout.addView(inputDisplay);

        // 3. Container per Keyboard + Progress Bar
        LinearLayout keyboardContainer = new LinearLayout(this);
        keyboardContainer.setOrientation(LinearLayout.HORIZONTAL);
        keyboardContainer.setBackgroundColor(Color.parseColor("#1A1A2E"));
        keyboardContainer.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams keyboardContainerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            0.25f  // Peso 25%
        );
        keyboardContainer.setLayoutParams(keyboardContainerParams);

        // Progress bar verticale (lato sinistro, larghezza 3% dello schermo)
        final android.widget.ProgressBar progressBar = new android.widget.ProgressBar(
            this, null, android.R.attr.progressBarStyleHorizontal
        );
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setRotation(270); // Ruota 270° per renderla verticale
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#27AE60")));
        progressBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2C2C3E")));
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            (int) (screenWidth * 0.03),
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        progressBar.setLayoutParams(progressParams);
        keyboardContainer.addView(progressBar);

        // Keyboard (resto dello spazio - 97% larghezza)
        int keyboardWidth = screenWidth - (int) (screenWidth * 0.03) - 20; // -20 per padding
        keyboard = new KeyboardView(this, this, keyboardWidth, keyboardHeight);
        LinearLayout.LayoutParams keyboardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        keyboard.setLayoutParams(keyboardParams);
        keyboardContainer.addView(keyboard);

        mainLayout.addView(keyboardContainer);

        setContentView(mainLayout);

        // Inizializza InputManager con progress bar
        inputManager = new InputManager(this, progressBar);

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
