package com.example.android.popularmovies.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utility;
import com.example.android.popularmovies.adapters.SyncAdapter;
import com.example.android.popularmovies.fragments.DetailFragment;
import com.example.android.popularmovies.fragments.MainFragment;

public class MainActivity extends AppCompatActivity implements MainFragment.Callback
{
    private boolean mTwoPane;
    String mSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mSortOrder = Utility.getPreferredSortOrder(this);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null)
        {
            mTwoPane = true;
            if (savedInstanceState == null)
            {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container,
                                new DetailFragment(),
                                DetailFragment.LOG_TAG)
                        .commit();
            }
        }
        else
        {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        SyncAdapter.initializeSyncAdapter(this);
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        String sortBy = Utility.getPreferredSortOrder(this);
        if(sortBy != null && !sortBy.equals(mSortOrder))
        {
            MainFragment mainFragment =
                    (MainFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
            if(null != mainFragment)
            {
                mainFragment.refreshPosterGrid();
            }
            mSortOrder = sortBy;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri)
    {
        if (mTwoPane)
        {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, contentUri);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DetailFragment.LOG_TAG)
                    .commit();
        }
        else
        {
            Intent intent = new Intent(this, DetailActivity.class).setData(contentUri);
            startActivity(intent);
        }
    }
}
