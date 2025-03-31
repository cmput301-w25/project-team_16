/**
 * MapScrollContainer.java
 *
 * A custom FrameLayout used to allow smooth interaction between a scrollable container
 * (like ScrollView or RecyclerView) and an embedded Google Map view inside a Fragment or Activity.
 *
 * Purpose:
 * - Solves gesture conflicts between a map and parent scrollable layouts.
 * - Prevents the parent container (e.g., ScrollView) from intercepting touch events while
 *   interacting with the map (e.g., panning or zooming).
 *
 * How it works:
 * - Overrides `onInterceptTouchEvent()` to detect touch actions.
 * - When the user touches down or moves on the map area, it disables parent interception of touch events.
 * - When the user lifts their finger or cancels the interaction, it re-enables parent interception.
 *
 * Use Case:
 * - Wrap the Google Map fragment or view inside this container when the map is inside a scrollable view.
 * - Typically used in layouts that mix scrollable UI and interactive map components.
 *
 */

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
