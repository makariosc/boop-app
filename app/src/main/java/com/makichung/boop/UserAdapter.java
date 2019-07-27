package com.makichung.boop;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by nimrod on 12/10/2017.
 */

public class UserAdapter extends RecyclerView.Adapter<UserHolder> {

    Map<String, Integer> fromFriends;
    Map<String, Integer> toFriends;
    ArrayList<String> friends;

    public UserAdapter (Map<String, Integer> fromFriends, Map<String, Integer> toFriends, ArrayList<String> friends) {
        this.fromFriends = fromFriends;
        this.toFriends = toFriends;
        this.friends = friends;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userholder_layout, parent, false);
        UserHolder ret = new UserHolder(view);
        return ret;
    }

    @Override
    public void onBindViewHolder(final UserHolder holder, int position) {

        final String friend = friends.get(position);
        Log.d("FRIEND", friend);
        String fromCount = fromFriends.get(friend).toString();
        String toCount = toFriends.get(friend).toString();

        holder.friendNameField.setText(friend);
        holder.fromField.setText(fromCount);
        holder.toField.setText(toCount);
        holder.friendItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toBoop = new Intent(holder.friendItem.getContext(), BoopActivity.class);
                toBoop.putExtra("friend", friend);

                holder.friendItem.getContext().startActivity(toBoop);
            }
        });


    }

    @Override
    public int getItemCount() {
        return friends.size();
    }
}
