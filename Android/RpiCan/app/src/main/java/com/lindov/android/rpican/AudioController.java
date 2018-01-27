package com.lindov.android.rpican;

import android.media.AudioManager;
import android.view.KeyEvent;

/**
 * Created by ilia on 16.01.18.
 */

public class AudioController {
    private AudioManager audioManager;

    public AudioController(RpiCanService service) {
        this.audioManager = service.getAudioManager();
    }

    public void play() {
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }
}
