package com.example.a5geigir;

import static com.google.firebase.FirebaseOptions.*;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class TokenProvider extends FirebaseMessagingService {

    private static TokenProvider instance = null;
    private Context context;
    private static String token;

    private TokenProvider(Context context){
        this.context = context;

        /*FileInputStream refreshToken = new FileInputStream("path/to/refreshToken.json");

        FirebaseOptions options = FirebaseOptions.builder()

                .build();*/

        //FirebaseApp.initializeApp(context);
    }

    public static TokenProvider getInstance(Context context){
        if (instance == null)
            instance = new TokenProvider(context);
        return instance;
    }

    @Override
    public void onNewToken(String s) {  //https://stackoverflow.com/a/41515597
        super.onNewToken(s);
        Log.d("SignalDB", "New token: " + s);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    public String getToken() {
        FirebaseApp.initializeApp(context);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("Firebase", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();

                        Log.d("Firebase", "token: " + token);
                    }
                });

        return token;
    }
}
