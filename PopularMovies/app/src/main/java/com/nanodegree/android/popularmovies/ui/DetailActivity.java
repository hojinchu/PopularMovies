package com.nanodegree.android.popularmovies.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.data.MoviesContract;

public class DetailActivity extends AppCompatActivity implements MovieDetailFragment.ActivityToFragment{

    private final int NUM_PAGES=3;
    public static final int DETAIL_RESULT=100;

    //Add 15.09.14 18:03
    private FloatingActionButton floatingActionButton;

    public void favorite(View view){
        String[] movieId=
            new String[] {
                    MoviesContract.MovieEntry.getMovieIdFromURI(MainActivity.mUri)
            };
        new AlterFavoriteTask(this).execute(movieId);
    }

    public class AlterFavoriteTask extends AsyncTask<String, Void, Boolean>{
        private Context context;

        public AlterFavoriteTask(Context context){
            this.context=context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean favorite=false;
            String movieId=params[0];
            Log.d("흥행 영화들", "ID = " + movieId);

            Cursor cursor=context.getContentResolver().query(
                    MoviesContract.MovieEntry.CONTENT_URI,
                    null,
                    MoviesContract.MovieEntry._ID + " =?",
                    new String[]{
                            movieId
                    },
                    null
            );

            Log.d("흥행하는 영화들", "Cursor Count = " + cursor.getCount());

            cursor.moveToFirst();
            int isFavorite=cursor.getInt(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_FAVORITE));

            cursor.close();;

            ContentValues contentValues;

            switch(isFavorite){
                case 0:
                    contentValues=new ContentValues();
                    contentValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 1);
                    context.getContentResolver().update(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            contentValues,
                            MoviesContract.MovieEntry._ID + " =?",
                            new String[]{
                                    movieId
                            }
                    );
                    break;
                case 1:
                    contentValues=new ContentValues();
                    contentValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 0);
                    context.getContentResolver().update(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            contentValues,
                            MoviesContract.MovieEntry._ID + " =?",
                            new String[]{
                                    movieId
                            }
                    );
                    //Add 15.9.15 01:35
                    favorite=true;
                    break;
                default:
                    break;
            }

            return favorite;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                floatingActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            }else{
                floatingActionButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        setResult(DETAIL_RESULT, new Intent());

        CoordinatorLayout rootLayout=(CoordinatorLayout) findViewById(R.id.main_content);

        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_chevron_left_white_24dp));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        CollapsingToolbarLayout collapsingToolbarLayout=
                (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        //타이틀 나타냬기
        collapsingToolbarLayout.setTitle(getIntent().getStringExtra(MoviesContract.MovieEntry.COLUMN_TITLE));

        ImageView backdrop=(ImageView)findViewById(R.id.backdrop);
        Uri backdropUri=getIntent().getParcelableExtra(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH);
        //상단에 이미지 표시하기
        Glide.with(this).load(backdropUri).centerCrop().into(backdrop);

        //우선 Hello blank fragment 나오게 하기.
        ViewPager viewPager=(ViewPager)findViewById(R.id.detailPager);
        viewPager.setAdapter(new FragmentPager(getSupportFragmentManager()));

        ((TabLayout)findViewById(R.id.detailTabs)).setupWithViewPager(viewPager);

        floatingActionButton=(FloatingActionButton)findViewById((R.id.favorite));
    }

    @Override
    public void setFab(int drawableId) {
        floatingActionButton.setImageResource(drawableId);
    }

    public class FragmentPager extends FragmentPagerAdapter {

        public FragmentPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return MovieDetailFragment.newInstance();
                case 1:
                    return MovieVideoFragment.newInstance();
                case 2:
                    return MovieReviewFragment.newInstance();
                default:
                    return null;
            }

            //return MovieDetailFragment.newInstance(getIntent().getExtras());
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.title_detail_information);
                case 1:
                    return getString(R.string.title_detail_videos);
                case 2:
                    return getString(R.string.title_detail_reviews);
                default:
                    return null;
            }

            //return super.getPageTitle(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     *
     *
     *
     */
    //여기서부터 오후에 먼저 마저 구현하고 리펙토링하자.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
























