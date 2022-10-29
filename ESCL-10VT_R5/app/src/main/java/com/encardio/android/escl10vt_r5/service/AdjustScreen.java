package com.encardio.android.escl10vt_r5.service;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class AdjustScreen {
    /**
     * Width of Screen
     */
    public static int SCREEN_WIDTH = 320;
    /**
     * Height of Screen
     */
    public static int SCREEN_HEIGHT = 480;
    private final int width;
    private final int height;
    private final Context ctx;

    /*Constructor of class*/
    public AdjustScreen(Context context) {
        this.ctx = context;
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay(); // gives default display object.
        width = display.getWidth();
        height = display.getHeight();
    }

    public int getHeightOfScreen() {
        return height;
    }

    public int getWidthOfScreen() {
        return width;
    }
}
