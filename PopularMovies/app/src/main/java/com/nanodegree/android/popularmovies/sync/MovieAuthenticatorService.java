package com.nanodegree.android.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.nanodegree.android.popularmovies.sync.MovieAuthenticator;

public class MovieAuthenticatorService extends Service {
    private MovieAuthenticator movieAuthenticator;

    public MovieAuthenticatorService() {
    }

    @Override
    public void onCreate() {
        movieAuthenticator=new MovieAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return movieAuthenticator.getIBinder();
    }
}










