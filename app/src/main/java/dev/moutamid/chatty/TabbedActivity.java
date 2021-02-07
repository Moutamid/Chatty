package dev.moutamid.chatty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class TabbedActivity extends AppCompatActivity {

    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager viewPager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        sharedPreferences = TabbedActivity.this.getSharedPreferences("dev.moutamid.chatty", Context.MODE_PRIVATE);

        String userName = sharedPreferences.getString("userName", "Error");

        if (userName.equals("Error")){
            finish();
            startActivity(new Intent(TabbedActivity.this, UserNameActivity.class));
        }

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    public void setupViewPager(ViewPager viewPager) {

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(), "Chats");
//        adapter.addFragment(new StatusFragment(), "Status");
        adapter.addFragment(new InfoFragment(), "info");

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
