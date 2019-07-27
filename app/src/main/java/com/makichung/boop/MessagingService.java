package com.makichung.boop;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by nimrod on 12/10/2017.
 */

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message){
        User currUser = MainActivity.getCurrUser(getApplicationContext());

        if (message.getData().get("fromuser") != null) {
            String fromUser = message.getData().get("fromuser");
            Log.d("From", fromUser);

            if (currUser.getFriendUsernames().contains(fromUser)) {

                MainActivity.updateFromBoopCount(fromUser);
                MainActivity.saveUser(currUser, getApplicationContext());


                //Tells MainActivity to update count immediately if it's awake-- if it's asleep it won't do anything
                Intent newMessage = new Intent();
                newMessage.putExtra("from", fromUser);
                newMessage.setAction("com.makichung.boop.NEW_BOOP");
                sendBroadcast(newMessage);

            }
        }

    }




}
