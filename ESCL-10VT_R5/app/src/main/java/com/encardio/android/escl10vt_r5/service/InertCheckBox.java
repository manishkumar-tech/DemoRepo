package com.encardio.android.escl10vt_r5.service;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatCheckBox;

/**
 * @author Sandeep
 * The Class InertCheckBox. CheckBox that does not react to any user event in order to let the
 * container handle them.
 */
public class InertCheckBox extends AppCompatCheckBox {
    // Provide the same constructors as the superclass

    /**
     * Instantiates a new inert check box.
     *
     * @param context  the context
     * @param attrs    the attribute set
     * @param defStyle the style
     */
    public InertCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Provide the same constructors as the superclass

    /**
     * Instantiates a new inert check box.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public InertCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Provide the same constructors as the superclass

    /**
     * Instantiates a new inert check box.
     *
     * @param context the context
     */
    public InertCheckBox(Context context) {
        super(context);
    }

    /**
     * @param event
     * @return
     */
    /*
     * (non-Javadoc)
     *
     * @see android.widget.TextView#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Make the checkbox not respond to any user event
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.TextView#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Make the checkbox not respond to any user event
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.TextView#onKeyMultiple(int, int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        // Make the checkbox not respond to any user event
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onKeyPreIme(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // Make the checkbox not respond to any user event
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.TextView#onKeyShortcut(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        // Make the checkbox not respond to any user event
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.TextView#onKeyUp(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Make the checkbox not respond to any user event
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.TextView#onTrackballEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        // Make the checkbox not respond to any user event
        return false;
    }
}
