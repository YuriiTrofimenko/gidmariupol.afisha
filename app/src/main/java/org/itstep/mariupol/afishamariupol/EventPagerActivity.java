package org.itstep.mariupol.afishamariupol;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * класс активности экрана детализации
 */

public class EventPagerActivity extends FragmentActivity {

    private progressableViewPager mViewPager;
    private ArrayList<String> mEventsUrlList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new progressableViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        final Handler handler = new Handler();

		//принимаем из приложения намерения (интента) список всех адресов детализаций,
		//чтобы была возможна пагинация по горизонтали
        mEventsUrlList = getIntent().getStringArrayListExtra(EventPageFragment.EXTRA_EVENTS_LIST);
        FragmentManager fm = getSupportFragmentManager();

		//подключение адаптера пагинации
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {

            @Override
            public void startUpdate(ViewGroup container) {
                /*handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Globals.showProgressToastInstance(getApplicationContext());
                    }
                });*/
                super.startUpdate(container);
            }

            @Override
            public void finishUpdate(ViewGroup container) {
                /*handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Globals.closeProgressToast();
                    }
                });*/
                super.finishUpdate(container);

            }

            @Override
            public int getCount() {
                return mEventsUrlList.size();
            }

            @Override
            public Fragment getItem(int pos) {
                String contentUrl = mEventsUrlList.get(pos);
                EventPageFragment eventPageFragment =
                        EventPageFragment.newInstance(contentUrl);
                return eventPageFragment;
            }
        });

		//получение из приложения намерения адреса страницы сайта с детализацией текущего события
		//и передача его адаптеру
        String contentUrl = (String) getIntent().
                getSerializableExtra(EventPageFragment.EXTRA_EVENT_URL);
        for (int i_event_url = 0; i_event_url < mEventsUrlList.size(); i_event_url++) {
            if (mEventsUrlList.get(i_event_url).equals(contentUrl)) {
                mViewPager.setCurrentItem(i_event_url);
                break;
            }
        }
    }

    private class progressableViewPager extends ViewPager {

        public progressableViewPager(Context context) {
            super(context);
        }

        public progressableViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void addView(View child, int index, ViewGroup.LayoutParams params) {
            //Globals.showProgressToastInstance(getContext());
            //Log.i("asd", "set1");
            super.addView(child, index, params);
        }

        @Override
        public void removeView(View view) {
            //Log.i("asd", "set2");
            //Globals.showProgressToastInstance(getContext());
            //Globals.closeProgressToast();
            super.removeView(view);
        }


    }
}
