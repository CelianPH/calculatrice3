package com.example.calculatrice3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView textResult;
    private boolean isNewCalculation = true;
    private List<String> calculationHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textResult = findViewById(R.id.text_result);
        calculationHistory = new ArrayList<>();
    }

    public void appendToResult(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        if (isNewCalculation) {
            textResult.setText(buttonText);
            isNewCalculation = false;
        } else {
            String currentText = textResult.getText().toString();
            textResult.setText(currentText + buttonText);
        }
    }

    public void calculateResult(View view) {
        String expression = textResult.getText().toString();

        try {
            double result = evaluateExpression(expression);
            DecimalFormat decimalFormat = new DecimalFormat("#.#####");
            textResult.setText(decimalFormat.format(result));

            // Ajouter le calcul à l'historique
            calculationHistory.add(expression + " = " + decimalFormat.format(result));
        } catch (Exception e) {
            textResult.setText("Error");
        }

        isNewCalculation = true;
    }

    public void clearResult(View view) {
        textResult.setText("0");
        isNewCalculation = true;
    }

    public void showHistory(View view) {
        StringBuilder history = new StringBuilder();
        for (String calculation : calculationHistory) {
            history.append(calculation).append("\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Historique des calculs");
        builder.setMessage(history.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private double evaluateExpression(String expression) {
        return new Object() {
            int index = -1, nextChar;

            void nextChar() {
                if (++index < expression.length()) {
                    nextChar = expression.charAt(index);
                } else {
                    nextChar = -1;
                }
            }

            boolean isDigit() {
                return Character.isDigit(nextChar);
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (index < expression.length()) {
                    throw new RuntimeException("Unexpected: " + (char) nextChar);
                }
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) {
                        x += parseTerm();
                    } else if (eat('-')) {
                        x -= parseTerm();
                    } else {
                        return x;
                    }
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) {
                        x *= parseFactor();
                    } else if (eat('/')) {
                        x /= parseFactor();
                    } else {
                        return x;
                    }
                }
            }

            double parseFactor() {
                if (eat('+')) {
                    return parseFactor();
                }
                if (eat('-')) {
                    return -parseFactor();
                }

                double x;
                int startPos = this.index;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if (isDigit()) {
                    while (isDigit()) {
                        nextChar();
                    }
                    x = Double.parseDouble(expression.substring(startPos, this.index));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) nextChar);
                }

                return x;
            }

            boolean eat(int charToEat) {
                while (nextChar == ' ') {
                    nextChar();
                }
                if (nextChar == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }
        }.parse();
    }
}
