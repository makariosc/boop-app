package com.makichung.boop;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class BoopActivity extends AppCompatActivity {

    @BindView(R.id.button) TextView button;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    User currUser;
    BroadcastReceiver br;

    @Override
    protected void onResume(){
        super.onResume();

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                currUser.updateFriends(MainActivity.fromFriends, MainActivity.toFriends);
                MainActivity.saveUser(currUser, getApplicationContext());
            }
        };

        IntentFilter broadcastFilter = new IntentFilter("com.makichung.boop.NEW_BOOP");
        this.registerReceiver(br, broadcastFilter);

        currUser = MainActivity.getCurrUser(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boop);
        ButterKnife.bind(this);

        final String friend = getIntent().getStringExtra("friend");

        try {
            File file = new File(getDir(LoginActivity.CURR_USER_OBJ, MODE_PRIVATE), "user");
            ObjectInputStream getUser = new ObjectInputStream(new FileInputStream(file));
            currUser = (User) getUser.readObject();
            getUser.close();

        } catch (Exception e){
            //TODO: Something when getUser is screwed
        }
        final Intent intent = getIntent();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RedisService.getService().getUser(friend).enqueue(new Callback<RedisService.GetResponse>() {

                    @Override
                    public void onResponse(Call<RedisService.GetResponse> call, retrofit2.Response<RedisService.GetResponse> response) {

                        if (response.body().user.getFriendUsernames().contains(currUser.getUsername())) {
                            sendNotification(friend, response.body().user.getToken());
                        }
                    }

                    @Override
                    public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {
                        Log.d("Just tried to poke", friend);
                        Log.e("IDKWTF", "HELP SOMETHING'S BROKEN");
                    }
                });

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void sendNotification(final String username, final String reg_token) {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try{
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    JSONObject notificationJson = new JSONObject();

                    notificationJson.put("body", currUser.getUsername() + " has poked you " + currUser.getBoopsToFriend(username) + " times!");
                    notificationJson.put("title", "title? apparently? idk anymore man");

                    json.put("to", reg_token);
                    json.put("notification", notificationJson);

                    Log.d("Reg token", reg_token);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Log.d("JSON body", json.toString());
                    Request request = new Request.Builder()
                            .header("Content-Type", "application/json")
                            .addHeader("Authorization", "key=AAAAZ-YQKe8:APA91bEAWUqJkC8FcU1iSJymggln1E41aauRBHW0HBY2DaWEwHazr9_odq8TGBGArWjTb_Eq4xjtBy9wmB7fPQ5ZhYzvX56Yjr52WAqCpeHajo-dZXU0gQqSMD_VqE0Vh_dHNWQTk0fm") //insecure much?
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalresponse = response.body().toString();
                    MainActivity.saveUser(currUser, getApplicationContext());
                } catch (Exception e){
                }
                return null;

            }
        }.execute();

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try{
                    OkHttpClient client = new OkHttpClient();
                    JSONObject dataJson = new JSONObject();
                    JSONObject json = new JSONObject();

                    dataJson.put("fromuser", currUser.getUsername());

                    json.put("to", reg_token);
                    json.put("data", dataJson);

                    Log.d("Reg token", reg_token);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Log.d("JSON body", json.toString());
                    Request request = new Request.Builder()
                            .header("Content-Type", "application/json")
                            .addHeader("Authorization", "key=AAAAZ-YQKe8:APA91bEAWUqJkC8FcU1iSJymggln1E41aauRBHW0HBY2DaWEwHazr9_odq8TGBGArWjTb_Eq4xjtBy9wmB7fPQ5ZhYzvX56Yjr52WAqCpeHajo-dZXU0gQqSMD_VqE0Vh_dHNWQTk0fm") //insecure much?
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalresponse = response.body().toString();
                    currUser.sendBoopToFriend(username);
                    MainActivity.saveUser(currUser, getApplicationContext());
                } catch (Exception e){
                }
                return null;

            }
        }.execute();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(br);
    }

    @Override
    public void onBackPressed(){
        //Cleanup: Store user

        MainActivity.saveUser(currUser, getApplicationContext());

        Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
        //returnIntent.putExtra("newBoops", currUser.getBoops(getIntent().getStringExtra("friend")));
        startActivity(returnIntent);
    }
}
