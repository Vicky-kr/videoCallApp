package com.example.strangers.models;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class OkkHttp {

    public OkkHttp() {

    }

    public JSONObject getRandomUser() {
        JSONObject user = new JSONObject();;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://randomuser.me/api/")
                .build();

        client.newCall(request).

                enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        } else {
                            // do something wih the result
                            try {
                                JSONArray array = new JSONArray(new JSONObject(response.body().string()).getJSONArray("results").toString());

                                String fullName = new JSONObject(String.valueOf(array.get(0))).getJSONObject("name").getString("first")
                                        + " " + new JSONObject(String.valueOf(array.get(0))).getJSONObject("name").getString("last");
                                String email = new JSONObject(String.valueOf(array.get(0))).getString("email");
                                String photo = new JSONObject(String.valueOf(array.get(0))).getJSONObject("picture").getString("large");
//                        Log.e("MESSAGE", String.valueOf(array.get(0)));
//                                Log.e("FULL NAME", fullName);
//                                Log.e("EMAIL", email);
//                                Log.e("PHOTO", photo);
                                user.put("fullName", fullName);
                                user.put("email", email);
                                user.put("photo", email);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                });
        return user;
    }
}
