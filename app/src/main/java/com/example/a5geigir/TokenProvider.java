package com.example.a5geigir;

import static com.google.firebase.FirebaseOptions.*;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class TokenProvider extends FirebaseMessagingService {

    private static String token;
    private static int TOKEN_LENGTH = 10;

    public static String getShortenedToken(Context context){
        initialize(context);
        return token.substring(token.length() - TOKEN_LENGTH);  //last n characters of the token
    }

    public static void initialize(Context context){
        if (token == null)
            generateToken(context);
    }

    private static void generateToken(Context context){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Firebase", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();

                        // Log and toast
                        Log.d("Firebase", "token: " + token);
                    }
                });
    }

}
