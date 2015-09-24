package com.nanodegree.android.popularmovies.ui;


import android.database.Cursor;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String DETAIL_URI="URI";

    private static final int DETAIL_LOADER=1;       //Edit
    private static final String[] DETAIL_COLUMNS={
            MoviesContract.MovieEntry.TABLE_NAME +"."+ MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_POPULARITY,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_FAVORITE       //Add 15.9.8 12:23
    };
    //Add 15.9.8 12:25-리뷰는 해당 영화에 대한 리뷰이므로 관련 칼럼을 함께 초기화 함.
    private static final String[] REVIEW_COLUMNS={
            MoviesContract.ReviewEntry.TABLE_NAME +"."+ MoviesContract.ReviewEntry._ID,
            MoviesContract.ReviewEntry.COLUMN_MOVIE_ID,
            MoviesContract.ReviewEntry.COLUMN_AUTHOR,
            MoviesContract.ReviewEntry.COLUMN_CONTENT,
            MoviesContract.ReviewEntry.COLUMN_URL
    };

    private ImageView poster;
    private ImageView backdrop;

    private TextView title;

    //Edit 15.9.8 12:39
    public static MovieDetailFragment newInstance() {
        return new MovieDetailFragment();
    }

    //Add 15.9.7 14:44
    public interface ActivityToFragment{
        void setFab(int drawableId);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // 무비디테일프래그먼트에 채워질 뷰들을 초기화한다.
        //Edit 15.9.8 12:41
        mUri=MainActivity.mUri;
        Log.d("URI", mUri.toString());

        poster=(ImageView)rootView.findViewById(R.id.movie_poster);
        title=(TextView)rootView.findViewById(R.id.movie_title);
        releaseDate=(TextView)rootView.findViewById(R.id.movie_release_date);
        rating=(TextView)rootView.findViewById(R.id.movie_rating);
        overview=(TextView)rootView.findViewById(R.id.movie_overview);
        return (rootView);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mUri !=null){
            Log.d("흥행하는 영화들", "Detail Loader Created");
            return new CursorLoader(getActivity(),mUri,DETAIL_COLUMNS,MoviesContract.MovieEntry._ID +" =?",
                                    new String[]{MoviesContract.MovieEntry.getMovieIdFromURI(mUri)},null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data !=null && data.moveToFirst()){
            //포스터 나타내기.
            Uri posterUri= Uri.parse(data.getString(COL_MOVIE_POSTER));
            Picasso.with(getActivity().getBaseContext()).load(getString(R.string.base_movieposter_url,posterUri)).into(poster);

            //타이틀 나타내기.
            String titleInfo=data.getString(COL_MOVIE_TITLE);
            title.setText(titleInfo);

            //오버뷰정보 나타내기.
            String overviewInfo=data.getString(COL_MOVIE_OVERVIEW);
            overview.setText(overviewInfo);

            //평점 나타내기
            String ratingInfo=data.getString(COL_MOVIE_VOTE);
            rating.setText(getString(R.string.movie_rating,ratingInfo));

            //릴리즈 날짜 나타내기.
            String releaseInfo=data.getString(COL_MOVIE_RELEASE);
            releaseDate.setText(getString(R.string.release_date, formatDate(releaseInfo)));

            //Add 9.9 14:59
            int favorite=data.getInt(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_FAVORITE));
            if(favorite ==0)
                ((ActivityToFragment)getActivity()).setFab(R.drawable.ic_favorite_border_white_24dp);
            else
                ((ActivityToFragment)getActivity()).setFab(R.drawable.ic_favorite_white_24dp);

        }
    }

    public String formatDate(String date){
        Calendar newDate=new GregorianCalendar(Calendar.getInstance().getTimeZone());

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-mm-dd");

        try{
            Date oldDate=simpleDateFormat.parse(date);
            newDate.setTime(oldDate);
        }catch(ParseException e){
            e.printStackTrace();
        }

        return (newDate.get(Calendar.MONTH)+1) +"/"+
                newDate.get(Calendar.DAY_OF_MONTH) +"/"+
                newDate.get(Calendar.YEAR);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //Add 15.8.5 12:43
    public static MovieDetailFragment newInstance(Bundle arguments){
/*        MovieDetailFragment movieDetailFragment= new MovieDetailFragment();
        movieDetailFragment.setArguments(arguments);
        return(movieDetailFragment);*/
        return new MovieDetailFragment();
    }

    //Add 15.8.5 14:27
    public static final int COL_MOVIE_ID=0;
    public static final int COL_MOVIE_BACKDROP=1;
    public static final int COL_MOVIE_POSTER=2;
    public static final int COL_MOVIE_TITLE=3;
    public static final int COL_MOVIE_OVERVIEW=4;
    public static final int COL_MOVIE_VOTE=5;
    public static final int COL_MOVIE_POPULARITY=6;
    public static final int COL_MOVIE_RELEASE=7;
    //Add 15.9.8 12:35
    public static final int COL_FAVORITE=8;
    public static final int COL_REVIEW_ID=0;
    public static final int COL_REVIEW_MOVIE_ID=1;
    public static final int COL_REVIEW_AUTHOR=2;
    public static final int COL_REVIEW_CONTENT=3;
    public static final int COL_REVIEW_URL=4;

    private TextView releaseDate;
    private TextView rating;
    private TextView overview;
    private Uri mUri;

    //Add 15.8.5 15:30
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }
}




















