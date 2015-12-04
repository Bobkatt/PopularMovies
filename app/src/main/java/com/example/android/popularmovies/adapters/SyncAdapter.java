package com.example.android.popularmovies.adapters;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.DataBaseContract.MovieEntry;
import com.example.android.popularmovies.data.DataBaseContract.ReviewEntry;
import com.example.android.popularmovies.data.DataBaseContract.TrailerEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    public final String LOG_TAG = SyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 180;   // seconds * minutes, sync interval
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final String STR_REVIEWS = "reviews";
    private static final String STR_TRAILERS = "videos";
    private static final String STR_MOVIE_TIME = "movies";
    private static final String SORT_BY = "sort_by";
    private static final String API_KEY = "api_key";

    final String STR_POPULAR = getContext().getString(R.string.movie_most_popular);
    final String STR_RATING = getContext().getString(R.string.movie_rating);
    final String STR_SORT_BY_URL = getContext().getString(R.string.movie_sort_by_url);
    final String STR_DETAIL_URL = getContext().getString(R.string.movie_detail_url);

    public SyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
    }

    //get JSON String for the 2 types of movie lists
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult)
    {
        getApiString(STR_SORT_BY_URL, STR_POPULAR, "");
        getApiString(STR_SORT_BY_URL, STR_RATING, "");
    }

    //download Json string for the selected movie list from themoviedb.org
    public void getApiString(String baseUrl, String searchOption, String searchType)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJsonStr = null;
        String apiKey = getContext().getString(R.string.api_key);
        try
        {
            Uri builtUri;
            //build the query string based on the request
            switch(searchType)
            {
                case STR_MOVIE_TIME:
                {
                    builtUri = Uri.parse(baseUrl).buildUpon().appendPath(searchOption)
                            .appendQueryParameter(API_KEY, apiKey).build();
                    break;
                }
                case STR_TRAILERS:
                {
                    builtUri = Uri.parse(baseUrl).buildUpon().appendPath(searchOption)
                            .appendPath(searchType).appendQueryParameter(API_KEY, apiKey).build();
                    break;
                }
                case STR_REVIEWS:
                {
                    builtUri = Uri.parse(baseUrl).buildUpon().appendPath(searchOption)
                            .appendPath(searchType).appendQueryParameter(API_KEY, apiKey).build();
                    break;
                }
                default:
                {
                    builtUri = Uri.parse(baseUrl).buildUpon()
                            .appendQueryParameter(SORT_BY, searchOption)
                            .appendQueryParameter(API_KEY, apiKey).build();
                }
            }
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
            {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null)
            {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0)
            {
                return;
            }
            movieJsonStr = buffer.toString();
            if(searchType.equals(""))
            {
                if(searchOption.equals(STR_POPULAR)|| searchOption.equals(STR_RATING))
                {
                    getMovieDataFromJson(movieJsonStr);
                }
                else
                {
                    getMovieDataFromJson(movieJsonStr);
                }
            }
            else
            {
                //pass the result string to the correct routine for parsing into dbase
                switch(searchType)
                {
                    case STR_TRAILERS:
                    {
                        getTrailerDataFromJson(movieJsonStr, Integer.parseInt(searchOption));
                        break;
                    }
                    case STR_REVIEWS:
                    {
                        getReviewDataFromJson(movieJsonStr, Integer.parseInt(searchOption));
                        break;
                    }
                    case STR_MOVIE_TIME:
                    {
                        getMovieRunTimeFromJson(movieJsonStr, Integer.parseInt(searchOption));
                    }
                }
            }
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "getApiString Error ", e);
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (final IOException e)
                {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    //parse Json String into movie table, updating run time for each movie
    private void getMovieRunTimeFromJson (String movieJsonStr, int movieId) throws JSONException
    {
        final String OWM_RUNTIME = "runtime";
        try
        {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            int runTime = 0;
            if(!movieJson.isNull(OWM_RUNTIME))
            {
                runTime = movieJson.getInt(OWM_RUNTIME);
            }
            ContentValues values = new ContentValues();
            values.put(MovieEntry.COLUMN_RUN_TIME, String.valueOf(runTime));
            ContentResolver resolver = getContext().getContentResolver();
            resolver.update(MovieEntry.CONTENT_URI, values,
                    MovieEntry.COLUMN_MOVIE_ID + " = ?", new String[]{String.valueOf(movieId)});
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    //parse Json String into review table, adding reviews for each movie
    private void getReviewDataFromJson (String reviewJsonStr, int movieId) throws JSONException
    {
        final String OWM_RESULTS = "results";
        final String OWM_AUTHOR = "author";
        final String OWM_DETAIL = "content";
        final String OWM_URL = "url";
        try
        {
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(OWM_RESULTS);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(reviewArray.length());
            for (int i = 0; i < reviewArray.length(); i++)
            {
                String author;
                String content;
                String url;
                JSONObject reviewDetail = reviewArray.getJSONObject(i);
                author = reviewDetail.getString(OWM_AUTHOR);
                content = reviewDetail.getString(OWM_DETAIL);
                url = reviewDetail.getString(OWM_URL);
                ContentValues reviewValues = new ContentValues();
                reviewValues.put(ReviewEntry.COLUMN_MOVIE_ID, movieId);
                reviewValues.put(ReviewEntry.COLUMN_AUTHOR, author);
                reviewValues.put(ReviewEntry.COLUMN_CONTENT, content);
                cVVector.add(reviewValues);
            }
            if (cVVector.size() > 0)
            {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(ReviewEntry.CONTENT_URI, cvArray);
            }
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    //parse Json String into trailers table, adding trailers for each movie
    private void getTrailerDataFromJson (String trailerJsonStr, int movieId) throws JSONException
    {
        final String OWM_RESULTS = "results";
        final String OWM_DETAIL = "name";
        final String OWM_KEY = "key";
        try
        {
            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray(OWM_RESULTS);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(trailerArray.length());
            for (int i = 0; i < trailerArray.length(); i++)
            {
                String urlKey;
                String detail;
                JSONObject trailerDetail = trailerArray.getJSONObject(i);
                urlKey = trailerDetail.getString(OWM_KEY);
                detail = trailerDetail.getString(OWM_DETAIL);
                ContentValues trailerValues = new ContentValues();
                trailerValues.put(TrailerEntry.COLUMN_MOVIE_ID, movieId);
                trailerValues.put(TrailerEntry.COLUMN_TRAILER_URL, urlKey);
                trailerValues.put(TrailerEntry.COLUMN_TRAILER_DETAIL, detail);
                cVVector.add(trailerValues);
            }
            if (cVVector.size() > 0)
            {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(TrailerEntry.CONTENT_URI, cvArray);
            }
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    //parse Json String into movie table, adding row for each movie
    private void getMovieDataFromJson (String movieJsonStr) throws JSONException
    {
        final String OWM_RESULTS = "results";
        final String OWM_ID = "id";
        final String OWM_OVERVIEW = "overview";
        final String OWM_RELEASE_DATE = "release_date";
        final String OWM_POSTER_PATH = "poster_path";
        final String OWM_POPULARITY = "popularity";
        final String OWM_TITLE = "title";
        final String OWM_VOTE_AVERAGE = "vote_average";
        try
        {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());
            ArrayList<Integer> movies = new ArrayList<>();
            for (int i = 0; i < movieArray.length(); i++)
            {
                int movieId;
                String overView;
                String releaseDate;
                String posterPath;
                double popularity;
                String movieTitle;
                double voteAverage;
                JSONObject movieDetail = movieArray.getJSONObject(i);
                movieId = movieDetail.getInt(OWM_ID);
                overView = movieDetail.getString(OWM_OVERVIEW);
                releaseDate = movieDetail.getString(OWM_RELEASE_DATE);
                posterPath = movieDetail.getString(OWM_POSTER_PATH);
                popularity = movieDetail.getDouble(OWM_POPULARITY);
                movieTitle = movieDetail.getString(OWM_TITLE);
                voteAverage = movieDetail.getDouble(OWM_VOTE_AVERAGE);
                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, overView);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MovieEntry.COLUMN_TITLE, movieTitle);
                movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MovieEntry.COLUMN_RUN_TIME, 0);
                movieValues.put(MovieEntry.COLUMN_FAVOURITE, 0);
                cVVector.add(movieValues);
                movies.add(movieId);
                getApiString(STR_DETAIL_URL, String.valueOf(movieId), STR_REVIEWS);
                getApiString(STR_DETAIL_URL, String.valueOf(movieId), STR_TRAILERS);
            }
            if (cVVector.size() > 0)
            {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
                for(int i = 0; i < cVVector.size(); i++)
                {
                    getApiString(STR_DETAIL_URL, String.valueOf(movies.get(i)), STR_MOVIE_TIME);
                }
            }
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**Helper method to schedule the sync adapter periodic execution*/
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime)
    {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        }
        else
        {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context)
    {
        AccountManager accountManager = (AccountManager)
                context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));
        if (null == accountManager.getPassword(newAccount) )
        {
            if (!accountManager.addAccountExplicitly(newAccount, "", null))
            {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context)
    {
        /*
         * Since we've created an account
         */
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority), true);
        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context)
    {
        getSyncAccount(context);
    }
}
