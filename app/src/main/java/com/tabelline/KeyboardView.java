package com.tabelline;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;

public class KeyboardView extends LinearLayout {
    public interface KeyboardListener {
        void onNumberPressed(int number);
        void onResetPressed();
    }

    private KeyboardListener listener;

    public KeyboardView(Context context, KeyboardListener listener, int width, int height) {
        super(context);
        this.listener = listener;

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.parseColor("#2C2C3E"));

        // Crea subito la tastiera con le dimensioni passate
        createKeyboard(width, height);
    }

    private void createKeyboard(int width, int height) {
        // Crea griglia 4x3
        GridLayout grid = new GridLayout(getContext());
        grid.setRowCount(4);
        grid.setColumnCount(3);

        int padding = 20;
        grid.setPadding(padding, padding, padding, padding);

        // Calcola dimensioni tasti in base allo spazio disponibile
        int totalWidth = width - (padding * 2);
        int totalHeight = height - (padding * 2);

        int buttonWidth = (totalWidth / 3) - 16;   // 3 colonne
        int buttonHeight = (totalHeight / 4) - 16; // 4 righe
        int margin = 8;

        // Layout tasti:
        // 1 2 3
        // 4 5 6
        // 7 8 9
        // ⌫ 0 (vuoto)

        int[] keys = {1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 0, -2};
        // -1 = RESET (⌫)
        // -2 = placeholder vuoto

        for (int key : keys) {
            if (key == -2) {
                // Spazio vuoto
                View empty = new View(getContext());
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonWidth;
                params.height = buttonHeight;
                params.setMargins(margin, margin, margin, margin);
                empty.setLayoutParams(params);
                grid.addView(empty);
            } else {
                Button btn = createButton(key, buttonWidth, buttonHeight, margin);
                grid.addView(btn);
            }
        }

        addView(grid);
    }

    private Button createButton(final int key, int width, int height, int margin) {
        Button btn = new Button(getContext());

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = width;
        params.height = height;
        params.setMargins(margin, margin, margin, margin);
        btn.setLayoutParams(params);

        if (key == -1) {
            // Tasto RESET
            btn.setText("⌫");
            btn.setTextSize(32);
            btn.setBackgroundColor(Color.parseColor("#E74C3C"));
        } else {
            // Tasti numerici
            btn.setText(String.valueOf(key));
            btn.setTextSize(28);
            btn.setBackgroundColor(Color.parseColor("#3498DB"));
        }

        btn.setTextColor(Color.WHITE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (key == -1) {
                    listener.onResetPressed();
                } else {
                    listener.onNumberPressed(key);
                }
            }
        });

        return btn;
    }
}
