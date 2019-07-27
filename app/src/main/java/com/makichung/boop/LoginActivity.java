package com.makichung.boop;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username) TextView usernameField;
    @BindView(R.id.password) TextView passwordField;
    @BindView(R.id.loginButton) Button loginButton;
    @BindView(R.id.registerIntentButton) Button registerIntentButton;

    public static final String CURR_USER_OBJ = "currentUserInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        final Activity activity = this;

        File file = new File(getDir(CURR_USER_OBJ, MODE_PRIVATE), "user");
        try {
            ObjectInputStream userInputStream = new ObjectInputStream(new FileInputStream(file));
            User currUser = (User) userInputStream.readObject();
            userInputStream.close();

            if (currUser != null) {
                Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
                loginIntent.putExtra("username", currUser.getUsername());
                startActivity(loginIntent);
            }


        } catch (Exception e) {
            Log.d("LoginActivity", "No previous user detected, going to login screen");
        }

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final String username = usernameField.getText().toString();
                final String password = passwordField.getText().toString();

                RedisService.getService().getUser(username).enqueue(new Callback<RedisService.GetResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.GetResponse> call, Response<RedisService.GetResponse> response) {
                        if (response.body().user.validPassword(password)){

                            File file = new File(getDir(CURR_USER_OBJ, MODE_PRIVATE), "user");
                            try {
                                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                                outputStream.writeObject(response.body().user);
                                outputStream.flush();
                                outputStream.close();
                                Log.e("LoginActivity", "Logging in with user " + username);
                            } catch (Exception e){
                                Log.e("LoginActivity", "Could not write loginUser to file");
                            }
                            Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
                            loginIntent.putExtra("username", username);
                            startActivity(loginIntent);
                        } else {
                            Toast.makeText(activity, "Invalid password!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {
                        Log.d("THROWABLE", t.getLocalizedMessage());
                        Toast.makeText(activity, "User with username " + username + " does not exist!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        registerIntentButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }

        });


        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.INTERNET) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("Testing", "no internet permissions");
            int requestCode = 1;
            ArrayList<String> permissions = new ArrayList<>(2);
            ActivityCompat.requestPermissions(getParent(), permissions.toArray(new String[1]), requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestcode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++){
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Can't proceed if you deny the permission!", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }
}
