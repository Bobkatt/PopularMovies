package com.example.android.popularmovies.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utility;
import com.example.android.popularmovies.adapters.MainAdapter;
import com.example.android.popularmovies.data.DataBaseContract.MovieEntry;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private MainAdapter mMainAdapter;
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final int MOVIE_LOADER = 0;
    private static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_POSTER_PATH};

    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_POSTER_PATH = 2;

    public MainFragment()
    {
    }

    public interface Callback
    {
        void onItemSelected(Uri movieUri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mMainAdapter = new MainAdapter(getActivity(), null, 0);
        mGridView = (GridView) rootView.findViewById(R.id.grid_view_movie);
        mGridView.setAdapter(mMainAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null)
                {
                    String movieId = cursor.getString(COL_MOVIE_ID);
                    ((Callback) getActivity()).onItemSelected(MovieEntry.buildMovieURL(movieId));
                }
                mPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY))
        {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void refreshPosterGrid()
    {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if(mPosition != GridView.INVALID_POSITION)
        {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        Uri moviePosterUri = MovieEntry.buildMoviesURL();
        String sortBy = Utility.getPreferredSortOrder(getContext());
        int limit = getContext().getResources().getInteger(R.integer.grid_items);
        CursorLoader cursorLoader =
                new CursorLoader(getActivity(), moviePosterUri, MOVIE_COLUMNS, null, null, null);
        switch(sortBy)
        {
            case "Most popular":
            {
                cursorLoader.setSortOrder(MovieEntry.COLUMN_POPULARITY + " DESC LIMIT " + limit);
                break;
            }
            case "Highest rated":
            {
                cursorLoader.setSortOrder(MovieEntry.COLUMN_VOTE_AVERAGE + " DESC LIMIT " + limit);
                break;
            }
            case "Favourite":
            {
                cursorLoader.setSelection(MovieEntry.COLUMN_FAVOURITE + " = 1");
                break;
            }
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        mMainAdapter.swapCursor(data);
        if(mPosition != GridView.INVALID_POSITION)
        {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mMainAdapter.swapCursor(null);
    }
}