package org.wintrisstech.erik.iaroc;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener
{

    private static final String ACTION_PLAY = "com.example.action.PLAY";

    MediaPlayer mMediaPlayer = null;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)
    {
        if (intent.getAction().equals(ACTION_PLAY))
        {
            mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hotrod);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        }

        return 0;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(final MediaPlayer player)
    {
        player.start();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
