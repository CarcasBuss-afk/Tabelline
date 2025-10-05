package com.tabelline;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.graphics.Color;
import android.text.InputType;
import java.util.Random;

public class MainActivity extends Activity {
    private TextView questionText, scoreText;
    private EditText answerInput;
    private Button submitButton, newGameButton;
    private int num1, num2, correctAnswer, score = 0, questionsAsked = 0;
    private Random random = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));

        scoreText = new TextView(this);
        scoreText.setText("Punteggio: 0 / 0");
        scoreText.setTextSize(18);
        scoreText.setTextColor(Color.parseColor("#333333"));
        scoreText.setPadding(0, 0, 0, 60);
        layout.addView(scoreText);

        questionText = new TextView(this);
        questionText.setTextSize(48);
        questionText.setTextColor(Color.parseColor("#2196F3"));
        questionText.setPadding(0, 0, 0, 80);
        layout.addView(questionText);

        answerInput = new EditText(this);
        answerInput.setHint("Risposta");
        answerInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        answerInput.setTextSize(32);
        answerInput.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT);
        p1.setMargins(0, 0, 0, 60);
        answerInput.setLayoutParams(p1);
        layout.addView(answerInput);

        submitButton = new Button(this);
        submitButton.setText("Verifica");
        submitButton.setTextSize(20);
        submitButton.setBackgroundColor(Color.parseColor("#4CAF50"));
        submitButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT);
        p2.setMargins(0, 0, 0, 40);
        submitButton.setLayoutParams(p2);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { checkAnswer(); }
        });
        layout.addView(submitButton);

        newGameButton = new Button(this);
        newGameButton.setText("Nuovo Gioco");
        newGameButton.setTextSize(18);
        newGameButton.setBackgroundColor(Color.parseColor("#FF9800"));
        newGameButton.setTextColor(Color.WHITE);
        newGameButton.setLayoutParams(new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT));
        newGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { resetGame(); }
        });
        layout.addView(newGameButton);

        setContentView(layout);
        generateQuestion();
    }

    private void generateQuestion() {
        num1 = random.nextInt(10) + 1;
        num2 = random.nextInt(10) + 1;
        correctAnswer = num1 * num2;
        questionText.setText(num1 + " × " + num2 + " = ?");
        answerInput.setText("");
    }

    private void checkAnswer() {
        String s = answerInput.getText().toString();
        if (s.isEmpty()) {
            Toast.makeText(this, "Inserisci risposta!", Toast.LENGTH_SHORT).show();
            return;
        }
        int ans = Integer.parseInt(s);
        questionsAsked++;
        if (ans == correctAnswer) {
            score++;
            Toast.makeText(this, "Corretto! 🎉", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sbagliato! Era: " + correctAnswer, Toast.LENGTH_SHORT).show();
        }
        scoreText.setText("Punteggio: " + score + " / " + questionsAsked);
        if (questionsAsked < 10) {
            generateQuestion();
        } else {
            endGame();
        }
    }

    private void endGame() {
        questionText.setText("Fine!");
        submitButton.setEnabled(false);
        answerInput.setEnabled(false);
        String msg = score + " / 10";
        if (score == 10) msg += " Perfetto! 🏆";
        else if (score >= 7) msg += " Bravo! 👏";
        else if (score >= 5) msg += " Buon lavoro! 👍";
        else msg += " Continua ad esercitarti! 💪";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void resetGame() {
        score = 0;
        questionsAsked = 0;
        submitButton.setEnabled(true);
        answerInput.setEnabled(true);
        scoreText.setText("Punteggio: 0 / 0");
        generateQuestion();
    }
}
