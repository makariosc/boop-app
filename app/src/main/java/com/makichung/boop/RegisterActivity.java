package com.makichung.boop;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.newUsernameField) EditText newUsernameField;
    @BindView(R.id.newPasswordField) EditText newPasswordField;
    @BindView(R.id.confirmPasswordField) EditText confirmPasswordfield;
    @BindView(R.id.registerButton) Button registerButton;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        context = getApplicationContext();

        //TODO: Make sure confirm and password are the same

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String newUsername = newUsernameField.getText().toString();
                final String newPassword = newPasswordField.getText().toString();

                RedisService.getService().getUser(newUsername).enqueue(new Callback<RedisService.GetResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.GetResponse> call, Response<RedisService.GetResponse> response) {
                        Toast.makeText(context, "Username " + newUsername + " already taken!", Toast.LENGTH_SHORT).show();
                        Log.d("WTFFFF", "WTFFF");
                    }

                    @Override
                    public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {

                        Log.d("WTFFFFFAILURE", "WTFFF");

                            final User newUser = new User(new HashMap<String, Integer>(), newUsername, newPassword, 0);
                            Log.d("New username", newUsername);

                            RedisService.getService().setUser(newUsername, newUser).enqueue(new Callback<RedisService.SetResponse>() {
                                @Override
                                public void onResponse(Call<RedisService.SetResponse> call, Response<RedisService.SetResponse> response) {

                                    try {
                                        File file = new File(getDir(LoginActivity.CURR_USER_OBJ, MODE_PRIVATE), "user");
                                        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                                        outputStream.writeObject(newUser);
                                        outputStream.flush();
                                        outputStream.close();
                                        Log.e("LoginActivity", "Logging in with user " + newUsername);
                                    } catch (Exception e){
                                        Log.e("LoginActivity", "Could not write newUser to file");
                                    }

                                    Log.d("Created new user", newUser.getUsername());

                                    Intent loginIntent = new Intent(context, MainActivity.class);
                                    loginIntent.putExtra("currUser", newUsername);
                                    startActivity(loginIntent);
                                }

                                @Override
                                public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {

                                    Log.d("WTFFFFFAILURE2", "WTFFF");

                                    Toast.makeText(context, "Failed to connect to server wtf", Toast.LENGTH_SHORT).show();

                                }

                            });

                    }
                });
            }
        });

    }
}
