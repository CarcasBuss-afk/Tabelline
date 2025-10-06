package com.tabelline;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class InputManager {
    public interface InputListener {
        void onInputChanged(String display);
        void onAnswerSubmitted(int factor1, int factor2);
    }

    private String currentInput = "";
    private Handler handler = new Handler();
    private InputListener listener;
    private View progressBar;

    private static final long MULTIPLY_DELAY = 700; // 0.7 secondi
    private static final long CONFIRM_DELAY = 700;  // 0.7 secondi

    public InputManager(InputListener listener, View progressBar) {
        this.listener = listener;
        this.progressBar = progressBar;
    }

    // Timer che aggiunge automaticamente ×
    private Runnable multiplyRunnable = new Runnable() {
        @Override
        public void run() {
            if (!currentInput.contains("×") && !currentInput.isEmpty()) {
                currentInput += "×";
                notifyInputChanged();
            }
        }
    };

    // Timer che conferma automaticamente
    private Runnable confirmRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentInput.contains("×")) {
                submitAnswer();
            }
        }
    };

    public void addDigit(int digit) {
        // Cancella timer precedenti
        handler.removeCallbacks(multiplyRunnable);
        handler.removeCallbacks(confirmRunnable);
        stopProgressAnimation();

        if (!currentInput.contains("×")) {
            // Primo fattore
            currentInput += digit;
            notifyInputChanged();
            // Avvia timer per aggiungere × e anima progress bar
            startProgressAnimation(MULTIPLY_DELAY);
            handler.postDelayed(multiplyRunnable, MULTIPLY_DELAY);
        } else {
            // Secondo fattore
            currentInput += digit;
            notifyInputChanged();
            // Avvia timer per confermare e anima progress bar
            startProgressAnimation(CONFIRM_DELAY);
            handler.postDelayed(confirmRunnable, CONFIRM_DELAY);
        }
    }

    public void reset() {
        handler.removeCallbacks(multiplyRunnable);
        handler.removeCallbacks(confirmRunnable);
        stopProgressAnimation();
        currentInput = "";
        notifyInputChanged();
    }

    private void submitAnswer() {
        if (!currentInput.contains("×")) {
            reset();
            return;
        }

        try {
            String[] parts = currentInput.split("×");
            if (parts.length != 2) {
                reset();
                return;
            }

            int factor1 = Integer.parseInt(parts[0]);
            int factor2 = Integer.parseInt(parts[1]);

            listener.onAnswerSubmitted(factor1, factor2);
            reset();
        } catch (Exception e) {
            reset();
        }
    }

    private void notifyInputChanged() {
        String display = currentInput.isEmpty() ? "_" : currentInput + "_";
        listener.onInputChanged(display);
    }

    public String getCurrentInput() {
        return currentInput.isEmpty() ? "_" : currentInput + "_";
    }

    public void cleanup() {
        handler.removeCallbacks(multiplyRunnable);
        handler.removeCallbacks(confirmRunnable);
        stopProgressAnimation();
    }

    // Anima la progress bar da sinistra a destra in base al tempo
    private void startProgressAnimation(long duration) {
        if (progressBar == null) return;

        // Animazione di scala orizzontale da 0 a 1
        ScaleAnimation scaleAnim = new ScaleAnimation(
            0.0f, 1.0f,    // Da 0% a 100% in larghezza
            1.0f, 1.0f,    // Altezza rimane al 100%
            Animation.ABSOLUTE, 0,  // Pivot X a sinistra
            Animation.RELATIVE_TO_SELF, 0.5f  // Pivot Y al centro
        );
        scaleAnim.setDuration(duration);
        scaleAnim.setFillAfter(true);

        progressBar.startAnimation(scaleAnim);
    }

    private void stopProgressAnimation() {
        if (progressBar == null) return;
        progressBar.clearAnimation();
        progressBar.setScaleX(0.0f); // Reset a 0%
    }
}
