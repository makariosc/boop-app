package com.makichung.boop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycler) RecyclerView recycler;
    @BindView(R.id.logoutButton) TextView logoutButton;
    @BindView(R.id.navigation) NavigationView navigation;

    static User currUser;
    UserAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    static Map<String, Integer> fromFriends;
    static Map<String, Integer> toFriends;
    static ArrayList<String> friendsUsernames;
    BroadcastReceiver br;

    public final int REQUEST_ADD_FRIEND = 0;
    public static final int REQUEST_BOOP_PAGE = 1;

    @Override
    protected void onResume(){
        super.onResume();

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
                currUser.updateFriends(fromFriends, toFriends);
                saveUser(currUser, getApplicationContext());
            }
        };

        IntentFilter broadcastFilter = new IntentFilter("com.makichung.boop.NEW_BOOP");
        this.registerReceiver(br, broadcastFilter);

        currUser = getCurrUser(getApplicationContext());

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent service = new Intent(getBaseContext(), MessagingService.class);
        startService(service);

        currUser = getCurrUser(getApplicationContext());
        currUser.setToken(FirebaseInstanceId.getInstance().getToken());
        saveUser(currUser, getApplicationContext());

        navigation.getMenu().findItem(R.id.menuUsername).setTitle("Welcome, " + currUser.getUsername() + "!");
        navigation.getMenu().findItem(R.id.total_boops).setTitle("Total Boops: " + Integer.toString(currUser.getTotalBoops()));

        fromFriends = new HashMap<String, Integer>((currUser.getFromFriends()));
        toFriends = new HashMap<String, Integer>((currUser.getToFriends()));
        friendsUsernames = new ArrayList<>(fromFriends.keySet());
        adapter = new UserAdapter(fromFriends, toFriends, friendsUsernames);

        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);

        navigation.getMenu().findItem(R.id.add_new_friend).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                saveUser(currUser, getApplicationContext());

                Intent addNewFriendIntent = new Intent(navigation.getContext(), addFriendActivity.class);
                startActivityForResult(addNewFriendIntent, REQUEST_ADD_FRIEND);
                return false;
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RedisService.getService().setUser(currUser.getUsername(), currUser).enqueue(new Callback<RedisService.SetResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.SetResponse> call, Response<RedisService.SetResponse> response) {
                        try {
                            File file = new File(getDir(LoginActivity.CURR_USER_OBJ, MODE_PRIVATE), "user");
                            new FileOutputStream(file).close();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                        } catch (Exception e){
                            //TODO: Something when getUser is screwed
                        }
                    }

                    @Override
                    public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                        Log.e("LogoutButton", "Unable to store updated user in Server!");
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == REQUEST_ADD_FRIEND) {
            if (resultCode == RESULT_OK) {
                Log.d("ONACTIVITY RESULT", "DAMMIT WE'RE IN HERE LET US OUT");
                Log.d("New friend", data.getStringExtra("newfriend"));
                toFriends.put(data.getStringExtra("newfriend"), 0);
                fromFriends.put(data.getStringExtra("newfriend"), 0);
                friendsUsernames.add(data.getStringExtra("newfriend"));
                adapter.notifyDataSetChanged();
            }
        }

    }

    //Update count for user, add friend if they don't exist
    static void updateFromBoopCount(String user){
        if (!friendsUsernames.contains(user)) {
            friendsUsernames.add(user);
            fromFriends.put(user, 1);
            currUser.addFriend(user);
        } else {
            int count = fromFriends.get(user);
            fromFriends.put(user, count+1);
        }

        //currUser.getBoopFromFriend(user);
    }

    static User getCurrUser(Context context) {
        User ret;
        try {
            File file = new File(context.getDir(LoginActivity.CURR_USER_OBJ, MODE_PRIVATE), "user");
            ObjectInputStream getUser = new ObjectInputStream(new FileInputStream(file));
            ret = (User) getUser.readObject();
            getUser.close();
            return ret;

        } catch (Exception e){
            //TODO: Something when getUser is screwed
            return null;
        }

    }

    static void saveUser(User user, Context context){
        File file = new File(context.getDir(LoginActivity.CURR_USER_OBJ, MODE_PRIVATE), "user");
        try {
            RedisService.getService().setUser(currUser.getUsername(), currUser).enqueue(new Callback<RedisService.SetResponse>() {
                @Override
                public void onResponse(Call<RedisService.SetResponse> call, Response<RedisService.SetResponse> response) {
                    Log.d("UpdateUser", "Saved user to server");
                }

                @Override
                public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {

                }
            });
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(user);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(br);
    }
}
