package rs.ac.su.vts.vp.stopper;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;


public class VolumeChangeObserver extends ContentObserver {
    int prevVolume;
    Context context;
    int keyPressed=0;
    MainActivity mainA;

        public VolumeChangeObserver(Context c, Handler handler, MainActivity mainActivity) {
            super(handler);
            context=c;
            this.mainA=mainActivity;
            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            prevVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            int diff= prevVolume -currentVolume;
            MainActivity.statikTest();
            if(diff>0)
            {
                Log.d("stopper_verbose","vol down!");
                keyPressed=2;
                prevVolume =currentVolume;
                mainA.onResetClick();
            }
            else if(diff<0)
            {
                Log.d("stopper_verbose", "vol up!");
                keyPressed=1;
                prevVolume =currentVolume;
                mainA.onStartClick();

            }
        }

        public void setKeyPressed() {
            keyPressed=0;
        }

        public int getKeyPressed() {
            return keyPressed;
        }
}