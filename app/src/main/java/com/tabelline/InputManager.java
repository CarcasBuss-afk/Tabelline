package com.tabelline;

import android.os.Handler;
import java.util.ArrayList;

public class InputManager {
    public interface InputListener {
        void onInputChanged(String display);
        void onAnswerSubmitted(ArrayList<Integer> factors);
    }

    private ArrayList<Integer> factors = new ArrayList<>();
    private String currentFactor = "";
    private Handler handler = new Handler();
    private InputListener listener;
    private VerticalProgressBar progressBar;

    private static final long FACTOR_DELAY = 700; // 0.7 secondi per finalizzare fattore
    private static final long CONFIRM_DELAY = 700;  // 0.7 secondi per confermare
    private static final long PROGRESS_UPDATE_INTERVAL = 16; // ~60 FPS

    private long timerStartTime = 0;
    private long timerDuration = 0;
    private boolean timerRunning = false;

    public InputManager(InputListener listener, VerticalProgressBar progressBar) {
        this.listener = listener;
        this.progressBar = progressBar;
    }

    // Timer che finalizza il fattore corrente aggiungendo ×
    private Runnable factorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!currentFactor.isEmpty()) {
                try {
                    int factor = Integer.parseInt(currentFactor);
                    factors.add(factor);
                    currentFactor = "";
                    notifyInputChanged();
                    // Avvia timer per confermare se non arrivano altri numeri
                    startProgressAnimation(CONFIRM_DELAY);
                    handler.postDelayed(confirmRunnable, CONFIRM_DELAY);
                } catch (NumberFormatException e) {
                    reset();
                }
            }
        }
    };

    // Timer che conferma automaticamente
    private Runnable confirmRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentFactor.isEmpty() && !factors.isEmpty()) {
                submitAnswer();
            }
        }
    };

    public void addDigit(int digit) {
        // Cancella timer precedenti
        handler.removeCallbacks(factorRunnable);
        handler.removeCallbacks(confirmRunnable);
        stopProgressAnimation();

        // Aggiungi cifra al fattore corrente
        currentFactor += digit;
        notifyInputChanged();

        // Avvia timer per finalizzare questo fattore
        startProgressAnimation(FACTOR_DELAY);
        handler.postDelayed(factorRunnable, FACTOR_DELAY);
    }

    public void reset() {
        handler.removeCallbacks(factorRunnable);
        handler.removeCallbacks(confirmRunnable);
        stopProgressAnimation();
        factors.clear();
        currentFactor = "";
        notifyInputChanged();
    }

    private void submitAnswer() {
        if (factors.isEmpty()) {
            reset();
            return;
        }

        listener.onAnswerSubmitted(new ArrayList<>(factors));
        reset();
    }

    private void notifyInputChanged() {
        StringBuilder display = new StringBuilder();

        // Aggiungi fattori già confermati con ×
        for (int i = 0; i < factors.size(); i++) {
            if (i > 0) display.append("×");
            display.append(factors.get(i));
        }

        // Aggiungi × prima del fattore corrente se ci sono già fattori
        if (!factors.isEmpty() && !currentFactor.isEmpty()) {
            display.append("×");
        }

        // Aggiungi fattore corrente
        display.append(currentFactor);

        // Aggiungi cursore
        display.append("_");

        listener.onInputChanged(display.toString());
    }

    public String getCurrentInput() {
        StringBuilder display = new StringBuilder();

        for (int i = 0; i < factors.size(); i++) {
            if (i > 0) display.append("×");
            display.append(factors.get(i));
        }

        if (!factors.isEmpty() && !currentFactor.isEmpty()) {
            display.append("×");
        }

        display.append(currentFactor);
        display.append("_");

        return display.toString();
    }

    public void cleanup() {
        handler.removeCallbacks(factorRunnable);
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
