package com.mishappstudios.bottomsup;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotContents;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import static com.mishappstudios.bottomsup.Constants.BOTTOMS_UP_SAVE_NAME;
import static com.mishappstudios.bottomsup.Constants.COINS_PURCHASE_INITIATED;
import static com.mishappstudios.bottomsup.Constants.LAUNCH_PROMO_KEY;
import static com.mishappstudios.bottomsup.Constants.SKU_110_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_50_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_600_COINS;
import static com.mishappstudios.bottomsup.Constants.SKU_TO_BUY;
import static com.mishappstudios.bottomsup.Constants.SNAPSHOT_NOT_CREATED;

public class MainActivity extends ImmersiveSlidingAppCompatActivity implements PurchasesUpdatedListener {
    private static final int CLOUD_OFF = 1;
    private static final int CLOUD_UPLOAD = 2;
    private static final int CLOUD_DONE = 3;
    private BillingClient billingClient;
    private boolean keepBillingAlive = false;
    private static final int RC_SIGN_IN = 20;
    private static final int RC_SAVED_GAMES = 9009;
    private String TAG = "BottomsUpMain";
    private GoogleSignInClient mGoogleSignInClient;
    private String mCurrentSaveName = SNAPSHOT_NOT_CREATED;
    private ImageView cloud_save_icon;
    private FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    private SnapshotsClient snapshotsClient;

    /*
        Android Activity Lifecycle Management
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Animation lbounce = AnimationUtils.loadAnimation(this, R.anim.lightbounce);
        Animation mbounce = AnimationUtils.loadAnimation(this, R.anim.medbounce);
        Animation hbounce = AnimationUtils.loadAnimation(this, R.anim.hardbouncer);
        findViewById(R.id.easyBtn).startAnimation(lbounce);
        findViewById(R.id.mediumBtn).startAnimation(mbounce);
        findViewById(R.id.hardBtn).startAnimation(hbounce);
        // Firebase RemoteConfig
        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().build());
        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        Task<Void> remoteConfigFetchTask = remoteConfig.fetch(0);
        remoteConfigFetchTask.addOnSuccessListener(aVoid -> remoteConfig.activateFetched());

        // Google Play Games Sign-in Client
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        // Since we are using SavedGames, we need to add the SCOPE_APPFOLDER to access Google Drive.
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build());

        // Background animation
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        // Updates the Coins Amount
        updateCoinsValue();

        // If the app has been instructed to provision coins, give them here.
        Bundle fcmBundle = getIntent().getExtras();
        if (fcmBundle != null && fcmBundle.containsKey("coinsToGive")) {
            Log.v("ICR COINS", "Increased coins");
            BottomsUpStorageHelper.setCoins(this, BottomsUpStorageHelper.getCoins(this) + Integer.valueOf(fcmBundle.getString("coinsToGive")));
        }
        cloud_save_icon = findViewById(R.id.cloudSaveButton);
        if (BuildConfig.DEBUG) {
            // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
            MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        } else {
            MobileAds.initialize(this, "ca-app-pub-4721362007363130~5150595342");


        }
    }

    @Override
    protected void onResume() {
        signInSilently();
        super.onResume();
        if (billingClient == null || !billingClient.isReady()) {
            billingClient = BillingClient.newBuilder(this).setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(int responseCode) {
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        // The billing client is ready. You can query purchases here.
                        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP,
                                (responseCode1, purchasesList) ->
                                        processAndProvisionCoinsIfNecessary(responseCode1, purchasesList));
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
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (resultCode == COINS_PURCHASE_INITIATED) {
            keepBillingAlive = true;
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSku(intent.getStringExtra(SKU_TO_BUY))
                    .setType(BillingClient.SkuType.INAPP) // SkuType.SUB for subscription
                    .build();
            int responseCode = billingClient.launchBillingFlow(this, flowParams);
        }
        if (intent != null) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                snapshotsClient = Games.getSnapshotsClient(this, GoogleSignIn.getLastSignedInAccount(this));
            }
            if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                // Load a snapshot.
                SnapshotMetadata snapshotMetadata =
                        intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
                mCurrentSaveName = snapshotMetadata.getUniqueName();
                BottomsUpStorageHelper.setSnapshotUID(getApplicationContext(), BOTTOMS_UP_SAVE_NAME);
                // Load the game data from the Snapshot
                loadDataFromSnapshot();

            } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                // Create a new snapshot named with a unique string
                mCurrentSaveName = BOTTOMS_UP_SAVE_NAME;
                BottomsUpStorageHelper.setSnapshotUID(this, mCurrentSaveName);
                // Create a new snapshot named with a unique string
                snapshotsClient.open(mCurrentSaveName, true);

                Task<SnapshotsClient.DataOrConflict<Snapshot>> task = snapshotsClient.open(mCurrentSaveName, true);
                task.addOnCompleteListener(task1 -> {
                    if (task1.getResult().isConflict()) {
                        resolveSnapshotConflicts(task1, snapshotsClient);
                    } else {
                        createAndWriteNewSaveGame(task1);
                    }
                });
            }
        }
        // Handle Google sign in
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Everything looks good, open the saves dialog
                Log.d(TAG, "onActivityResult: showed savegames from under account");
                showSavedGamesUI();
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.google_sign_in_error);
                }
                new AlertDialog.Builder(this)
                        .setMessage("Google Sign in Error: " + message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    /*
        onClick Listeners
     */
    public void easyButtonClicked(View view) {
        Intent intent = new Intent(this, ListPlayersActivity.class);
        intent.putExtra("Difficulty", "Easy");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

    }

