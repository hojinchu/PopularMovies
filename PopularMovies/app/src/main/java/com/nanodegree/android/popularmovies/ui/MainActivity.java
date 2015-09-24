package com.nanodegree.android.popularmovies.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.Utils;
import com.nanodegree.android.popularmovies.data.MoviesContract;
import com.nanodegree.android.popularmovies.sync.MoviesSyncAdapter;

// Add 15.09.07 14:43 태블릿 화면위해 상속 추가
public class MainActivity extends AppCompatActivity implements MovieListingFragment.Callback,MovieDetailFragment.ActivityToFragment {

    //Add 15.9.8 11:05
    private static final String DETAIL_TAG="DTAG";          //태블릿에 디테일부분이 나옴.
    public static boolean showFavorites=false;
    public static Uri mUri=null;                            //태블릿에 디테일부분이 나오기에 필요함.

    public static boolean mTwoPane=false;      //for SmartPhone 인지 태블릿인지 판별 플래그 용도. Edit private=>public으로 수정.
    private MovieListingFragment movieListingFragment;
    private String mSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Add 15.09.03 11:18
        Log.d("천만 영화들", "OnCreate()");

        setContentView(R.layout.activity_main);

        //Add 15.8.1 21:30
        MoviesSyncAdapter.initializeSyncAdapter(this);
        mSortOrder= Utils.getPreferredSortOrder(this);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);   //android.support.v7.widget.Toolbar 사용

        //Edit 15.9.4 15:54 태블릿 UI고려하여 수정함.
        if(findViewById(R.id.movie_detail) !=null){
            Log.d("유행하는 영화들", "Two Pane is True");
            /**
             *  2015.8.3 00:35 추가
             */
            mTwoPane=true;
            if(savedInstanceState ==null){
                //Edit 15.9.7 17:39 -태블릿인 경우
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_listing, MovieListingFragment.newInstance()).commit();
                //태블릿에선 디테일부분이 같이 보이므로.
                if(mUri !=null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment, MovieDetailFragment.newInstance()).commit();

                    //Add 15.9.9 17:12
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.video_fragment, MovieVideoFragment.newInstance())
                            .commit();
                    //Add 15.9.14 14:49
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.review_fragment, MovieReviewFragment.newInstance())
                            .commit();

                    String movieId=MoviesContract.MovieEntry.getMovieIdFromURI(mUri);

                    new IsFavoriteTask(this).execute(movieId);
                }



/*                movieListingFragment=new MovieListingFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_listing, new MovieListingFragment()).commit();*/

            }

        }else{
            Log.d("유행하는 영화들", "Two Pane is False");
            //스마트폰의 경우.
            mTwoPane=false;

           // movieListingFragment=new MovieListingFragment();          Edit 15.9.14 19:24 주석처리함.
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_listing,MovieListingFragment.newInstance())          //Edit 15.9.14 16:24 싱글톤으로 생성.
                    .commit();
        }
    }

    //Add 15.08.02
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("유명한 영화들", "OnResume");
        //여기 구현....
        MoviesSyncAdapter.syncImmediately(this);
        String sortby=Utils.getPreferredSortOrder(this);
        Boolean resort=false;
        if(sortby !=null & !sortby.equals(mSortOrder)){
            resort=true;
            MovieListingFragment movieListingFragment=(MovieListingFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_listing);
            if(movieListingFragment !=null){
                movieListingFragment.onSortOrderChanged();
            }
            mSortOrder=sortby;
        }
/*        if(mTwoPane){
            if(MovieListingFragment.mPosition != GridView.INVALID_POSITION && resort==false)
                new GetFirstMovieTask().execute(MovieListingFragment.mPosition);
             else{
                resort=false;
                new GetFirstMovieTask().execute(0);
            }
        }*/
    }

    //Add 15.08.04
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== DetailActivity.DETAIL_RESULT){
/*            if(MovieListingFragment.mPosition != GridView.INVALID_POSITION){
                movieListingFragment.moveToPosition(MovieListingFragment.mPosition);*/

            //Edit 15.09.14 19:31
            MovieListingFragment movieListingFragment=(MovieListingFragment)getSupportFragmentManager()
                                                        .findFragmentById(R.id.fragment_listing);
            if( movieListingFragment != null && MovieListingFragment.mPosition != GridView.INVALID_POSITION){
                movieListingFragment.moveToPosition(MovieListingFragment.mPosition);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //좋아하는 표시 구현 함수
    @Override
    public void setFab(int drawableId) {
        if(mTwoPane){       //기본갓이 false이므로 태블릿의 경우라면임.
            FloatingActionButton floatingActionButton=(FloatingActionButton)findViewById(R.id.favorite);
            if(floatingActionButton.getVisibility() == View.GONE)
                floatingActionButton.setVisibility(View.VISIBLE);

            floatingActionButton.setImageResource(drawableId);

        }
    }

    //Add 15.09.07 16:48
    private class IsFavoriteTask extends AsyncTask<String, Void, Boolean>{
        private Context context;

        public IsFavoriteTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String movieId=params[0];

            Cursor cursor= context.getContentResolver().query(
                    MoviesContract.MovieEntry.CONTENT_URI,
                    null,
                    MoviesContract.MovieEntry._ID +" =?",
                    new String[]{movieId},
                    null
            );

            if(cursor !=null && cursor.moveToFirst()){
                String isFavorite=cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_FAVORITE));
                if(isFavorite.equals("1")){
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean) setFab(R.drawable.ic_favorite_white_24dp);
            else setFab(R.drawable.ic_favorite_border_white_24dp);
        }
    }

