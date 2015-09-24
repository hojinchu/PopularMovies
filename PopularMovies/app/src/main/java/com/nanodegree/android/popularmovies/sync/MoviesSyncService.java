package com.nanodegree.android.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.nanodegree.android.popularmovies.sync.MoviesSyncAdapter;

public class MoviesSyncService extends Service {
    private final Object sSyncAdapterLock= new Object();
    private static MoviesSyncAdapter sMoviesSyncAdapter=null;
    public MoviesSyncService() {
    }

    @Override
    public void onCreate() {
        Log.d("Popular Movies", "onCreate - MoviesSyncService");
        synchronized(sSyncAdapterLock){
            if(sMoviesSyncAdapter==null){
                sMoviesSyncAdapter=new MoviesSyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return sMoviesSyncAdapter.getSyncAdapterBinder();
    }
}
