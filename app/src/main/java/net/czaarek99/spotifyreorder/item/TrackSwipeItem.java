package net.czaarek99.spotifyreorder.item;

import android.content.Context;
import android.util.AttributeSet;

import com.woxthebox.draglistview.swipe.ListSwipeItem;

/**
 * Created by Czarek on 2017-04-06.
 */

public class TrackSwipeItem extends ListSwipeItem {

    public TrackSwipeItem(Context context) {
        super(context);
        init();
    }

    public TrackSwipeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrackSwipeItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setSupportedSwipeDirection(SwipeDirection.LEFT);
    }
}
