package com.example.android.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncAuthenticatorService extends Service
{
    private SyncAuthenticator mAuthenticator;

    @Override
    public void onCreate()
    {
        mAuthenticator = new SyncAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mAuthenticator.getIBinder();
    }

}
