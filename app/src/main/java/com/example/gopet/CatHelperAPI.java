package com.example.gopet;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
public class CatHelperAPI {


        private static final String API_KEY = "live_LI5Gn3NlzXgyar5gfhthUwKQ2x1WP15raxCbdjjsRWePhStghM94skD9lXEOxviZ";
        private static final OkHttpClient client = new OkHttpClient();

        public interface CatInfoCallback {
            void onResult(String name, String description, String temperament, String origin, String imageUrl);
            void onError(String error);
        }

        public static void fetchCatInfo(String breedName, CatInfoCallback callback) {
            String url = "https://api.thecatapi.com/v1/breeds/search?q=" + breedName;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("x-api-key", API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Eroare" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("Eroare" + response.message());
                        return;
                    }

                    String jsonData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        if (jsonArray.length() > 0) {
                            JSONObject catData = jsonArray.getJSONObject(0);
                            String name = catData.getString("name");
                            String desc = catData.getString("description");
                            String temp = catData.getString("temperament");
                            String origin = catData.getString("origin");
                            String imageUrl = "";

                            // extragem imaginea
                            if (catData.has("reference_image_id")) {
                                String imageId = catData.getString("reference_image_id");
                                imageUrl = "https://cdn2.thecatapi.com/images/" + imageId + ".jpg";
                            }

                            callback.onResult(name, desc, temp, origin, imageUrl);
                        } else {
                            callback.onError("Nu am găsit nicio rasă cu acest nume.");
                        }
                    } catch (Exception e) {
                        callback.onError("Eroare la parsare: " + e.getMessage());
                    }
                }
            });
        }
    }

