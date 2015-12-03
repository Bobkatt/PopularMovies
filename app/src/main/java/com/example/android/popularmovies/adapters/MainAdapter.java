package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.fragments.MainFragment;
import com.squareup.picasso.Picasso;

public class MainAdapter extends CursorAdapter
{
    private String baseURL = mContext.getString(R.string.movie_db_poster_url);

    public MainAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    }

    public static class ViewHolder
    {
        public final ImageView moviePoster;
        public ViewHolder(View view)
        {
            moviePoster = (ImageView) view.findViewById(R.id.image_view_movie_poster);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String posterUrl = cursor.getString(MainFragment.COL_MOVIE_POSTER_PATH);
        Picasso.with(context).load(baseURL + posterUrl).into(viewHolder.moviePoster);
    }
}
