package com.example.gopet;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

public class BreedInfoActivity extends AppCompatActivity {

    TextView breedNameTextView, descriptionTextView, temperamentTextView, originTextView;
    ImageView imageView;

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

        breedNameTextView = findViewById(R.id.breedNameTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        temperamentTextView = findViewById(R.id.temperamentTextView);
        originTextView = findViewById(R.id.originTextView);
        imageView = findViewById(R.id.breedImageView);

        String breed = getIntent().getStringExtra("breed");

        CatHelperAPI.fetchCatInfo(breed, new CatHelperAPI.CatInfoCallback() {
            @Override
            public void onResult(String name, String description, String temperament, String origin, String imageUrl) {
                runOnUiThread(() -> {
                    breedNameTextView.setText(name);
                    descriptionTextView.setText(description);
                    temperamentTextView.setText("Temperament: " + temperament);
                    originTextView.setText("Origine: " + origin);

                    Picasso.get()
                            .load(imageUrl)
                            .into(imageView);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(BreedInfoActivity.this, error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}
