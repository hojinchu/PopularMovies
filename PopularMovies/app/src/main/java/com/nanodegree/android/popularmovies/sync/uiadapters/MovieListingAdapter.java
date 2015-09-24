package com.nanodegree.android.popularmovies.sync.uiadapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.data.MoviesContract;
import com.nanodegree.android.popularmovies.ui.MovieListingFragment;
import com.squareup.picasso.Picasso;

/**
 * Created by hojin on 15. 7. 31.
 */
public class MovieListingAdapter extends CursorAdapter {
    public MovieListingAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view=LayoutInflater.from(context).inflate(R.layout.item_movie,parent, false);
        ViewHolder viewHolder=new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    public static class ViewHolder{
        public final ImageView poster;
        //Add 15.9.9 16:39
        public final ImageView favorite;

        public ViewHolder(View view) {

            poster=(ImageView)view.findViewById(R.id.movie_poster);
            favorite=(ImageView)view.findViewById(R.id.favorite_indicator);  //Add 15.9.9 16:59
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Add 15.9.9 17:00
        String favorite=cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_FAVORITE));

        ViewHolder viewHolder=(ViewHolder)view.getTag();
        Picasso.with(context).load(
                context.getString(R.string.base_movieposter_url,cursor.getString(MovieListingFragment.COL_POSTER_URL))
        ).fit().into(viewHolder.poster);

        //Add 15.9.9 17:02
        if(favorite.equals("1"))
            viewHolder.favorite.setVisibility(View.VISIBLE);
        else
            viewHolder.favorite.setVisibility(View.INVISIBLE);
    }
}




















