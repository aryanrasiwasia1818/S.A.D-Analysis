package com.example.mypersonalityapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {

    private TextView textViewQuestion;
    private RadioGroup radioGroupOptions;
    private RadioButton radioOption1, radioOption2, radioOption3, radioOption4, radioOption5;
    private Button buttonNext, buttonBack;

    private String[] oceanOptions = {
            "Very Inaccurate",
            "Moderately Inaccurate",
            "Neither Accurate Nor Inaccurate",
            "Moderately Accurate",
            "Very Accurate"
    };

    private String[] dassOptions = {
            "Did not apply to me at all (0)",
            "Applied to me to some degree (1)",
            "Applied to me to a considerable degree (2)",
            "Applied to me very much (3)"
    };

    private String[] questions = {
            // OCEAN (10)
            "Am the life of the party.",
            "Don't mind being the center of attention.",
            "Sympathize with others' feelings.",
            "Make people feel at ease.",
            "Pay attention to details.",
            "Follow a schedule.",
            "Am relaxed most of the time.",
            "Seldom feel blue.",
            "Have a vivid imagination.",
            "Am quick to understand things.",

            // STRESS (7)
            "I found it hard to wind down.",
            "I tended to over-react to situations.",
            "I felt that I was using a lot of nervous energy.",
            "I found myself getting agitated.",
            "I found it difficult to relax.",
            "I was intolerant of anything that kept me from getting on with what I was doing.",
            "I felt that I was rather touchy.",

            // ANXIETY (7)
            "I was aware of dryness of my mouth.",
            "I experienced breathing difficulty.",
            "I experienced trembling.",
            "I was worried about situations in which I might panic.",
            "I felt I was close to panic.",
            "I was aware of the action of my heart.",
            "I felt scared without any good reason.",

            // DEPRESSION (7)
            "I couldn’t seem to experience any positive feeling at all.",
            "I found it difficult to work up the initiative to do things.",
            "I felt that I had nothing to look forward to.",
            "I felt down-hearted and blue.",
            "I was unable to become enthusiastic about anything.",
            "I felt I wasn’t worth much as a person.",
            "I felt that life was meaningless."
    };

    private boolean[] isPositiveKeyed = {
            true, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, true, true,
            true, true, true, true, true, true, true,
            true, true, true, true, true, true, true
    };

    private String[] traitLabels = {
            "Extraversion", "Extraversion",
            "Agreeableness", "Agreeableness",
            "Conscientiousness", "Conscientiousness",
            "Emotional Stability", "Emotional Stability",
            "Openness", "Openness",

            "Stress", "Stress", "Stress", "Stress", "Stress", "Stress", "Stress",
            "Anxiety", "Anxiety", "Anxiety", "Anxiety", "Anxiety", "Anxiety", "Anxiety",
            "Depression", "Depression", "Depression", "Depression", "Depression", "Depression", "Depression"
    };

    private int currentQuestionIndex = 0;
    private int[] selectedAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.textViewQuestion);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        radioOption1 = findViewById(R.id.radioOption1);
        radioOption2 = findViewById(R.id.radioOption2);
        radioOption3 = findViewById(R.id.radioOption3);
        radioOption4 = findViewById(R.id.radioOption4);
        radioOption5 = findViewById(R.id.radioOption5);
        buttonNext = findViewById(R.id.buttonNext);
        buttonBack = findViewById(R.id.buttonBack);

        selectedAnswers = new int[questions.length];
        for (int i = 0; i < questions.length; i++) {
            selectedAnswers[i] = -1;
        }

        loadQuestion();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedOptionId = radioGroupOptions.getCheckedRadioButtonId();
                if (selectedOptionId == -1) return;

                int optionIndex = radioGroupOptions.indexOfChild(findViewById(selectedOptionId));
                selectedAnswers[currentQuestionIndex] = optionIndex;

                if (currentQuestionIndex < questions.length - 1) {
                    currentQuestionIndex++;
                    loadQuestion();
                } else {
                Map<String, Double> traitScores = computeTraitScores();
                String personalityType = getPersonalityType(traitScores);
                Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
                for (Map.Entry<String, Double> entry : traitScores.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
                intent.putExtra("PERSONALITY_TYPE", personalityType);

                // 👇 Send all answers
                intent.putExtra("ALL_ANSWERS", selectedAnswers);

                startActivity(intent);
                finish();
            }

        }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--;
                    loadQuestion();
                }
            }
        });
    }

    private void loadQuestion() {
        textViewQuestion.setText((currentQuestionIndex + 1) + ". " + questions[currentQuestionIndex]);
        radioGroupOptions.clearCheck();

        if (currentQuestionIndex < 10) {
            radioOption1.setText(oceanOptions[0]);
            radioOption2.setText(oceanOptions[1]);
            radioOption3.setText(oceanOptions[2]);
            radioOption4.setText(oceanOptions[3]);
            radioOption5.setText(oceanOptions[4]);
            radioOption5.setVisibility(View.VISIBLE);
        } else {
            radioOption1.setText(dassOptions[0]);
            radioOption2.setText(dassOptions[1]);
            radioOption3.setText(dassOptions[2]);
            radioOption4.setText(dassOptions[3]);
            radioOption5.setVisibility(View.GONE);
        }

        if (selectedAnswers[currentQuestionIndex] != -1) {
            RadioButton previouslySelected = (RadioButton) radioGroupOptions.getChildAt(selectedAnswers[currentQuestionIndex]);
            if (previouslySelected != null) {
                previouslySelected.setChecked(true);
            }
        }

        buttonBack.setEnabled(currentQuestionIndex > 0);
    }

    private Map<String, Double> computeTraitScores() {
        Map<String, Integer> sum = new HashMap<>();
        Map<String, Integer> count = new HashMap<>();

        for (int i = 0; i < questions.length; i++) {
            if (selectedAnswers[i] != -1) {
                int score;
                if (i < 10) {
                    score = selectedAnswers[i] + 1;
                    if (!isPositiveKeyed[i]) {
                        score = 6 - score;
                    }
                } else {
                    score = selectedAnswers[i]; // DASS: 0–3
                }

                String trait = traitLabels[i];
                sum.put(trait, sum.getOrDefault(trait, 0) + score);
                count.put(trait, count.getOrDefault(trait, 0) + 1);
            }
        }

        Map<String, Double> averages = new HashMap<>();
        for (String trait : sum.keySet()) {
            averages.put(trait, sum.get(trait) / (double) count.get(trait));
        }
        return averages;
    }






    private String getPersonalityType(Map<String, Double> scores) {
        StringBuilder type = new StringBuilder();
        if (scores.get("Extraversion") < 3.0) {
            type.append("Introvert");
        } else {
            type.append("Extrovert");
        }

        if (scores.get("Emotional Stability") >= 3.0) {
            type.append(" - Calm");
        } else {
            type.append(" - Reactive");
        }
        return type.toString();
    }
}
