package com.nanodegree.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nanodegree.android.popularmovies.R;

/**
 * Created by hojin on 15. 7. 31.
 */
public class Utils {
    // 사용자 설정으로부터 언급된 정렬된 순서를 획득
    public static String getPreferredSortOrder(Context context){
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_order_key), context.getString(R.string.pref_order_popularity));
    }
}
