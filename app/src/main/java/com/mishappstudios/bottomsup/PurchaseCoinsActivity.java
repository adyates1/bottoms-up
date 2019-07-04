package com.mishappstudios.bottomsup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;

import static com.mishappstudios.bottomsup.Constants.COINS_PURCHASE_INITIATED;
import static com.mishappstudios.bottomsup.Constants.SKU_110_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_50_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_600_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_TO_BUY;

/**
 * Class to represent the Coins purchasing Modal
 */
public class PurchaseCoinsActivity extends ImmersiveSlidingAppCompatActivity implements RewardedVideoAdListener {
    // Private fields
    private String currentSKU = "";
    private BillingClient billingClient;
    private RewardedVideoAd mRewardedVideoAd;

    /**
     * Called when PurchaseCoinsActivity is started
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Code below updates prices from Google Play in local currency
        final RadioButton buttonOne, buttonTwo, buttonThree;
        buttonOne = findViewById(R.id._50CoinsRadioButton);
        buttonTwo = findViewById(R.id._110CoinsRadioButton);
        buttonThree = findViewById(R.id._600CoinsRadioButton);
        billingClient = BillingClient.newBuilder(this).setListener((responseCode, purchases) -> {
        }).build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, (responseCode12, purchasesList) -> {
                        ArrayList<String> skuStrings = new ArrayList<>();
                        skuStrings.add(SKU_50_COINS);
                        skuStrings.add(SKU_110_COINS);
                        skuStrings.add(SKU_600_COINS);
                        SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(skuStrings).setType(BillingClient.SkuType.INAPP).build();
                        billingClient.querySkuDetailsAsync(params, (responseCode1, skuDetailsList) -> {
                            for (SkuDetails skud : skuDetailsList) {
                                Log.d("pricefound", "onSkuDetailsResponse: " + skud.getPrice());
                                switch (skud.getSku()) {
                                    case SKU_50_COINS:
                                        buttonOne.setText(buttonOne.getText() + "- " + skud.getPrice() + " ");
                                        break;
                                    case SKU_110_COINS:
                                        buttonTwo.setText(buttonTwo.getText() + "- " + skud.getPrice() + " ");
                                        break;
                                    case SKU_600_COINS:
                                        buttonThree.setText(buttonThree.getText() + "- " + skud.getPrice() + " ");
                                        break;
                                }
                            }
                        });
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    /**
     * Called when the PurchaseCoinsActivity is created
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coins_modal);
        RadioGroup radioGroup = findViewById(R.id.coinsSelector);
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            switch (i) {
                case R.id._50CoinsRadioButton:
                    currentSKU = Constants.SKU_50_COINS;
                    break;
                case R.id._110CoinsRadioButton:
                    currentSKU = Constants.SKU_110_COINS;
                    break;
                case R.id._600CoinsRadioButton:
                    currentSKU = Constants.SKU_600_COINS;
                    break;
            }
        });
        // Use an activity context to get the rewarded video instance.
        MobileAds.initialize(this, "ca-app-pub-4721362007363130~5150595342");

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        mRewardedVideoAd.loadAd("ca-app-pub-4721362007363130/5400180237", new AdRequest.Builder().build());

    }

    /**
     * Sends the bought SKU back the calling Activity
     *
     * @param view
     */
    public void buyTheCoins(View view) {
        Intent databackIntent = new Intent();
        databackIntent.putExtra(SKU_TO_BUY, currentSKU);
        setResult(COINS_PURCHASE_INITIATED, databackIntent);
        finish();
    }

    public void launchVideoAd(View view) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.rewarded_ad_hint))
                .setPositiveButton("Awesome!", (dialogInterface, i) -> {
                    if (mRewardedVideoAd.isLoaded()) mRewardedVideoAd.show();
                }).setNegativeButton("No Thanks", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().addTestDevice("705D8248C67C80BE183094AE1923809D").build());
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        int amount = rewardItem.getAmount();
        BottomsUpStorageHelper.setCoins(this, BottomsUpStorageHelper.getCoins(this) + amount);
        Toast.makeText(this, "Reward applied!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }
}