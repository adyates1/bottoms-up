package com.mishappstudios.bottomsup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import static com.mishappstudios.bottomsup.Constants.COINS_PURCHASE_INITIATED;
import static com.mishappstudios.bottomsup.Constants.SKU_110_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_50_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_600_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_TO_BUY;

public class SelectCategoriesActivity extends ImmersiveSlidingAppCompatActivity implements PurchasesUpdatedListener {
    private String difficulty = null;
    private ArrayList<Category> allCategories;
    private Theme allCategoriesTheme;
    private ArrayList<Category> addedCategories = new ArrayList<>();
    BillingClient billingClient;
    boolean keepBillingAlive = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == COINS_PURCHASE_INITIATED) {
            keepBillingAlive = true;
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSku(data.getStringExtra(SKU_TO_BUY))
                    .setType(BillingClient.SkuType.INAPP) // SkuType.SUB for subscription
                    .build();
            int responseCode = billingClient.launchBillingFlow(this, flowParams);
        }
    }

    private void addFreeCategories(Context c) {
        if (BottomsUpStorageHelper.getUnlockedCategories(c).isEmpty()) {
            for (Category cat : allCategories) {
                if (cat.getCost() == 0) {
                    BottomsUpStorageHelper.addUnlockedCategory(c, cat.getName());
                }
            }
        }
    }

    private void updateCoinsValue() {
        Button getCoins = findViewById(R.id.sel_cat_coins_button);
        if (getCoins == null) {
            return;
        }
        int myCoins = BottomsUpStorageHelper.getCoins(this);
        getCoins.setText(String.valueOf(myCoins));
    }

    private void populateCategoryButtons(int rows, int columns) {

        LinearLayout table = findViewById(R.id.verticalButtonGridLinearLayout);
        for (int row = 0; row < rows; row++) {
            LinearLayout tableRow = new LinearLayout(this);
            tableRow.setOrientation(LinearLayout.HORIZONTAL);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            table.addView(tableRow);
            for (int col = 0; col < columns; col++) {
                try {
                    final int currentIndex = (row * columns) + col;
                    int buttonCost = allCategories.get(currentIndex).getCost();
                    LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final LinearLayout v = (LinearLayout) li.inflate(R.layout.category_button, tableRow, false);
                    tableRow.addView(v);
                    LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    v.setLayoutParams(newParams);
                    v.setWeightSum(1);
                    final TextView catName = v.findViewById(R.id.category_name_label);
                    catName.setText(String.valueOf(allCategories.get(currentIndex).getName()));
                    catName.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.bungee_inline));
                    catName.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black_overlay, null));
                    TextView catPrice = v.findViewById(R.id.category_price_label);
                    catPrice.setText(String.valueOf(buttonCost));
                    catPrice.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.bungee_inline));
                    catPrice.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black_overlay, null));

                    String categoryName = allCategories.get(currentIndex).getName();
                    boolean found = false;
                    for (String name : BottomsUpStorageHelper.getUnlockedCategories(this)) {

                        if (name.equals(categoryName)) {
                            Log.d("Name: ", categoryName);
                            found = true;
                        }
                        if (found) {
                            v.findViewById(R.id.category_price_layout).setVisibility(View.GONE);
                        }
                    }
                    if (!found) {
                        if (buttonCost > 0) {
                            catName.setTextColor(ResourcesCompat.getColor(getResources(), R.color.gold, null));
                            catPrice.setTextColor(ResourcesCompat.getColor(getResources(), R.color.gold, null));
                        }
                    }
                    v.setOnClickListener(view -> {

                        Category cat = allCategories.get(currentIndex);

                        for (String name : BottomsUpStorageHelper.getUnlockedCategories(SelectCategoriesActivity.this)) {
                            if (name.equals(cat.getName())) {
                                if (!addedCategories.contains(cat)) {
                                    catName.setTextColor(Color.parseColor("#7cff68"));
                                    categoryButtonClicked(cat);
                                    return;
                                } else {
                                    catName.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black_overlay, null));
                                    addedCategories.remove(cat);
                                    return;
                                }
                            }
                        }

                        attemptPurchase(cat, view);

                    });
                } catch (Exception e) {
                    Log.d("CategoriesError", e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }

        final ScrollView sv = findViewById(R.id.buttonGridScrollView);
        sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
        sv.postDelayed(() -> sv.fullScroll(View.FOCUS_UP), 1000);
    }

    private void attemptPurchase(final Category cat, final View view) {
        final int coins = BottomsUpStorageHelper.getCoins(this);
        final String categoryText = cat.getName();
        final int valueOfCategory = cat.getCost();
        if (coins >= valueOfCategory) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            BottomsUpStorageHelper.setCoins(SelectCategoriesActivity.this, (coins - valueOfCategory));
                            BottomsUpStorageHelper.addUnlockedCategory(SelectCategoriesActivity.this, cat.getName());
                            updateCoinsValue();
                            view.findViewById(R.id.category_price_layout).setVisibility(View.GONE);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                Animator anim =
                                        ViewAnimationUtils.createCircularReveal(view, view.getWidth() / 2, view.getHeight() / 2, 0, Math.max(view.getWidth(), view.getHeight()));
                                anim.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        view.setBackgroundColor(Color.TRANSPARENT);
                                        view.callOnClick();
                                    }

                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        super.onAnimationStart(animation);
                                        view.setBackgroundColor(Color.argb(25, 255, 215, 0));

                                    }
                                });

                                anim.start();
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you'd like to buy " + categoryText).setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } else {
            Toast.makeText(this, "You don't have enough coins to purchase " + categoryText + "!", Toast.LENGTH_LONG).show();
        }
    }

    private void categoryButtonClicked(Category cat) {
        addedCategories.add(cat);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_categories);
        Toast.makeText(this, "Suggestion: use landscape mode when selecting categories", Toast.LENGTH_SHORT).show();
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.lightbounce);
        findViewById(R.id.startNowButton).startAnimation(bounce);
        difficulty = getIntent().getStringExtra("Difficulty");
        allCategoriesTheme = new Theme("allCategoriesTheme");
        InputStream is = getResources().openRawResource(R.raw.themes);
        allCategoriesTheme.setCategories(is);
        allCategories = allCategoriesTheme.getCategories();
        addFreeCategories(this);
        int columns = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 3;
        int rows = (int) Math.ceil(allCategories.size() / (float) columns);
        populateCategoryButtons(rows, columns);
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(0);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        Button getCoins = findViewById(R.id.sel_cat_coins_button);
        int myCoins = BottomsUpStorageHelper.getCoins(this);
        getCoins.setText(String.valueOf(myCoins));
        getCoins.setOnClickListener(view -> {
            keepBillingAlive = true;
            Intent i = new Intent(getApplicationContext(), PurchaseCoinsActivity.class);
            startActivityForResult(i, COINS_PURCHASE_INITIATED);
        });
    }

    @Keep
    public void playerListClicked(View view) {
        if (addedCategories.size() > 0) {
            if (difficulty.equals("Easy")) {
                if (addedCategories.size() >= 20) {
                    Toast.makeText(this, "You've added too many categories!", Toast.LENGTH_SHORT).show();
                } else if (addedCategories.size() > 1) {
                    launchPlayerList(view);
                } else {
                    Toast.makeText(this, "Please add at-least 2 categories!", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (addedCategories.size() >= 25) {
                    Toast.makeText(this, "You've added too many categories!", Toast.LENGTH_SHORT).show();
                } else if (addedCategories.size() > 2) {
                    launchPlayerList(view);
                } else {
                    Toast.makeText(this, "Please add at-least 3 categories!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Please add some categories!", Toast.LENGTH_SHORT).show();
        }
    }

    @Keep
    public void launchPlayerList(View view) {
        if (addedCategories.size() < 2) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, QuestionActivity.class);
        ArrayList<Question> questions = new ArrayList<>();
        int totalQuestions = 30;
        switch (difficulty) {
            case "Easy":
                totalQuestions = 20;
                break;
            case "Medium":
                totalQuestions = 25;
                break;
            case "Hard":
                totalQuestions = 30;
                break;
        }
        int questionsPerCategory = Math.round(totalQuestions / addedCategories.size());
        int remainder = totalQuestions % addedCategories.size();
        boolean remainderAdded = false;
        for (Category cat : addedCategories) {
            String fileName = cat.getFilePath().substring(0, cat.getFilePath().lastIndexOf('.'));
            Log.d("DEBUGD", "launchPlayerList: filename " + fileName);
            Log.d("REE", "launchPlayerList: ");
            InputStream is = getResources().openRawResource(getResources().getIdentifier(fileName, "raw", getPackageName()));
            cat.setQuestions(is);
            ArrayList<Question> potentialQuestions = cat.getQuestions();
            Log.d("MATH", "launchPlayerList: " + potentialQuestions.size());
            ArrayList<Integer> indexesAdded = new ArrayList<>();
            for (int i = 0; i < questionsPerCategory; i++) {
                Log.d("YOO", "launchPlayerList: Entered for");
                int randomIndex = new Random().nextInt(potentialQuestions.size());
                while (indexesAdded.contains(randomIndex)) {
                    randomIndex = new Random().nextInt(potentialQuestions.size());
                }
                questions.add(potentialQuestions.get(randomIndex));
                indexesAdded.add(randomIndex);
                if (!remainderAdded) {
                    for (int q = 0; q < remainder; q++) {
                        while (indexesAdded.contains(randomIndex)) {
                            randomIndex = new Random().nextInt(potentialQuestions.size());
                        }
                        questions.add(potentialQuestions.get(randomIndex));
                        indexesAdded.add(randomIndex);
                    }
                    remainderAdded = true;
                }
            }
        }

        Collections.shuffle(questions);
        intent.putExtra("questionQueue", questions);
        intent.putExtra("questionCount", questions.size());
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("players", getIntent().getStringArrayExtra("players"));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (billingClient == null || !billingClient.isReady()) {
            billingClient = BillingClient.newBuilder(this).setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(int responseCode) {
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        // The billing client is ready. You can query purchases here.
                        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, (responseCode1, purchasesList) -> processAndProvisionCoinsIfNecessary(responseCode1, purchasesList));
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {

                }
            });
        } else {
            keepBillingAlive = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!keepBillingAlive) {
            billingClient.endConnection();
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        processAndProvisionCoinsIfNecessary(responseCode, purchases);
    }

    public void processAndProvisionCoinsIfNecessary(int responseCode, List<Purchase> purchasesList) {
        if (purchasesList != null) {
            final int currentCoins = BottomsUpStorageHelper.getCoins(getApplicationContext());
            // then there are some purchases that need to be handled
            for (final Purchase purchase : purchasesList) {
                billingClient.consumeAsync(purchase.getPurchaseToken(), (responseCode1, purchaseToken) -> {
                    if (responseCode1 == BillingClient.BillingResponse.OK) {
                        switch (purchase.getSku()) {
                            case SKU_50_COINS:
                                BottomsUpStorageHelper.setCoins(getApplicationContext(), currentCoins + 50);
                                break;
                            case SKU_110_COINS:
                                BottomsUpStorageHelper.setCoins(getApplicationContext(), currentCoins + 110);
                                break;
                            case SKU_600_COINS:
                                BottomsUpStorageHelper.setCoins(getApplicationContext(), currentCoins + 600);
                                break;
                            default:
                                Log.d("ERROR", "onPurchasesUpdated: Invalid Purchase SKU");
                        }
                        updateCoinsValue();
                    }
                });
            }
        }
    }
}