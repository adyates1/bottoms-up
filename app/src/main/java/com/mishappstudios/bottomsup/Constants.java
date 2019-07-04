package com.mishappstudios.bottomsup;

/**
 * This class is dedicated to String constants
 * that do not need to be translated / are for
 * developer use only. They represent SKUs,
 * result codes and the like.
 */
class Constants {

    // Google Play In-App Purchase SKUs
    final static String SKU_50_COINS = "50coinspurchase";
    final static String SKU_110_COINS = "110coinspurchase";
    final static String SKU_600_COINS = "600coinspurchase";
    static final String SKU_TO_BUY = "SKU_TO_BUY";

    // Magic Constant replacements
    static int COINS_PURCHASE_INITIATED = 2002;

    // Firebase Analytics Custom Events and Items
    static String UNIMPLEMENTED = "this_feature_has_not_been_implemented";
    static String STARTED_A_GAME_EVENT = "started_game_event";
    static String ENDED_A_GAME_EVENT = "ended_game_event";
    static String NUMBER_OF_PLAYERS_ITEM = "player_count_item";
    static String CATEGORIES_SELECTED_ITEM = "categories_selected_item";
    static String DIFFICULTY_ITEM = "difficulty_item";

    static String FIREBASE_INSTANCE_ID = "firebase_instance_id_pref";

    static String USER_COINS = "UserCoins";
    static String USER_UNLOCKED_CATEGORIES = "UnlockedCats";

    static String FEEDBACK_SERVER_URL = "https://bottoms-up-59b0d.appspot.com/submitForm.php";
    static String SERVER_VERIFICATION_KEY = "xxvkpNFEYM9TM8WhXvFX";

    static String SNAPSHOT_ON_DEVICE_KEY = "snapshot_uid_preference";
    static String SNAPSHOT_NOT_CREATED = "default_snapshot_uid";
    static String BOTTOMS_UP_SAVE_NAME = "BottomsUpSingleSaveKey";

    static String LAUNCH_PROMO_KEY = "launch_promo_free_coins";

}