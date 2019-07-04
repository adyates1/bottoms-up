package com.mishappstudios.bottomsup;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import static com.mishappstudios.bottomsup.Constants.CATEGORIES_SELECTED_ITEM;
import static com.mishappstudios.bottomsup.Constants.NUMBER_OF_PLAYERS_ITEM;
import static com.mishappstudios.bottomsup.Constants.STARTED_A_GAME_EVENT;
import static com.mishappstudios.bottomsup.Constants.UNIMPLEMENTED;

/**
 * Class to represent the player list Activity
 */
public class ListPlayersActivity extends ImmersiveSlidingAppCompatActivity {
    // Private fields
    private ListView playerList = null;
    private ArrayList playerListStrings = new ArrayList();
    private ArrayList<Question> addedQuestions = new ArrayList<>();
    private FirebaseAnalytics mFirebaseAnalytics;
    private ArrayAdapter<String> stringArrayAdapter;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_players);
        //setupUI(findViewById(R.id.layout));
        playerList = findViewById(R.id.playerListLinearLayout);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.lightbounce);
        findViewById(R.id.chooseCategoriesButton).startAnimation(bounce);
        addedQuestions = (ArrayList<Question>) getIntent().getSerializableExtra("allQuestions");
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        stringArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.name_layout, playerListStrings);
        playerList.setAdapter(stringArrayAdapter);

        playerList.setOnItemClickListener((adapterView, view, i, l) -> {
            TextView tv = (TextView) view;
            playerListStrings.remove(tv.getText().toString());
            stringArrayAdapter.remove(tv.getText().toString());
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Resources r = getResources();
        Toast toast = Toast.makeText(this, "You can tap on \n a player's name \n to remove them", Toast.LENGTH_SHORT);
        toast.getView().setBackgroundResource(R.drawable.custom_toast);
        TextView tv = toast.getView().findViewById(android.R.id.message);
        tv.setGravity(Gravity.CENTER);
        int dpT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
        tv.setTextSize(dpT);
        toast.setGravity(Gravity.LEFT | Gravity.START, 50, 10);
        toast.show();

    }

    public void addPlayer(View view) {
        TextInputLayout nameInputTextInputLayout = findViewById(R.id.nameInputTextInputLayout);
        TextInputEditText nameInputTextInputEditText = findViewById(R.id.nameInputTextInputEditText);
        final String userInputText = nameInputTextInputEditText.getText().toString().toUpperCase();
        nameInputTextInputEditText.setText("");
        // Test for no input
        if (userInputText.equals("")) {
            nameInputTextInputLayout.setError(getString(R.string.empty_name_error));
        } else if (playerListStrings.contains(userInputText)) { // Can't have identical players
            nameInputTextInputLayout.setError(getString(R.string.name_already_added_error));
        } else {

            nameInputTextInputLayout.setError(null);
            // Create a new item in the list
            final TextView newListItem = new TextView(getApplicationContext());
            newListItem.setText(userInputText);
            newListItem.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.bungee));
            newListItem.setGravity(Gravity.CENTER);
            // name deletion
            newListItem.setOnClickListener(view1 -> {
                playerList.removeView(newListItem);
                playerListStrings.remove(userInputText);
            });

            // Add them in
            playerListStrings.add(userInputText);
            stringArrayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Called when the user confirms which players to use
     *
     * @param view
     */
    public void confirmationClick(View view) {
        if (playerListStrings.size() > 1) {
            Bundle bundle = new Bundle();
            bundle.putString(NUMBER_OF_PLAYERS_ITEM, String.valueOf(playerListStrings.size()));
            bundle.putString(CATEGORIES_SELECTED_ITEM, UNIMPLEMENTED);

            mFirebaseAnalytics.logEvent(STARTED_A_GAME_EVENT, bundle);

            Intent i = new Intent(this, SelectCategoriesActivity.class);
            i.putExtra("Difficulty", getIntent().getStringExtra("Difficulty"));
            i.putExtra("players", playerListStrings.toArray(new String[playerListStrings.size()]));
            startActivity(i);
            finish();

        } else {
            TextInputLayout til = findViewById(R.id.nameInputTextInputLayout);
            til.setError("You need to add at least 2 players");
        }

    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(ListPlayersActivity.this);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}
