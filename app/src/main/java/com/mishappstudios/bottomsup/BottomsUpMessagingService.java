package com.mishappstudios.bottomsup;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Service that communicates with Firebase Messaging Service
 */
public class BottomsUpMessagingService extends FirebaseMessagingService {

    /**
     * This method is called whenever a NEW token is assigned to the user's installation
     *
     * @param token the new token
     */
    public void onNewToken(String token) {
        Log.d("BottomsUpInstanceID", "Refreshed token: " + token);
        BottomsUpStorageHelper.setFirebaseInstanceID(getApplicationContext(), token);
    }

    /**
     * This method is called whenever a message is received to this user's installation
     *
     * @param remoteMessage The remote message's contents
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("Service ", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Log.d("Service ", "Message data payload: " + remoteMessage.getData());
            if (data.containsKey("coinsToGive")) {
                Log.v("ICR COINS", "Increased coins");
                BottomsUpStorageHelper.setCoins(getApplicationContext(), BottomsUpStorageHelper.getCoins(getApplicationContext()) + Integer.valueOf(data.get("coinsToGive")));
            }
        }


        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d("Notify", "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }


    }

}
