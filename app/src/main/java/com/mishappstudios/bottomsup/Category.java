package com.mishappstudios.bottomsup;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import androidx.annotation.Keep;

/**
 * Class to represent a Category
 */
public class Category {
    // Private fields
    private String name;
    private String filePath;
    private ArrayList<Question> questions;
    private int cost;
    private int questionIndex;

    public int getQuestionIndex() {
        return questionIndex;
    }

    /**
     * Class constructor
     *
     * @param nameP     Category Name
     * @param filePathP Category's file path
     * @param costP     Cost associated
     */
    public Category(String nameP, String filePathP, int costP) {
        this.name = nameP;
        this.filePath = filePathP;
        this.cost = costP;
    }

    /**
     * Getter for name
     * @return name as a String
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for File Path
     * @return
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Getter for Question Set
     * @return
     */
    public ArrayList<Question> getQuestions() {
        return questions;
    }

    /**
     * Setter for questions
     * @param file
     */
    public void setQuestions(InputStream file) {
        this.questions = readQuestionsFromCSV(file);
    }

    /**
     * Getter for cost
     *
     * @return
     */
    public int getCost() {
        return cost;
    }

    /**
     * Reads Questions from an InputStream
     * @param file
     * @return
     */
    @Keep
    private ArrayList<Question> readQuestionsFromCSV(InputStream file) {
        String TAG = "CSVDEBUG";
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file, Charset.forName("UTF-8"))
        );
        Log.d(TAG, "readQuestionsFromCSV: opened file");
        String line;
        ArrayList<Question> questionsN = new ArrayList<>();
        try {

            Log.d(TAG, "readQuestionsFromCSV: entered try" + file.available());
            int lineNo = 0;
            // THIS DOES NOT WORK IN RELEASE MODE
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "readQuestionsFromCSV: entered while");
                String[] questionRaw = line.split(",");
                if (lineNo != 0) {
                    String content = questionRaw[0].substring(questionRaw[0].indexOf('-') + 1, questionRaw[0].length());
                    questionIndex = Integer.valueOf(questionRaw[0].substring(0, questionRaw[0].indexOf('-')));
                    Log.d("STRING", "readQuestionsFromCSV: iD" + questionIndex);
                    Log.d("STRING", "readQuestionsFromCSV: content" + content);
                    Question q = new Question(content);
                    questionsN.add(q);
                }
                lineNo++;
            }
            reader.close();
        } catch (Exception e) {
            Log.d("WHOOPS", "readQuestionsFromCSV: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return questionsN;
    }
}