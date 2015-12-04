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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

    public static final String DETAIL_URI = "URI";

    private static final String POPULAR_MOVIE_SHARE_HASHTAG = " #PopularMovies";
    private ShareActionProvider mShareActionProvider;
    private String mMovieTrailer;

    TrailerAdapter mTrailerAdapter;
    ReviewAdapter mReviewAdapter;

    private Uri mDetailUri;

    private static final int DETAIL_LOADER = 0;

    //projection for the movie detail
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

    //projection for trailers
    private static final String[] TRAILER_COLUMNS = {
            TrailerEntry.TABLE_NAME + "." + TrailerEntry._ID,
            TrailerEntry.COLUMN_MOVIE_ID,
            TrailerEntry.COLUMN_TRAILER_DETAIL,
            TrailerEntry.COLUMN_TRAILER_URL};

    //projection for reviews
    private static final String[] REVIEW_COLUMNS = {
            ReviewEntry.TABLE_NAME + "." + ReviewEntry._ID,
            ReviewEntry.COLUMN_MOVIE_ID,
            ReviewEntry.COLUMN_AUTHOR,
            ReviewEntry.COLUMN_CONTENT};

    //static column numbers required for the movie detail projection
    public final static int COL_MOVIE_ID = 1;
    public final static int COL_MOVIE_TITLE = 2;
    public final static int COL_MOVIE_POSTER_PATH = 3;
    public final static int COL_MOVIE_RELEASE_DATE = 4;
    public final static int COL_MOVIE_RUN_TIME = 5;
    public final static int COL_MOVIE_RATING = 6;
    public final static int COL_MOVIE_FAVOURITE = 7;
    public final static int COL_MOVIE_OVERVEW = 8;

    //static column numbers required for the movie trailers projection
    public final static int COL_TRAILER_DETAIL = 2;
    public final static int COL_TRAILER_URL = 3;

    //static column numbers required for the movie reviews projection
    public final static int COL_REVIEW_AUTHOR = 2;
    public final static int COL_REVIEW_CONTENT = 3;

    //Trailers query based on movie id
    private static final String sMovieTrailers =
            TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    //Reviews query based on movie id
    private static final String sMovieReviews =
            ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    //Objects in this fragment
    private TextView mTitle, mReleaseDate, mRunTime, mRating, mOverview;
    private ImageView mPoster;
    private Button mFavourite;
    private ListView mTrailers, mReviews;

    int mMovieId;

    //Constructor
    public DetailFragment()
    {
        setHasOptionsMenu(true);
    }

    //Find and assign variables to the objects for easier ref, also, assign events where required
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Inflate menu resource file.
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if(mMovieTrailer != null)
        {
            Log.d(LOG_TAG, "OnCreateOptionsMenu: " + mMovieTrailer);
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private Intent createShareTrailerIntent()
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovieTrailer + "-" + POPULAR_MOVIE_SHARE_HASHTAG);
//        return shareIntent;
//        startActivity(shareIntent);
        return shareIntent;
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

    //populate objects in activity from completed dbase query
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (data != null && data.moveToFirst())
        {
            mMovieId = data.getInt(COL_MOVIE_ID);
            String movieTitle = data.getString(COL_MOVIE_TITLE);
            mTitle.setText(movieTitle);
            Picasso.with(getActivity()).load(getActivity().getString(R.string.movie_db_poster_url)
                    + data.getString(COL_MOVIE_POSTER_PATH))
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
            //after main detail is complete, populate Trailers, then reviews
            populateExtras(TrailerEntry.TABLE_NAME, TRAILER_COLUMNS, sMovieTrailers, mTrailers);
            populateExtras(ReviewEntry.TABLE_NAME, REVIEW_COLUMNS, sMovieReviews, mReviews);
        }
    }

    //set/reset int in dbase to determine if movie is in favourites
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

    //used to get Trailer and Review Information from database
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
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view,
                                                int position, long l) {
                            Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                            if (cursor != null) {
                                openYouTubeTrailerLink(cursor.getString(COL_TRAILER_URL));
                            }
                        }
                    });
                    Log.d(LOG_TAG, "Adding Share Intent");
//                    get url of first trailer for share trailer intent
                    if(cursor != null)
                    {
                        cursor.moveToFirst();
                        mMovieTrailer = getActivity().getString(R.string.youtube_base_url)
                                + cursor.getString(COL_TRAILER_URL);
                        Log.d(LOG_TAG, "populateExtras: " + mMovieTrailer);
                    }
                    if (mShareActionProvider != null)
                    {
                        mShareActionProvider.setShareIntent(createShareTrailerIntent());
                    }
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

    //Intent using trailer link to display trailer in youtube
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
