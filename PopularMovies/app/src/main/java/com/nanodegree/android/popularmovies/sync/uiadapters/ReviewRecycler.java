package com.nanodegree.android.popularmovies.sync.uiadapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.data.MoviesContract;

/**
 * Created by hojin on 15. 9. 14.
 */
public class ReviewRecycler extends CursorRecyclerAdapter<ReviewRecycler.ViewHolder>{
    private Context context;

    public ReviewRecycler(Context context, Cursor cursor) {
        super(cursor);
        this.context=context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public final TextView author;
        public final TextView content;
        public final TextView url;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Uri address = Uri.parse(url.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, address);
                    if (intent.resolveActivity(v.getContext().getPackageManager()) != null) {
                        v.getContext().startActivity(intent);
                    }
                }
            });
            author=(TextView)itemView.findViewById(R.id.author);
            content=(TextView)itemView.findViewById(R.id.content);
            url=(TextView)itemView.findViewById(R.id.url);

        }
    }




    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        String author=cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_AUTHOR));
        String content=cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT));
        String url=cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_URL));

        holder.author.setText(author);
        holder.url.setText(url);

        if(content.length()>100){
            holder.content.setText(context.getString(R.string.content_shorten, content.substring(0,99)));
        }else{
            holder.content.setText(content);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review,parent,false);
        return new ViewHolder(view);
    }

    //정확한 용도 확인.
    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        Cursor cursor=getmCursor();
        cursor.moveToPosition(holder.getLayoutPosition());
        String author=cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_AUTHOR));
        String content=cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT));
        String url=cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_URL));

        holder.author.setText(author);
        holder.url.setText(url);

        if(content.length() >100){
            holder.content.setText(context.getString(R.string.content_shorten, content.substring(0,99)));
        }else{
            holder.content.setText(content);
        }
        super.onViewAttachedToWindow(holder);
    }
}
































