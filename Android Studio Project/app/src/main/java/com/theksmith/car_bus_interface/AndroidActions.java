package com.theksmith.car_bus_interface;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;


/**
 * singleton helper class for performing common actions on android device
 *
 * @author Kristoffer Smith <kristoffer@theksmith.com>
 */
public class AndroidActions {
    private static final String TAG = "AndroidActions";
    private static final boolean D = BuildConfig.SHOW_DEBUG_LOG;

    private static AndroidActions mInstance = null;

    private final Context mContext;
    private final boolean mSilentErrors;


    private AndroidActions(final Context context, boolean silentErrors) {
        mContext = context.getApplicationContext();
        mSilentErrors = silentErrors;
    }

    public static AndroidActions getInstance(final Context appContext, final boolean silentErrors){
        if(mInstance == null) {
            mInstance = new AndroidActions(appContext, silentErrors);
        }
        return mInstance;
    }

    /**
     * show a toast notification
     * @param text  the message to show
     */
    public void sysAlert(final String text) {
        if (D) Log.d(TAG, "sysAlert() : text= " + text);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) {}
            }
        });
    }

    /**
     * execute a shell command
     * @param command  the command string. if root is required, begin with "su -c"
     */
    public void sysExecuteCommand(final String command) {
        if (D) Log.d(TAG, "sysExecuteCommand() : command= " + command);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec(command);
                } catch (Exception e) {
                    Log.e(TAG, "doAction() : unexpected exception : exception= " + e.getMessage(), e);

                    if (!mSilentErrors) {
                        final String text = mContext.getApplicationInfo().name + ": " + mContext.getResources().getString(R.string.msg_app_error_executing_command) + " " + command;
                        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * simulate a device/keyboard button (requires root)
     * @param keyCode  one of the android.view.KeyEvent.KEYCODE_* constants
     */
    public void sysSimulateButton(final int keyCode) {
        if (D) Log.d(TAG, "sysSimulateButton() : keyCode= " + keyCode);

        sysExecuteCommand("su -c input keyevent " + keyCode);
    }

    /**
     * simulate a MEDIA device/keyboard button via root or non-root method
     * @param keyCode  any of the android.view.KeyEvent.KEYCODE_MEDIA_* constants (must be a MEDIA one)
     * @param useRootMethod  root method works most consistently, otherwise may not work correctly when multiple media players are present (gives focus to the default one)
     */
    public void sysSimulateMediaButton(final int keyCode, final boolean useRootMethod) {
        if (D) Log.d(TAG, "sysSimulateMediaButton() : keyCode= " + keyCode + " useRootMethod= " + useRootMethod);

        if (useRootMethod) {
            try {
                sysSimulateButton(keyCode);
            } catch (Exception ignored) {}
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try{
                        final long now = SystemClock.uptimeMillis();

                        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                        KeyEvent event = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0);
                        intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
                        mContext.sendOrderedBroadcast(intent, null);

                        intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                        event = new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0);
                        intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
                        mContext.sendOrderedBroadcast(intent, null);
                    } catch (Exception e) {
                        Log.e(TAG, "sysSimulateMediaButton() : unexpected exception : exception= " + e.getMessage(), e);

                        if (!mSilentErrors) {
                            final String text = mContext.getApplicationInfo().name + ": " + mContext.getResources().getString(R.string.msg_app_error_simulating_media_btn) + " " + keyCode;
                            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    public void audioVolumeUp(final boolean visible) {
        if (D) Log.d(TAG, "audioVolumeUp() : visible= " + visible);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    final AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, (visible ? AudioManager.FLAG_SHOW_UI : 0));
                } catch (Exception e) {
                    Log.e(TAG, "audioVolumeUp() : unexpected exception : exception= " + e.getMessage(), e);

                    if (!mSilentErrors) {
                        final String text = mContext.getApplicationInfo().name + ": " + mContext.getResources().getString(R.string.msg_app_error_changing_volume);
                        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void audioVolumeDown(final boolean visible) {
        if (D) Log.d(TAG, "audioVolumeDown() : visible= " + visible);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    final AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, (visible ? AudioManager.FLAG_SHOW_UI : 0));
                } catch (Exception e) {
                    Log.e(TAG, "audioVolumeDown() : unexpected exception : exception= " + e.getMessage(), e);

                    if (!mSilentErrors) {
                        final String text = mContext.getApplicationInfo().name + ": " + mContext.getResources().getString(R.string.msg_app_error_changing_volume);
                        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}