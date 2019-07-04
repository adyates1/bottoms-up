package com.mishappstudios.bottomsup;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import static com.mishappstudios.bottomsup.Constants.FIREBASE_INSTANCE_ID;
import static com.mishappstudios.bottomsup.Constants.SNAPSHOT_NOT_CREATED;
import static com.mishappstudios.bottomsup.Constants.SNAPSHOT_ON_DEVICE_KEY;
import static com.mishappstudios.bottomsup.Constants.USER_COINS;
import static com.mishappstudios.bottomsup.Constants.USER_UNLOCKED_CATEGORIES;

/**
 * Storage helper for Bottoms Up!
 */
public class BottomsUpStorageHelper {

    /**
     * Get the number of coins
     *
     * @param c context
     * @return
     */
    public static int getCoins(Context c) {
        return getSharedPrefs(c).getInt(USER_COINS, 0);
    }

    /**
     * Gets all unlocked categories
     *
     * @param c context
     * @return
     */
    public static HashSet<String> getUnlockedCategories(Context c) {
        return (HashSet<String>) getSharedPrefs(c).getStringSet(USER_UNLOCKED_CATEGORIES, new HashSet<>());
    }

    /**
     * Sets the Snapshot UID
     *
     * @param c           context
     * @param snapshotUID the SnapshotUID to set
     */
    public static void setSnapshotUID(Context c, String snapshotUID) {
        SharedPreferences.Editor editor = getSharedPrefs(c).edit();
        editor.putString(SNAPSHOT_ON_DEVICE_KEY, snapshotUID);
        editor.commit();
    }

    /**
     * Gets the Snapshot UID
     *
     * @param c context
     * @return the Snapshot UID stored
     */
    public static String getSnapshotUID(Context c) {
        return getSharedPrefs(c).getString(SNAPSHOT_ON_DEVICE_KEY, SNAPSHOT_NOT_CREATED);
    }

    /**
     * Sets the number of coins
     *
     * @param c     context
     * @param coins number of coins to set
     */
    public static void setCoins(Context c, int coins) {
        SharedPreferences.Editor editor = getPrefsEditor(c);
        editor.putInt(USER_COINS, coins);
        editor.apply();
    }

    /**
     * Adds an unlocked category to the unlocked set
     *
     * @param c          context
     * @param cateogryID unlocked cateogoryID
     */
    public static void addUnlockedCategory(Context c, String cateogryID) {
        SharedPreferences.Editor editor = getPrefsEditor(c);
        Set<String> unlockedCats = getSharedPrefs(c).getStringSet(USER_UNLOCKED_CATEGORIES, new HashSet<>());
        unlockedCats.add(cateogryID);
        editor.putStringSet(USER_UNLOCKED_CATEGORIES, unlockedCats);
        editor.apply();
    }

    /**
     * Gets the stored FirebaseInstanceID
     *
     * @param c context
     * @return the Firebase Instance ID
     */
    public static String getFirebaseInstanceID(Context c) {
        return getSharedPrefs(c).getString(FIREBASE_INSTANCE_ID, "null");
    }

    /**
     * Sets the FirebaseInstanceID
     *
     * @param c  context
     * @param ID the FirebaseInstanceID to set
     */
    public static void setFirebaseInstanceID(Context c, String ID) {
        SharedPreferences.Editor editor = getSharedPrefs(c).edit();
        editor.putString(FIREBASE_INSTANCE_ID, ID);
        editor.apply();
    }

    /**
     * Gets the Default SharedPreferences for Read-Only access
     *
     * @param c context
     * @return the default SharedPreferences
     */
    private static SharedPreferences getSharedPrefs(Context c) {
        return c.getSharedPreferences(
                c.getString(R.string.dataStorageKey), Context.MODE_PRIVATE);
    }

    /**
     * Gets the Default SharedPreferences for Write access
     *
     * @param c context
     * @return a SharedPreferences.Editor object
     */
    private static SharedPreferences.Editor getPrefsEditor(Context c) {
        return c.getSharedPreferences(
                c.getString(R.string.dataStorageKey), Context.MODE_PRIVATE).edit();
    }

    public static void setLaunchPromoCoinsGiven(Context c, boolean flag) {
        getPrefsEditor(c).putBoolean("launchpromocoinsgiven", flag).commit();
    }

    public static boolean getLaunchPromoCoinsGiven(Context c) {
        return getSharedPrefs(c).getBoolean("launchpromocoinsgiven", false);
    }
}