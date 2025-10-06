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
    private GridLayout grid;

    public KeyboardView(Context context, KeyboardListener listener) {
        super(context);
        this.listener = listener;

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.parseColor("#2C2C3E"));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // Crea la tastiera solo quando conosciamo le dimensioni reali
        if (changed && grid == null) {
            int width = getWidth();
            int height = getHeight();
            if (width > 0 && height > 0) {
                createKeyboard(width, height);
            }
        }
    }

    private void createKeyboard(int width, int height) {
        // Crea griglia 4x3 che riempie tutto lo spazio
        grid = new GridLayout(getContext());
        grid.setRowCount(4);
        grid.setColumnCount(3);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // Padding uniforme
        int padding = 10;
        grid.setPadding(padding, padding, padding, padding);

        // Margine tra i tasti
        int margin = 8;

        // Calcola dimensioni esatte per ogni cella
        int buttonWidth = (width - (padding * 2) - (margin * 6)) / 3;
        int buttonHeight = (height - (padding * 2) - (margin * 8)) / 4;

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

        // Font size = 40% dell'altezza del pulsante
        float textSize = height * 0.4f;

        if (key == -1) {
            // Tasto RESET
            btn.setText("⌫");
            btn.setTextSize(0, textSize);
            btn.setBackgroundColor(Color.parseColor("#E74C3C"));
        } else {
            // Tasti numerici
            btn.setText(String.valueOf(key));
            btn.setTextSize(0, textSize);
            btn.setBackgroundColor(Color.parseColor("#3498DB"));
        }

        btn.setTextColor(Color.WHITE);
        btn.setPadding(0, 0, 0, 0);

        // Rimuovi minWidth e minHeight di default di Android
        btn.setMinWidth(0);
        btn.setMinHeight(0);
        btn.setMinimumWidth(0);
        btn.setMinimumHeight(0);

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
