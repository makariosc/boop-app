package com.makichung.boop;

import android.app.LauncherActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by nimrod on 12/8/2017.
 */

class RedisService {

    private static Service service;

    interface Service {

        // Upload an image
        @PUT("SET/{imageName}")
        Call<SetResponse> postImage(@Path("imageName") String imageName, @Body RequestBody imageFile);

        // Retrieve the value stored under a key
        @GET("GET/{key}")
        Call<GetResponse> getUser(@Path("key") String key);

        // Delete the key and its value
        @GET("DEL/{key}")
        Call<DelResponse> deletePost(@Path("key") String key);

        // Store a value (in a Java class ListItem) under a key
        @PUT("SET/{key}")
        Call<SetResponse> setUser(@Path("key") String key, @Body User body);

        // Get all keys that match the given pattern
        @GET("KEYS/{pattern}*")
        Call<KeysResponse> allKeys(@Path("pattern") String pattern);
    }

    class SetResponse {}

    class DelResponse {}

    class GetResponse {
        @SerializedName("GET")
        User user; // ListItem is a Java class that holds data in your RecyclerView
    }

    class KeysResponse {
        @SerializedName("KEYS")
        ArrayList<String> keys;
    }

    // Required because Redis stores JSON as strings
    public static class GetResponseSerializer implements JsonDeserializer<GetResponse> {
        @Override
        public GetResponse deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException {
            String content = je.getAsJsonObject().getAsJsonPrimitive("GET").getAsString();
            return new Gson().fromJson(String.format(Locale.ENGLISH, "{GET:%s}", content), GetResponse.class);
        }
    }

    static Service getService() {
        if (service == null) {
            GsonBuilder gsonBuilder = new GsonBuilder().setLenient();
            gsonBuilder.registerTypeHierarchyAdapter(GetResponse.class, new GetResponseSerializer());
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://ec2-54-210-25-34.compute-1.amazonaws.com/ba0c6ef2d1f90d164d2fb8c0120922c1/")
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build();

            service = retrofit.create(Service.class);
        }
        return service;
    }
}
