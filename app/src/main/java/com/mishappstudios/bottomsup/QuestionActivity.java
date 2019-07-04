package com.mishappstudios.bottomsup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.mishappstudios.bottomsup.Constants.DIFFICULTY_ITEM;
import static com.mishappstudios.bottomsup.Constants.ENDED_A_GAME_EVENT;

/**
 * Class for representing a question Activity
 */
public class QuestionActivity extends ImmersiveSlidingAppCompatActivity {
    // Private fields
    private ArrayList remainingQuestions = null;
    private Question thisQuestion;
    private ArrayList<Question> questionQueue;
    private ArrayList<String> players = new ArrayList<>();
    private int maxDrinks = 3; // Bhav don't increase this you alchie
    private String difficulty = null;
    private View.OnClickListener endOfQuestions = view -> finish();
    private int questionTotal;
    private View.OnClickListener nextQuestionChainer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
            Intent i = new Intent(getApplicationContext(), QuestionActivity.class);
            i.putExtra("difficulty", difficulty);
            i.putExtra("questionQueue", remainingQuestions);
            i.putExtra("players", players.toArray(new String[players.size()]));
            i.putExtra("questionCount", questionTotal);
            startActivity(i);
        }
    };
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] arrplayers = getIntent().getStringArrayExtra("players");
        difficulty = getIntent().getStringExtra("difficulty");
        setContentView(R.layout.activity_question);
        questionQueue = (ArrayList) getIntent().getSerializableExtra("questionQueue");
        Collections.addAll(players, arrplayers);
        ConstraintLayout questionWall = findViewById(R.id.questionWall);
        TextView questionText = findViewById(R.id.questionText);
        TextView penaltyText = findViewById(R.id.penaltyText);
        int[] materialColors = getResources().getIntArray(R.array.materialColors);
        int randomMaterialColor = materialColors[new Random().nextInt(materialColors.length)];
        questionWall.setBackgroundColor(randomMaterialColor);
        TextView questionCount = findViewById(R.id.questionIndicator);
        if (questionQueue != null && questionQueue.size() > 0) {
            thisQuestion = questionQueue.remove(0);
            remainingQuestions = questionQueue;

            questionText.setText(getQuestion(players, thisQuestion));
            penaltyText.setText(getPenalty());
            questionTotal = getIntent().getIntExtra("questionCount", 0);
            int currentQuestionNumber = questionTotal - remainingQuestions.size();
            questionCount.setText(getString(R.string.questionIndicator, currentQuestionNumber, questionTotal));
            //questionWall.setOnClickListener(nextQuestionChainer);
            questionText.setOnClickListener(nextQuestionChainer);

        } else {
            //questionWall.setOnClickListener(endOfQuestions);
            questionText.setText(R.string.end_of__questions);
            questionText.setOnClickListener(endOfQuestions);
            questionCount.setText(R.string.end_of_questions_hint);
            penaltyText.setText(R.string.end_of_questions_hint);
            Intent i = new Intent(this, FeedbackActivity.class);
            startActivity(i);
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString(DIFFICULTY_ITEM, difficulty);
            mFirebaseAnalytics.logEvent(ENDED_A_GAME_EVENT, bundle);
        }
    }

    private String getQuestion(ArrayList<String> passedPlayers, Question currentQuestion) {
        double randomNo = Math.random();
        if (randomNo < 0.25) {
            // X and Y take it in turns to ...
            ArrayList<String> twoPlayers = getTwoPlayerIndexes(passedPlayers);
            return (twoPlayers.get(0) + " and " + twoPlayers.get(1) + " take it in turns to " + currentQuestion.getqContent() + ".");
        } else {
            // Starting from X, go clockwise to ...
            Random rand = new Random();
            int  n = rand.nextInt(1);
            if(n == 0) {
                return ("Starting from " + getSinglePlayer(passedPlayers) + ", go clockwise and " + currentQuestion.getqContent() + ".");
            }else{
                return ("Starting from " + getSinglePlayer(passedPlayers) + ", go anticlockwise and " + currentQuestion.getqContent() + ".");
            }
        }
    }

    private String getPenalty(){
        int drinkNo = getDrinkNo();
        if(drinkNo == 1){
            return("Loser has to drink " + drinkNo + " time.");
        }else{
            return("Loser has to drink " + drinkNo + " times.");
        }
    }

    private int getDrinkNo() {
        Random r = new Random();
        switch (difficulty) {
            case "Easy":
                return ((r.nextInt(maxDrinks) + 1));
            case "Medium":
                return (int) Math.floor((r.nextInt(maxDrinks) + 1) * 1.5);
            case "Hard":
                return (int) Math.ceil((r.nextInt(maxDrinks) + 1) * 1.5);
            default:
                return 1;

        }
    }

    private String getSinglePlayer(ArrayList<String> passedPlayers) {
        ArrayList<String> pPlayers = new ArrayList<>(passedPlayers);
        Random rand = new Random();
        int randomIndex = rand.nextInt(pPlayers.size());
        return pPlayers.get(randomIndex);
    }

    private ArrayList<String> getTwoPlayerIndexes(ArrayList<String> passedPlayers) {
        ArrayList<String> pPlayers = new ArrayList<>(passedPlayers);
        Random rand = new Random();
        ArrayList<String> returnedPlayers = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int randomIndex = rand.nextInt(pPlayers.size());
            returnedPlayers.add(pPlayers.get(randomIndex));
            pPlayers.remove(randomIndex);
        }
        return returnedPlayers;
    }
}
