package com.leocardz.silence.please.custom.typeface;

import android.graphics.Typeface;

import com.leocardz.silence.please.utils.Manager;

public class OpenSans {

    private static OpenSans instance;
    private Typeface typeface;

    public OpenSans() {
    }

    public static OpenSans getInstance() {
        synchronized (OpenSans.class) {
            if (instance == null)
                instance = new OpenSans();
            return instance;
        }
    }

    public Typeface getTypeFace() {
        if (typeface == null) {
            typeface = Typeface.createFromAsset((Manager.getInstance().getContext()
                            .getApplicationContext()).getResources().getAssets(),
                    "open_sans_light.ttf");
        }
        return typeface;
    }
}
