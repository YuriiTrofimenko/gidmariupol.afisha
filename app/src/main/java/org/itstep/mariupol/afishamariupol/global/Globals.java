package org.itstep.mariupol.afishamariupol.global;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import org.itstep.mariupol.afishamariupol.widget.ProgressToast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * класс статических переменных для совместного использования разными активносями
 *(используется переменная mNoEventsString для хранения строки сообщения об отсутствии событий в данный день)
 */
public class Globals {

    private static String mNoEventsString = "";
    private final static Handler mHandler = new Handler();
    private static ProgressToast mProgressToast = null;
    private static Timer mTimer;
    private static boolean isProgressToastShow = false;

    public static void showProgressToastInstance (final Context context) {
        if (isProgressToastShow == false) {
            mProgressToast =
                    ProgressToast.makeText(context, "", Toast.LENGTH_LONG);
            mTimer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    mProgressToast.show();
                    //Log.i("glob", "show");
                }
            };
            mTimer.schedule(timerTask, 0, 4 * 60);
            isProgressToastShow = true;
        }
    }

    public static void closeProgressToast () {
        if (mProgressToast != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mProgressToast != null) {
                        mProgressToast.cancel();
                        mProgressToast = null;
                    }
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                    if (mProgressToast == null && mTimer == null) {
                        isProgressToastShow = false;
                        //Log.i("glob", "close");
                    }
                }
            });
        }
    }

    public static String getNoEventsString() {
        return mNoEventsString;
    }

    public static void setNoEventsString(String noEventsString) {
        Globals.mNoEventsString = noEventsString;
    }
}
