package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PetSittingDashboardActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_sitting_dashboard);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new PetSittingPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("All posts");
            } else {
                tab.setText("My posts");
            }
        }).attach();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateBackToSocialFriends();
    }

    private void navigateBackToSocialFriends() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigateTo", "open_social_friends");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        setResult(RESULT_OK);
        finish();
    }

    private static class PetSittingPagerAdapter extends FragmentStateAdapter {
        public PetSittingPagerAdapter(AppCompatActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new AllPetSittingPostsFragment();
            } else {
                return new MyPetSittingPostsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
