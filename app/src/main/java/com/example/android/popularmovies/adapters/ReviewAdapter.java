package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.fragments.DetailFragment;

/**
 * {@link ReviewAdapter} exposes a list of reiews for selected movie
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ReviewAdapter  extends CursorAdapter
{
    public ReviewAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    }

    /**Cache of the children views for a reviews list item.*/
    public static class ViewHolder
    {
        public final TextView mAuthor;
        public final TextView mContent;
        public ViewHolder(View view)
        {
            mAuthor = (TextView) view.findViewById(R.id.text_view_review_author);
            mContent = (TextView) view.findViewById(R.id.text_view_review_content);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        int layoutId = R.layout.list_item_review;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*This is where we fill-in the views with the contents of the cursor.*/
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String author = cursor.getString(DetailFragment.COL_REVIEW_AUTHOR);
        String content = cursor.getString(DetailFragment.COL_REVIEW_CONTENT);
        viewHolder.mAuthor.setText(author);
        viewHolder.mContent.setText(content);
    }
}
