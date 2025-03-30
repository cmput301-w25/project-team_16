// com/example/team_16/ui/views/MapScrollContainer.java

package com.example.team_16.ui.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;


public class MapScrollContainer extends FrameLayout {

    public MapScrollContainer(Context context) {
        super(context);
    }

    public MapScrollContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapScrollContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return false;
    }
}
