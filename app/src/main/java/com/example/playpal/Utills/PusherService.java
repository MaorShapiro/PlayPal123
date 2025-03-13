package com.example.playpal.Utills;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;

public class PusherService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        PusherOptions options = new PusherOptions();
        options.setCluster("mt1");

        Pusher pusher = new Pusher("1a9bf825ae43161ae6c6", options);
        pusher.connect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
