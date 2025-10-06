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

    public KeyboardView(Context context, KeyboardListener listener) {
        super(context);
        this.listener = listener;

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.parseColor("#2C2C3E"));

        // Crea griglia 4x3
        GridLayout grid = new GridLayout(context);
        grid.setRowCount(4);
        grid.setColumnCount(3);
        grid.setPadding(20, 20, 20, 20);

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
                View empty = new View(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 180;
                params.height = 140;
                params.setMargins(5, 5, 5, 5);
                empty.setLayoutParams(params);
                grid.addView(empty);
            } else {
                Button btn = createButton(context, key);
                grid.addView(btn);
            }
        }

        addView(grid);
    }

    private Button createButton(Context context, final int key) {
        Button btn = new Button(context);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 180;
        params.height = 140;
        params.setMargins(5, 5, 5, 5);
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
