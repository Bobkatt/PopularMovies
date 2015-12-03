package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.fragments.DetailFragment;

public class TrailerAdapter extends CursorAdapter
{
    public TrailerAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    }

    public static class ViewHolder
    {
        public final ImageView mPlayTrailer;
        public final TextView mTrailerDetail;
        public ViewHolder(View view)
        {
            mPlayTrailer = (ImageView) view.findViewById(R.id.image_view_play_trailer);
            mTrailerDetail = (TextView) view.findViewById(R.id.text_view_trailer_detail);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        int layoutId = R.layout.list_item_trailer;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String trailerDetail = cursor.getString(DetailFragment.COL_TRAILER_DETAIL);
        viewHolder.mTrailerDetail.setText(trailerDetail);
        viewHolder.mPlayTrailer.setImageResource(R.drawable.youtube);
    }
}
