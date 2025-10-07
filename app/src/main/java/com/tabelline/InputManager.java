package com.tabelline;

import android.os.Handler;

public class InputManager {
    public interface InputListener {
        void onInputChanged(String display);
        void onAnswerSubmitted(int factor1, int factor2);
    }

    private String currentInput = "";
    private Handler handler = new Handler();
    private InputListener listener;
    private VerticalProgressBar progressBar;

    private static final long MULTIPLY_DELAY = 700; // 0.7 secondi
    private static final long CONFIRM_DELAY = 700;  // 0.7 secondi
    private static final long PROGRESS_UPDATE_INTERVAL = 16; // ~60 FPS

    private long timerStartTime = 0;
    private long timerDuration = 0;
    private boolean timerRunning = false;

    public InputManager(InputListener listener, VerticalProgressBar progressBar) {
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

    // Runnable per aggiornare la progress bar
    private Runnable progressUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!timerRunning || progressBar == null) return;

            long elapsed = System.currentTimeMillis() - timerStartTime;
            float progress = Math.min(1.0f, (float) elapsed / timerDuration);

            // Aggiorna progress (0-100)
            progressBar.setProgress((int) (progress * 100));

            if (progress < 1.0f) {
                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        }
    };

    private void startProgressAnimation(long duration) {
        if (progressBar == null) return;

        timerStartTime = System.currentTimeMillis();
        timerDuration = duration;
        timerRunning = true;

        // Reset a 0
        progressBar.setProgress(0);

        // Avvia aggiornamenti
        handler.post(progressUpdateRunnable);
    }

    private void stopProgressAnimation() {
        if (progressBar == null) return;

        timerRunning = false;
        handler.removeCallbacks(progressUpdateRunnable);

        // Reset a 0
        progressBar.setProgress(0);
    }
}
