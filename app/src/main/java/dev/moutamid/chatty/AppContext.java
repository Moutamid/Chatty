package dev.moutamid.chatty;

import com.google.firebase.database.FirebaseDatabase;

public class AppContext extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