    public void mediumButtonClicked(View view) {
        Intent intent = new Intent(this, ListPlayersActivity.class);
        intent.putExtra("Difficulty", "Medium");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void hardButtonClicked(View view) {
        Intent intent = new Intent(this, ListPlayersActivity.class);
        intent.putExtra("Difficulty", "Hard");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void infoButtonClicked(View view) {
        Intent i = new Intent(this, AboutBottomsUpActivity.class);
        keepBillingAlive = true;
        startActivity(i);
    }

    public void feedbackButtonClicked(View view) {
        Intent i = new Intent(this, FeedbackActivity.class);
        keepBillingAlive = true;
        startActivity(i);
    }

    public void startSignInIntent(View view) {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    public void getCoinsButtonClicked(View view) {
        keepBillingAlive = true;
        Intent i = new Intent(getApplicationContext(), PurchaseCoinsActivity.class);
        startActivityForResult(i, COINS_PURCHASE_INITIATED);
    }

    /*
        IAP methods
     */

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

    private void updateCoinsValue() {
        Button getCoins = findViewById(R.id.getCoinsButton);
        if (getCoins == null) {
            return;
        }
        int myCoins = BottomsUpStorageHelper.getCoins(this);
        getCoins.setText(String.valueOf(myCoins));
    }

    /*
        Google Play Games Snapshot Functionality
    */

    /**
     * If the user has previously authorised Bottoms Up!
     * with Google Play Games Services then sign them
     * in silently
     */
    private void signInSilently() {
        GoogleSignInOptions signInOption =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        // Add the APPFOLDER scope for Snapshot support.
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOption);
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                snapshotsClient = Games.getSnapshotsClient(this, GoogleSignIn.getLastSignedInAccount(this));
                // Check if we can make saves
                String SavedSnapshotSaveName = BottomsUpStorageHelper.getSnapshotUID(getApplicationContext());
                if (SavedSnapshotSaveName.equals(SNAPSHOT_NOT_CREATED)) {
                    // If not then make sure this is reflected in the UI
                    Log.d(TAG, "signInSilently: showed save games from new creator");
                    showSavedGamesUI();
                    setCloudSavesIconState(CLOUD_OFF, false);
                } else if (SavedSnapshotSaveName.equals(BOTTOMS_UP_SAVE_NAME)) {
                    // If we can, then update the UI to reflect that we're updating
                    setCloudSavesIconState(CLOUD_UPLOAD, false);
                    mCurrentSaveName = SavedSnapshotSaveName;
                    snapshotsClient.open(mCurrentSaveName, true);
                    Task<SnapshotsClient.DataOrConflict<Snapshot>> saveTask = snapshotsClient.open(mCurrentSaveName, true);
                    saveTask.addOnCompleteListener(task1 -> {
                        if (task1.getResult().isConflict()) {
                            resolveSnapshotConflicts(task1, snapshotsClient);
                        } else {
                            createAndWriteNewSaveGame(task1);
                        }
                    });
                } else {
                    showSavedGamesUI();
                    setCloudSavesIconState(CLOUD_OFF, false);
                }
            }
        }).addOnFailureListener(e -> {
            // Player will need to sign-in explicitly using via UI
            Resources r = getResources();
            setCloudSavesIconState(CLOUD_OFF, true);
            String s = "Don't lose your coins or unlocks! Enable Cloud Saves by tapping the cloud above";
            Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
            toast.getView().setBackgroundResource(R.drawable.custom_toast);
            TextView tv = toast.getView().findViewById(android.R.id.message);
            int dpW = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170, r.getDisplayMetrics());
            tv.setWidth(dpW);
            tv.setGravity(Gravity.CENTER);
            int dpT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
            tv.setTextSize(dpT);
            int dp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
            int dp105 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
            toast.setGravity(Gravity.START | Gravity.LEFT, dp8, dp105);
            toast.show();
            e.printStackTrace();
        });
    }

    /**
     * Opens a Save Game Dialog. The app is configured in
     * such a way that there should ONLY ever be one save.
     */
    private void showSavedGamesUI() {
        int maxNumberOfSavedGamesToShow = 5;

        Task<Intent> intentTask = snapshotsClient.getSelectSnapshotIntent(
                getString(R.string.saves_dialog_title), true, true, maxNumberOfSavedGamesToShow);

        intentTask.addOnSuccessListener(intent -> startActivityForResult(intent, RC_SAVED_GAMES)).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_LONG).show());
    }

    /**
     * Writes a Snapshot and commits it for saving
     *
     * @param snapshot   The Snapshot to write to
     * @param data       Raw payload
     * @param coverImage A cover image
     * @param desc       A small description
     * @return The task
     */
    private Task<SnapshotMetadata> writeSnapshot(Snapshot snapshot, byte[] data, Bitmap coverImage, String desc) {
        // Set the data payload for the snapshot
        snapshot.getSnapshotContents().writeBytes(data);
        // Create the change operation
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setCoverImage(coverImage)
                .setDescription(desc)
                .build();
        SnapshotsClient snapshotsClient =
                Games.getSnapshotsClient(this, GoogleSignIn.getLastSignedInAccount(this));
        // Commit the operation
        return snapshotsClient.commitAndClose(snapshot, metadataChange).addOnFailureListener(e -> {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ERROR while writing snapshot", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Loads a Snapshot from mCurrentSaveName
     *
     * @return finished task
     */
    Task<byte[]> loadSnapshot() {
        // Get the SnapshotsClient from the signed in account.
        SnapshotsClient snapshotsClient =
                Games.getSnapshotsClient(this, GoogleSignIn.getLastSignedInAccount(this));

        // In the case of a conflict, the most recently modified version of this snapshot will be used.
        int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;

        // Open the saved game using its name.
        return snapshotsClient.open(mCurrentSaveName, true, conflictResolutionPolicy).addOnFailureListener(e -> {
            Log.e(TAG, "Error while opening Snapshot.", e);
            e.printStackTrace();
        }).continueWith(task -> {
            Snapshot snapshot = task.getResult().getData();
            // Opening the snapshot was a success and any conflicts have been resolved.
            try {
                // Extract the raw data from the snapshot.
                BottomsUpSaveGame saveGame = new BottomsUpSaveGame(snapshot.getSnapshotContents().readFully());
                BottomsUpStorageHelper.setCoins(getApplicationContext(), saveGame.getCoins());
                for (String s : saveGame.getUnlockedCategories()) {
                    BottomsUpStorageHelper.addUnlockedCategory(getApplicationContext(), s);
                }
                return snapshot.getSnapshotContents().readFully();
            } catch (IOException e) {
                Log.e(TAG, "Error while reading Snapshot.", e);
                e.printStackTrace();
            }
            return null;
        });
    }

    private void setCloudSavesIconState(int i, boolean sign_in_needed) {
        try {
            int buttonsTintColor = ResourcesCompat.getColor(getResources(), R.color.icons_tint_grey, null);
            Drawable state = null;
            switch (i) {
                case CLOUD_OFF:
                    state = DrawableCompat.wrap(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cloud_off, null));
                    break;
                case CLOUD_UPLOAD:
                    state = DrawableCompat.wrap(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cloud_upload, null));
                    break;
                case CLOUD_DONE:
                    state = DrawableCompat.wrap(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cloud_done, null));
                    break;
            }
            DrawableCompat.setTint(state, buttonsTintColor);
            //cloud_save_icon.setImageDrawable(state);
            cloudSaveNewIconAnimation(this, cloud_save_icon, state);
            if (sign_in_needed) {
                cloud_save_icon.setOnClickListener((View v) -> {
                    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
                });
            } else {
                cloud_save_icon.setOnClickListener(null);
            }
        } catch (Exception npe) {
            npe.printStackTrace();
        }
    }

    private void cloudSaveNewIconAnimation(Context c, final ImageView v, final Drawable new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageDrawable(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    private void loadDataFromSnapshot() {
        loadSnapshot().addOnSuccessListener(bytes -> {
            BottomsUpSaveGame saveGame = new BottomsUpSaveGame(bytes);
            saveGame.unloadSave(getApplicationContext());
        }).addOnSuccessListener(bytes -> {
            setCloudSavesIconState(CLOUD_DONE, false);
            updateCoinsValue();
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void resolveSnapshotConflicts
            (Task<SnapshotsClient.DataOrConflict<Snapshot>> task1, SnapshotsClient snapshotsClient) {
        Snapshot s1 = task1.getResult().getConflict().getConflictingSnapshot();
        Snapshot s2 = task1.getResult().getConflict().getSnapshot();
        SnapshotContents sc = task1.getResult().getConflict().getResolutionSnapshotContents();
        BottomsUpSaveGame newSave;
        try {
            newSave = BottomsUpSaveGame.resolveConflicts(s1, s2, sc);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String conflictID = task1.getResult().getConflict().getConflictId();
        String description = getString(R.string.saveDescription, newSave.getCoins(), newSave.getUnlockedCategories().size());
        SnapshotMetadataChange smdc = new SnapshotMetadataChange.Builder()
                .setDescription(description)
                .build();
        snapshotsClient.resolveConflict(conflictID, mCurrentSaveName, smdc, sc).addOnSuccessListener(snapshotDataOrConflict -> {
            newSave.unloadSave(getApplicationContext());
            // Show the user that the update was a success
            setCloudSavesIconState(CLOUD_DONE, false);
            if (checkLaunchPromoEligible()) {
                giveLaunchPromoCoins();
            }
            updateCoinsValue();
        });
    }

    private void createAndWriteNewSaveGame(Task<SnapshotsClient.DataOrConflict<Snapshot>> task1) {
        Snapshot snapshot = task1.getResult().getData();
        BottomsUpSaveGame saveGame = new BottomsUpSaveGame();
        HashSet<String> set = BottomsUpStorageHelper.getUnlockedCategories(getApplicationContext());
        saveGame.getUnlockedCategories().addAll(set);
        saveGame.setCoins(BottomsUpStorageHelper.getCoins(getApplicationContext()));
        saveGame.set_launch_promo_coins_given(BottomsUpStorageHelper.getLaunchPromoCoinsGiven(getApplicationContext()));
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_foreground);
        String desc = getString(R.string.saveDescription, saveGame.getCoins(), saveGame.getUnlockedCategories().size());
        writeSnapshot(snapshot, saveGame.toBytes(), bmp, desc).addOnSuccessListener(ssmd -> {
            BottomsUpStorageHelper.setLaunchPromoCoinsGiven(getApplicationContext(), saveGame.is_launch_promo_coins_given());
            setCloudSavesIconState(CLOUD_DONE, false);
            if (checkLaunchPromoEligible()) {
                giveLaunchPromoCoins();
            }
            updateCoinsValue();
        });
    }

    /*
        Promo Code
     */
    private boolean checkLaunchPromoEligible() {
        return remoteConfig.getBoolean(LAUNCH_PROMO_KEY) && !BottomsUpStorageHelper.getLaunchPromoCoinsGiven(getApplicationContext()) && BottomsUpStorageHelper.getSnapshotUID(getApplicationContext()).equals(BOTTOMS_UP_SAVE_NAME);
    }

    private void giveLaunchPromoCoins() {
        BottomsUpStorageHelper.setLaunchPromoCoinsGiven(getApplicationContext(), true);
        forceSave();
        BottomsUpStorageHelper.setCoins(getApplicationContext(), BottomsUpStorageHelper.getCoins(getApplicationContext()) + 50);
    }

    private void forceSave() {
        // If we can then update the UI to reflect that we're updating
        setCloudSavesIconState(CLOUD_UPLOAD, false);
        final SnapshotsClient snapshotsClient =
                Games.getSnapshotsClient(this, GoogleSignIn.getLastSignedInAccount(this));
        snapshotsClient.open(mCurrentSaveName, true);
        Task<SnapshotsClient.DataOrConflict<Snapshot>> saveTask = snapshotsClient.open(mCurrentSaveName, true);
        saveTask.addOnCompleteListener(task1 -> {
            if (task1.getResult().isConflict()) {
                resolveSnapshotConflicts(task1, snapshotsClient);
            } else {
                createAndWriteNewSaveGame(task1);
            }
        });
    }
}