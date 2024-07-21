package com.mibobinopro;
import com.google.firebase.database.FirebaseDatabase;
import android.app.Application;

public class Persistence extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Habilitar persistencia offline para Firebase Realtime Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
