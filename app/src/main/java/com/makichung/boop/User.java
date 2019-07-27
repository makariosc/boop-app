package com.makichung.boop;

import android.support.v7.widget.RecyclerView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nimrod on 12/9/2017.
 */

public class User implements Serializable{

    Map<String, Integer> toFriends = new HashMap<String, Integer>(); //Pokes you sent
    Map<String, Integer> fromFriends = new HashMap<String, Integer>(); //Pokes to you
    String username;
    String password;
    String token;
    int totalBoops;

    public User(Map<String, Integer> toFriends, String username, String password, int totalBoops){ //lol plaintext passwords
        toFriends = this.toFriends;
        fromFriends = this.fromFriends;
        this.username = username;
        this.password = password;
        this.totalBoops = totalBoops;
    }

    public void updateFriends(Map<String, Integer> fromFriends, Map<String, Integer> toFriends){
        this.fromFriends = fromFriends;
        this.toFriends = toFriends;
    }

    public void sendBoopToFriend(String friend){
        int count = toFriends.containsKey(friend) ? toFriends.get(friend) : 0;
        toFriends.put(friend, count+1);
        totalBoops++;
    }

    public void getBoopFromFriend(String friend) {
        int count = fromFriends.containsKey(friend)? fromFriends.get(friend) : 0;
        fromFriends.put(friend, count + 1);
    }

    public void addFriend(String friend){
        if (!toFriends.containsKey(friend)){
            toFriends.put(friend, 0);
            fromFriends.put(friend, 0);
        }
    }

    public int getBoopsFromFriend(String friend) {
        return fromFriends.get(friend);
    }

    public int getBoopsToFriend(String friend) {
        return toFriends.get(friend);
    }

    public String getUsername(){
        return username;
    }

    public boolean validPassword(String password){
        return this.password.equals(password);
    }

    public void setToken(String newToken){
        token = newToken;
    }

    public String getToken(){
        return token;
    }

    public int getTotalBoops(){
        return totalBoops;
    }

    public Map<String, Integer> getFromFriends(){
        return fromFriends;
    }

    public Map<String, Integer> getToFriends() {
        return toFriends;
    }

    public Set<String> getFriendUsernames(){
        return fromFriends.keySet();
    }



}
