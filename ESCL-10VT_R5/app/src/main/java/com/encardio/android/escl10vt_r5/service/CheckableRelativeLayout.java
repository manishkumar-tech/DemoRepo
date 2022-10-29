package com.encardio.android.escl10vt_r5.service;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sandeep
 * <p>
 * Extension of a relative layout to provide a checkable behavior.
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    /**
     * The ischecked to check the checked item.
     */
    private boolean isChecked;
    /**
     * The checkable views.
     */
    private List<Checkable> checkableViews;

    /**
     * Instantiates a new checkable relative layout.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialise(attrs);
    }

    /**
     * Instantiates a new checkable relative layout.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(attrs);
    }

    /**
     * Instantiates a new checkable relative layout.
     *
     * @param context     the context
     * @param checkableId the checkable id
     */
    public CheckableRelativeLayout(Context context, int checkableId) {
        super(context);
        initialise(null);
    }

    /*
     * @see android.widget.Checkable#isChecked()
     */
    /*
     * (non-Javadoc)
     *
     * @see android.widget.Checkable#isChecked()
     */
    public boolean isChecked() {
        return isChecked;
    }

    /*
     * @see android.widget.Checkable#setChecked(boolean)
     */
    /*
     * (non-Javadoc)
     *
     * @see android.widget.Checkable#setChecked(boolean)
     */
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        for (Checkable c : checkableViews) {
            c.setChecked(isChecked);
        }
    }

    /*
     * @see android.widget.Checkable#toggle()
     */
    /*
     * (non-Javadoc)
     *
     * @see android.widget.Checkable#toggle()
     */
    public void toggle() {
        this.isChecked = !this.isChecked;
        for (Checkable c : checkableViews) {
            c.toggle();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onFinishInflate()
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            findCheckableChildren(this.getChildAt(i));
        }
    }

    /**
     * Read the custom XML attributes.
     *
     * @param attrs the attrs
     */
    private void initialise(AttributeSet attrs) {
        this.isChecked = false;
        this.checkableViews = new ArrayList<Checkable>(5);
    }

    /**
     * Add to our checkable list all the children of the view that implement the interface Checkable.
     *
     * @param v
     */
    private void findCheckableChildren(View v) {
        if (v instanceof Checkable) {
            this.checkableViews.add((Checkable) v);
        }
        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            final int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                findCheckableChildren(vg.getChildAt(i));
            }
        }
    }
}
