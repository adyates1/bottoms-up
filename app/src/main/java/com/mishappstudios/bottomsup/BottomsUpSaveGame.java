package com.mishappstudios.bottomsup;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotContents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class to represent Saved Game Data for Bottoms Up!
 */
public class BottomsUpSaveGame {
    // serialization format version
    private static final String SERIAL_VERSION = "1.1";
    private ArrayList<String> unlockedCategories = new ArrayList<>();
    private int coins = 0;
    private boolean launch_promo_coins_given = false;

    BottomsUpSaveGame() {
    }

    /**
     * Constructs a BottomsUpSaveGame object from serialized data.
     */
    BottomsUpSaveGame(byte[] data) {
        if (data == null) return; // default progress
        loadFromJson(new String(data));
    }

    /**
     * Merges conflicting snapshots
     *
     * @param firstSnapshot  any Snapshot
     * @param secondSnapshot any Snapshot conflicting with firstSnapshot
     * @param resolver       The SnapshotContents to use to resolve the conflicts
     */
    public static BottomsUpSaveGame resolveConflicts(Snapshot firstSnapshot, Snapshot secondSnapshot, SnapshotContents resolver) throws IOException {
        BottomsUpSaveGame firstGameData = new BottomsUpSaveGame(firstSnapshot.getSnapshotContents().readFully());
        BottomsUpSaveGame secondGameData = new BottomsUpSaveGame(secondSnapshot.getSnapshotContents().readFully());
        BottomsUpSaveGame resolvedData = new BottomsUpSaveGame();
        resolvedData.coins = firstGameData.coins + secondGameData.coins;
        resolvedData.launch_promo_coins_given = firstGameData.launch_promo_coins_given || secondGameData.launch_promo_coins_given;
        ArrayList<String> resolvedUnlockedCategories = new ArrayList<>(firstGameData.unlockedCategories);
        for (String s : secondGameData.unlockedCategories) {
            if (!resolvedUnlockedCategories.contains(s)) resolvedUnlockedCategories.add(s);
        }
        resolvedData.unlockedCategories = resolvedUnlockedCategories;
        resolver.writeBytes(resolvedData.toBytes());
        return resolvedData;
    }

    /**
     * Gets the unlocked Categories
     *
     * @return
     */
    public ArrayList<String> getUnlockedCategories() {
        return unlockedCategories;
    }

    /**
     * Constructs a BottomsUpSaveGame object by reading from a SharedPreferences.
     */
    public BottomsUpSaveGame(SharedPreferences sp, String key) {
        loadFromJson(sp.getString(key, ""));
    }

    /**
     * Setter for coins
     *
     * @param coins
     */
    public void setCoins(int coins) {
        this.coins = coins;
    }

    /**
     * Serializes this BottomsUpSaveGame to an array of bytes.
     */
    public byte[] toBytes() {
        return toString().getBytes();
    }

    /**
     * Serializes this SaveGame to a JSON string.
     */
    @Override
    public String toString() {
        JSONArray arr = new JSONArray(unlockedCategories);
        try {
            JSONObject obj = new JSONObject();
            obj.put("version", SERIAL_VERSION);
            obj.put("cats", arr);
            obj.put("coins", coins);
            obj.put("launch_promo_coins_given", launch_promo_coins_given);
            return obj.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error converting save data to JSON.", ex);
        }
    }

    /**
     * Returns a clone of this SaveGame object.
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public BottomsUpSaveGame clone() {
        BottomsUpSaveGame result = new BottomsUpSaveGame();
        for (String cat : unlockedCategories) {
            result.addUnlockedCategory(cat);
        }
        result.launch_promo_coins_given = launch_promo_coins_given;
        result.coins = coins;
        return result;
    }

    private void addUnlockedCategory(String cat) {
        unlockedCategories.add(cat);
    }

    /**
     * Replaces this BottomsUpSaveGame's content with the content loaded from the given JSON string.
     */
    private void loadFromJson(String json) {
        zero();
        if (json == null || json.trim().equals("")) return;
        try {
            JSONObject obj = new JSONObject(json);
            String format = obj.getString("version");
            if (!format.equals(SERIAL_VERSION)) {
                throw new RuntimeException("Unexpected loot format " + format);
            }
            coins = obj.getInt("coins");
            for (int i = 0; i < obj.getJSONArray("cats").length(); i++) {
                unlockedCategories.add(obj.getJSONArray("cats").getString(i));
            }
            try {
                launch_promo_coins_given = obj.getBoolean("launch_promo_coins_given");
            } catch (JSONException e) {
                launch_promo_coins_given = false;
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            // NOTE: In your game, you want to try recovering from the snapshot payload.
            coins = 0;
            unlockedCategories = new ArrayList<>();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Save data has an invalid number in it: " + json, ex);
        }
    }

    /**
     * Returns whether or not BottomsUpSaveGame is empty.
     */
    public boolean isZero() {
        return coins == 0 && unlockedCategories.size() == 0 && !launch_promo_coins_given;
    }


    /**
     * Gets how many stars the player has on the given level. If the level does not exist
     * in the save game, will return 0.
     */
    public int getCoins() {
        return coins;
    }

    /**
     * Resets this SaveGame object to be empty. Empty means no stars on no levels.
     */
    private void zero() {
        coins = 0;
        unlockedCategories = new ArrayList<>();
    }

    public void unloadSave(Context c) {
        BottomsUpStorageHelper.setCoins(c, this.coins);
        for (String s : unlockedCategories) {
            BottomsUpStorageHelper.addUnlockedCategory(c, s);
        }
        BottomsUpStorageHelper.setLaunchPromoCoinsGiven(c, launch_promo_coins_given);
    }

    public boolean is_launch_promo_coins_given() {
        return launch_promo_coins_given;
    }

    public void set_launch_promo_coins_given(boolean launch_promo_coins_given) {
        this.launch_promo_coins_given = launch_promo_coins_given;
    }
}
