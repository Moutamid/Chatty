package dev.moutamid.chatty;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

// // TODO: DOWNLOAD THIS LIBRARY import ai.api.AIDataService;
//import ai.api.AIListener;
//import ai.api.AIServiceException;
//import ai.api.android.AIConfiguration;
//import ai.api.android.AIService;
//import ai.api.model.AIError;
//import ai.api.model.AIRequest;
//import ai.api.model.AIResponse;
//import ai.api.model.Result;
// TODO: DOWNLOAD THIS LIBRARY import com.google.gson.JsonElement;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(MainActivity.this, TabbedActivity.class));
            }
        }, 1000);

    }


}
