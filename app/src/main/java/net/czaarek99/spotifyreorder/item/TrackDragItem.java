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

    private final DragListView dragList;
    private final TrackAdapter trackAdapter;
    private int dragViewHeight = 0;

    public TrackDragItem(Context context, DragListView dragList) {
        super(context);
        this.dragList = dragList;
        this.trackAdapter = (TrackAdapter) dragList.getAdapter();
    }

    //TODO: Drag item for multiple tracks doesn't look too good atm
    @Override
    public void onBindDragView(View clickedView, View dragView) {
        if (trackAdapter.hasSelection()) {
            LinearLayout multipleDragLayout = (LinearLayout) LayoutInflater.from(dragView.getContext()).inflate(R.layout.multiple_drag_item, null, false);
            TextView trackAmount = (TextView) multipleDragLayout.findViewById(R.id.draggingTrackAmountText);
            trackAmount.setText(dragView.getResources().getString(R.string.track_amount, trackAdapter.getSelectionSize()));

            int widthSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getHeight(), View.MeasureSpec.EXACTLY);

            multipleDragLayout.measure(widthSpec, heightSpec);
            multipleDragLayout.layout(0, 0, multipleDragLayout.getMeasuredWidth(), multipleDragLayout.getMeasuredHeight());

            multipleDragLayout.setBackgroundColor(ContextCompat.getColor(clickedView.getContext(), R.color.translucentBlackLight));

            Bitmap multipleDragLayoutBitmap = Bitmap.createBitmap(multipleDragLayout.getMeasuredWidth(), multipleDragLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas tracksCanvas = new Canvas(multipleDragLayoutBitmap);
            multipleDragLayout.draw(tracksCanvas);

            dragViewHeight = multipleDragLayoutBitmap.getHeight();

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

    @Override
    public void onMeasureDragView(View clickedView, View dragView) {
        if(trackAdapter.hasSelection()){
            int measuredHeight = clickedView.getMeasuredHeight();
            int measuredWidth = clickedView.getMeasuredWidth();

            dragView.setLayoutParams(new FrameLayout.LayoutParams(measuredWidth, dragViewHeight));
            int widthSpec = View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(measuredHeight, View.MeasureSpec.EXACTLY);
            dragView.measure(widthSpec, heightSpec);

        } else {
            super.onMeasureDragView(clickedView, dragView);
        }
    }
}