/*    // 비동기 백그라운드 스레드 클래스(영화 정보 받아오는 스레드)
    private class GetFirstMovieTask extends AsyncTask<Integer,Void,Uri>{

        @Override
        protected Uri doInBackground(Integer... params) {

            Uri movieUri=null;
            Cursor movieCursor=getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI, new String[]{MoviesContract.MovieEntry._ID},
                    null, null, Utils.getPreferredSortOrder(getBaseContext()));

            if(movieCursor.moveToPosition(params[0])){
                String id=movieCursor.getString(movieCursor.getColumnIndex(MoviesContract.MovieEntry._ID));
                Log.d("Movie Cursor", id);

                movieUri=MoviesContract.MovieEntry.buildMovieLocationWithId(id);
            }else{
                Log.d("Movie Cursor", "Empty");
            }

            return movieUri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);

            if(uri !=null){
                Bundle bundle=new Bundle();
                bundle.putParcelable(MovieDetailFragment.DETAIL_URI,uri);

                MovieDetailFragment detailFragment=new MovieDetailFragment();
                detailFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container, detailFragment).commit();
            }
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
/*        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);*/

        //Edit 15.9.14 16:32
        switch(item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_favorites:
                if(showFavorites){
                    Toast.makeText(this, getString(R.string.hiding_favorites), Toast.LENGTH_LONG).show();
                    item.setIcon(R.drawable.ic_favorite_white_24dp);
                }else{
                    Toast.makeText(this, getString(R.string.showing_favorites), Toast.LENGTH_LONG).show();
                    item.setIcon(R.drawable.favorite_blue);
                }
                showFavorites = !showFavorites;

                ((MovieListingFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_listing))
                        .switchData(showFavorites);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Add 15.8.5 09:31
    @Override
    public void onItemSelected(Uri backdropPath, Uri uri, String title, int position) {
        Log.d("흥행중인 영화들", uri.toString());

        //Add 15.9.14 16:25
        mUri=uri;

        if(mTwoPane){
/*            Bundle bundle=new Bundle();
            bundle.putParcelable(MovieDetailFragment.DETAIL_URI, uri);

            MovieDetailFragment movieDetailFragment=new MovieDetailFragment();
            movieDetailFragment.setArguments(bundle);*/

            //Edit 15. 9.14 16:26
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment,MovieDetailFragment.newInstance()).commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.video_fragment, MovieVideoFragment.newInstance());

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.review_fragment,MovieReviewFragment.newInstance());

            String movieId=MoviesContract.MovieEntry.getMovieIdFromURI(mUri);
            new IsFavoriteTask(this).execute(movieId);

        }else{
            Intent intent=new Intent(this,DetailActivity.class);
            //intent.putExtra(MovieDetailFragment.DETAIL_URI,uri);
            intent.putExtra(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,backdropPath);
            intent.putExtra(MoviesContract.MovieEntry.COLUMN_TITLE,title);
            startActivityForResult(intent,position);
        }
    }

    //Add 15.09.07 15:47
    public void favorite(View view){
        //new InvertFavoriteTask(this).execute
        new InvertFavoriteTask(this).execute(MoviesContract.MovieEntry.getMovieIdFromURI(mUri));
    }

    //Add 15.09.14 16:50
    private class InvertFavoriteTask extends AsyncTask<String, Void, Boolean>{
        private Context context;

        public InvertFavoriteTask(Context context){
            this.context=context;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            String movieId=params[0];

            Cursor cursor=context.getContentResolver().query(
                    MoviesContract.MovieEntry.CONTENT_URI,
                    null,
                    MoviesContract.MovieEntry._ID+ " =?",
                    new String[]{
                            movieId
                    },
                    null
            );

            if(cursor !=null && cursor.moveToFirst()){
                String isFavorite=
                        cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_FAVORITE));

                if(isFavorite.equals("1")){
                    ContentValues contentValues= new ContentValues();
                    contentValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE,0);
                    context.getContentResolver().update(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            contentValues,
                            MoviesContract.MovieEntry._ID + " =?",
                            new String[]{
                                    movieId
                            }
                    );
                    return false;
                }else{
                    ContentValues contentValues= new ContentValues();
                    contentValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE,1);
                    context.getContentResolver().update(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            contentValues,
                            MoviesContract.MovieEntry._ID + " =?",
                            new String[]{
                                    movieId
                            }
                    );
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean) setFab(R.drawable.ic_favorite_white_24dp);
            else
                setFab(R.drawable.ic_favorite_border_white_24dp);
        }
    }
}





























