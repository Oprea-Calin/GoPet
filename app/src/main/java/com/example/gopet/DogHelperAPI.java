package com.example.gopet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class DogHelperAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_KEY = "live_cM448QnGZ32C4DtFsJbtv4UmD92ee6PX54rsY6FOhD3R8omP32eQZxnodzUshiTI";

    public interface DogInfoCallback {
        void onResult(String name, String description, String temperament, String origin, String imageUrl);
        void onError(String error);
    }

    public static void fetchDogInfo(String breedName, DogInfoCallback callback) {
        String url = "https://api.thedogapi.com/v1/breeds/search?q=" + breedName;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Error: " + response.message());
                    return;
                }

                String jsonData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    if (jsonArray.length() > 0) {
                        JSONObject dogData = jsonArray.getJSONObject(0);
                        String name = dogData.getString("name");
                        String desc = dogData.optString("bred_for", "Unknown");
                        String temp = dogData.optString("temperament", "Unknown");
                        String origin = dogData.optString("origin", "Unknown");
                        String imageUrl = "";

                        if (dogData.has("reference_image_id")) {
                            String imageId = dogData.getString("reference_image_id");
                            imageUrl = "https://cdn2.thedogapi.com/images/" + imageId + ".jpg";
                        }

                        callback.onResult(name, desc, temp, origin, imageUrl);
                    } else {
                        callback.onError("Breed '" + breedName + "' not found.");
                    }
                } catch (Exception e) {
                    callback.onError("Error: " + e.getMessage());
                }
            }
        });
    }
}