package com.example.gopet;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class BreedInfoActivity extends AppCompatActivity {

    TextView breedNameTextView, descriptionTextView, temperamentTextView, originTextView;
    ImageView imageView;
    String breed, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_breed_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        breedNameTextView = findViewById(R.id.breedNameTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        temperamentTextView = findViewById(R.id.temperamentTextView);
        originTextView = findViewById(R.id.originTextView);
        imageView = findViewById(R.id.breedImageView);

        breed = getIntent().getStringExtra("breed");
        type = getIntent().getStringExtra("type");

        if (type.equals("cat")) {
            CatHelperAPI.fetchCatInfo(breed, new CatHelperAPI.CatInfoCallback() {
                @Override
                public void onResult(String name, String description, String temperament, String origin, String imageUrl) {
                    runOnUiThread(() -> {
                        breedNameTextView.setText(name);
                        descriptionTextView.setText(description);
                        temperamentTextView.setText("Temperament: " + temperament);
                        originTextView.setText("Origine: " + origin);
                        Picasso.get().load(imageUrl).into(imageView);
                        progressBar.setVisibility(View.GONE);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(BreedInfoActivity.this, error, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        } else if (type.equals("dog")) {
            DogHelperAPI.fetchDogInfo(breed, new DogHelperAPI.DogInfoCallback() {
                @Override
                public void onResult(String name, String description, String temperament, String origin, String imageUrl) {
                    runOnUiThread(() -> {
                        breedNameTextView.setText(name);
                        descriptionTextView.setText(description);
                        temperamentTextView.setText("Temperament: " + temperament);
                        originTextView.setText("Origine: " + origin);

                        Picasso.get().load(imageUrl).into(imageView);
                        progressBar.setVisibility(View.GONE);
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(BreedInfoActivity.this, error, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        }


    }

    private void fetchDogBreedImage(String breedName) {
        String breed = breedName.toLowerCase().replace(" ", "");
        String url = "https://dog.ceo/api/breed/" + breed + "/images";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(BreedInfoActivity.this, "Eroare" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(BreedInfoActivity.this, "Rasa nu a fost găsită.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                try {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);
                    JSONArray images = obj.getJSONArray("message");
                    if (images.length() > 0) {
                        String imageUrl = images.getString(0);
                        runOnUiThread(() -> {
                            breedNameTextView.setText(breedName);
                            descriptionTextView.setText("");
                            temperamentTextView.setText("");
                            originTextView.setText("");
                            Picasso.get().load(imageUrl).into(imageView);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(BreedInfoActivity.this, "Nu există imagini pentru această rasă.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(BreedInfoActivity.this, "Eroare", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }
        });
    }
}
