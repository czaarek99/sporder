package net.czaarek99.spotifyreorder.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.adapter.TrackAdapter;

/**
 * Created by Czarek on 2017-03-24.
 */
public class TrackDragItem extends DragItem {

    private final TrackAdapter trackAdapter;

    public TrackDragItem(Context context, DragListView dragList) {
        super(context);
        this.trackAdapter = (TrackAdapter) dragList.getAdapter();
    }

    @Override
    public void onBindDragView(View clickedView, View dragView) {
        if (trackAdapter.hasSelection()) {
            TextView trackAmount = (TextView)  LayoutInflater.from(dragView.getContext()).inflate(R.layout.multiple_drag_item, null, false);
            trackAmount.setText(dragView.getResources().getString(R.string.track_amount, trackAdapter.getSelectionSize()));

            int widthSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getHeight(), View.MeasureSpec.EXACTLY);

            trackAmount.measure(widthSpec, heightSpec);
            trackAmount.layout(0, 0, trackAmount.getMeasuredWidth(), trackAmount.getMeasuredHeight());

            trackAmount.setBackgroundColor(ContextCompat.getColor(clickedView.getContext(), R.color.translucentBlackLight));

            Bitmap multipleDragLayoutBitmap = Bitmap.createBitmap(trackAmount.getMeasuredWidth(), trackAmount.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas tracksCanvas = new Canvas(multipleDragLayoutBitmap);
            trackAmount.draw(tracksCanvas);

            //noinspection deprecation
            dragView.setBackgroundDrawable(new BitmapDrawable(clickedView.getResources(), multipleDragLayoutBitmap));
        } else {
            TrackAdapter.TrackViewHolder viewHolder = (TrackAdapter.TrackViewHolder) clickedView.getTag();
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
            viewHolder.mainLayout.setBackgroundColor(ContextCompat.getColor(clickedView.getContext(), R.color.translucentBlackLight));

            super.onBindDragView(clickedView, dragView);

            viewHolder.mainLayout.setBackgroundColor(ContextCompat.getColor(clickedView.getContext(), R.color.translucentBlack));
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }
    }
}
