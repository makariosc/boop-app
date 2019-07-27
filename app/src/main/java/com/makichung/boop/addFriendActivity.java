package com.makichung.boop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class addFriendActivity extends AppCompatActivity {

    @BindView(R.id.newFriendUsernameField) EditText newFriendUsernameField;
    @BindView(R.id.addFriendButton) Button addFriendButton;
    User currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ButterKnife.bind(this);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    File file = new File(getDir(LoginActivity.CURR_USER_OBJ, MODE_PRIVATE), "user");
                    ObjectInputStream getUser = new ObjectInputStream(new FileInputStream(file));
                    currUser = (User) getUser.readObject();
                    getUser.close();

                } catch (Exception e) {
                    //TODO: Something when getUser is screwed
                }

                currUser.addFriend(newFriendUsernameField.getText().toString());
                File file = new File(getDir(LoginActivity.CURR_USER_OBJ, MODE_PRIVATE), "user");
                try {
                    ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                    outputStream.writeObject(currUser);
                    outputStream.flush();
                    outputStream.close();

                } catch (Exception e) {
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("newfriend", newFriendUsernameField.getText().toString());
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }
}
