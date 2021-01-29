package dev.moutamid.chatty;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class UserNameActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Button userNameBtn;
    private EditText userNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);
        sharedPreferences = UserNameActivity.this.getSharedPreferences("dev.moutamid.chatty", Context.MODE_PRIVATE);
        userNameBtn = findViewById(R.id.userNameSubmitBtn);
        userNameEditText = findViewById(R.id.userNameEditText);

        userNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                String name = userNameEditText.getText().toString().trim();// + time;

                if (!TextUtils.isEmpty(name)) {
                    sharedPreferences.edit().putString("userName", name).apply();
                    finish();
                    startActivity(new Intent(UserNameActivity.this, TabbedActivity.class));
                } else {
                    userNameEditText.setError("Please add a name!");
                    userNameEditText.requestFocus();
                }
            }
        });

    }
}
