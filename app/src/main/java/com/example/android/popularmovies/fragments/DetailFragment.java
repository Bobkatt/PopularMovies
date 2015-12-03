package com.example.android.popularmovies.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utility;
import com.example.android.popularmovies.adapters.ReviewAdapter;
import com.example.android.popularmovies.adapters.TrailerAdapter;
import com.example.android.popularmovies.data.DataBaseContract.ReviewEntry;
import com.example.android.popularmovies.data.DataBaseContract.TrailerEntry;
import com.example.android.popularmovies.data.DataBaseContract.MovieEntry;
import com.example.android.popularmovies.data.DataBaseHelper;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String baseURL = "http://image.tmdb.org/t/p/w185/";

    public static final String DETAIL_URI = "URI";

    TrailerAdapter mTrailerAdapter;
    ReviewAdapter mReviewAdapter;

    private Uri mDetailUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_RUN_TIME,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_FAVOURITE,
            MovieEntry.COLUMN_OVERVIEW};

    private static final String[] TRAILER_COLUMNS = {
            TrailerEntry.TABLE_NAME + "." + TrailerEntry._ID,
            TrailerEntry.COLUMN_MOVIE_ID,
            TrailerEntry.COLUMN_TRAILER_DETAIL,
            TrailerEntry.COLUMN_TRAILER_URL};

    private static final String[] REVIEW_COLUMNS = {
            ReviewEntry.TABLE_NAME + "." + ReviewEntry._ID,
            ReviewEntry.COLUMN_MOVIE_ID,
            ReviewEntry.COLUMN_AUTHOR,
            ReviewEntry.COLUMN_CONTENT};

    public final static int COL_MOVIE_ID = 1;
    public final static int COL_MOVIE_TITLE = 2;
    public final static int COL_MOVIE_POSTER_PATH = 3;
    public final static int COL_MOVIE_RELEASE_DATE = 4;
    public final static int COL_MOVIE_RUN_TIME = 5;
    public final static int COL_MOVIE_RATING = 6;
    public final static int COL_MOVIE_FAVOURITE = 7;
    public final static int COL_MOVIE_OVERVEW = 8;

    public final static int COL_TRAILER_DETAIL = 2;
    public final static int COL_TRAILER_URL = 3;

    public final static int COL_REVIEW_AUTHOR = 2;
    public final static int COL_REVIEW_CONTENT = 3;

    private static final String sMovieTrailers =
            TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sMovieReviews =
            ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    private TextView mTitle, mReleaseDate, mRunTime, mRating, mOverview;
    private ImageView mPoster;
    private Button mFavourite;
    private ListView mTrailers, mReviews;

    int mMovieId;

    public DetailFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        if (arguments != null)
        {
            mDetailUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitle = (TextView) rootView.findViewById(R.id.text_view_title);
        mPoster = (ImageView) rootView.findViewById(R.id.image_view_detail_poster);
        mReleaseDate = (TextView) rootView.findViewById(R.id.text_view_release_date);
        mRunTime = (TextView) rootView.findViewById(R.id.text_view_run_time);
        mRating = (TextView) rootView.findViewById(R.id.text_view_rating);
        mFavourite = (Button) rootView.findViewById(R.id.button_favorite);
        mFavourite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                toggleFavourite();
            }
        });
        mOverview = (TextView) rootView.findViewById(R.id.text_view_overview);
        mTrailers = (ListView) rootView.findViewById(R.id.list_view_trailer);
        mReviews = (ListView) rootView.findViewById(R.id.list_view_review);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle)
    {
        if (null != mDetailUri)
        {
            return new CursorLoader(getActivity(), mDetailUri, DETAIL_COLUMNS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (data != null && data.moveToFirst())
        {
            mMovieId = data.getInt(COL_MOVIE_ID);
            String movieTitle = data.getString(COL_MOVIE_TITLE);
            mTitle.setText(movieTitle);
            Picasso.with(getActivity()).load(baseURL + data.getString(COL_MOVIE_POSTER_PATH))
                    .into(mPoster);
            String releaseDate = data.getString(COL_MOVIE_RELEASE_DATE);
            mReleaseDate.setText(releaseDate);
            String runTime = data.getString(COL_MOVIE_RUN_TIME) +"min";
            mRunTime.setText(runTime);
            String rating = data.getString(COL_MOVIE_RATING) + "/10";
            mRating.setText(rating);
            String favouriteText = Utility.formatFavouriteText(getActivity(),
                    data.getInt(COL_MOVIE_FAVOURITE));
            mFavourite.setText(favouriteText);
            String overview = data.getString(COL_MOVIE_OVERVEW);
            mOverview.setText(overview);
            populateExtras(TrailerEntry.TABLE_NAME, TRAILER_COLUMNS, sMovieTrailers, mTrailers);
            populateExtras(ReviewEntry.TABLE_NAME, REVIEW_COLUMNS, sMovieReviews, mReviews);
        }
    }

    void toggleFavourite()
    {
        int isFavourite = 0;
        String favText = mFavourite.getText().toString();
        String addToFav = getActivity().getString(R.string.add_to_favorites);
        String removeFromFav = getActivity().getString(R.string.remove_from_favourites);
        if(favText.equals(addToFav))
        {
            mFavourite.setText(removeFromFav);
            isFavourite = 1;
        }
        else
        {
            mFavourite.setText(addToFav);
            isFavourite = 0;
        }
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_FAVOURITE, isFavourite);
        ContentResolver resolver = getContext().getContentResolver();
        resolver.update(MovieEntry.CONTENT_URI, values, MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(mMovieId)});
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
    }

    void populateExtras(String uri, String[] projection, String selection, ListView listview)
    {
        DataBaseHelper mDBHelper = new DataBaseHelper(getActivity());
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        try
        {
            Cursor cursor = db.query(uri, projection, selection,
                    new String[]{Integer.toString(mMovieId)}, null, null, null);
            switch(uri)
            {
                case TrailerEntry.TABLE_NAME:
                {
                    mTrailerAdapter = new TrailerAdapter(getActivity(),cursor , 0);
                    listview.setAdapter(mTrailerAdapter);
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
                        {
                            Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                            if (cursor != null)
                            {
                                openYouTubeTrailerLink(cursor.getString(COL_TRAILER_URL));
                            }
                        }
                    });
                    break;
                }
                case ReviewEntry.TABLE_NAME:
                {
                    mReviewAdapter = new ReviewAdapter(getActivity(),cursor , 0);
                    listview.setAdapter(mReviewAdapter);
                    break;
                }
                default:
                {
                    Log.e(LOG_TAG, "Undefined Extra Type");
                    break;
                }
            }
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, e.toString());
        }
        db.close();
        Utility.setListViewHeightBasedOnItems(listview);
    }

    private void openYouTubeTrailerLink(String url)
    {
        Uri uri = Uri.parse(getActivity().getString(R.string.youtube_base_url) + url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
        {
            startActivity(intent);
        }
        else
        {
            Log.d(LOG_TAG, "Couldn't call " + uri.toString() + ", no receiving apps installed!");
        }

    }


}
