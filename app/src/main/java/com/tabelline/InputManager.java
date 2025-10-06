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

    private static final long MULTIPLY_DELAY = 1000; // 1 secondo
    private static final long CONFIRM_DELAY = 1000;  // 1 secondo

    public InputManager(InputListener listener) {
        this.listener = listener;
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

        if (!currentInput.contains("×")) {
            // Primo fattore
            currentInput += digit;
            notifyInputChanged();
            // Avvia timer per aggiungere ×
            handler.postDelayed(multiplyRunnable, MULTIPLY_DELAY);
        } else {
            // Secondo fattore
            currentInput += digit;
            notifyInputChanged();
            // Avvia timer per confermare
            handler.postDelayed(confirmRunnable, CONFIRM_DELAY);
        }
    }

    public void reset() {
        handler.removeCallbacks(multiplyRunnable);
        handler.removeCallbacks(confirmRunnable);
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
    }
}
