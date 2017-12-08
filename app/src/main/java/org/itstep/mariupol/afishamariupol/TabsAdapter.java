package org.itstep.mariupol.afishamariupol;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;

import java.util.ArrayList;

/**
 * класс адаптера горизонтального листания и табуляции по дням для главного экрана (галлереи событий)
 */
//наследуем класс пагинации фрагментов, поддерживаем интерфейсы выбора вкладок и пагинации
public class TabsAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

    private final Context mContext;
    private final TabHost mGalleryTabHost;
    private TabWidget mGalleryTabWidget;
    private final HorizontalScrollView mHorizontalScrollView;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private boolean oldDevice = false;

    static final class TabInfo {

        private final String tag;
        private final Class<?> clss;
        private final Bundle args;

        TabInfo(final String _tag, final Class<?> _class, final Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }
    }

    static class DaysTabFactory implements TabHost.TabContentFactory {

        private final Context mContext;

        public DaysTabFactory(final Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(final String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);

            return v;
        }
    }

    public TabsAdapter(final FragmentActivity activity,
                       final TabHost tabHost,
                       final ViewPager pager,
                       final HorizontalScrollView horizontalScrollView) {

        super(activity.getSupportFragmentManager());
        mContext = activity;
        mGalleryTabHost = tabHost;
        mViewPager = pager;
        mGalleryTabHost.setOnTabChangedListener(this);
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
        mHorizontalScrollView = horizontalScrollView;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            oldDevice = true;
        }
    }

    public void addTab(final TabHost.TabSpec tabSpec, final Class<?> clss, final Bundle args) {
        tabSpec.setContent(new DaysTabFactory(mContext));
        String tag = tabSpec.getTag();
        TabInfo info = new TabInfo(tag, clss, args);
        mTabs.add(info);
        mGalleryTabHost.addTab(tabSpec);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        return mTabs.size();
    }

	//при выборе вкладки создаем экземпляр фрагмента, которому передаем аргумент - номер дня
    @Override
    public Fragment getItem(final int position) {
        TabInfo info = mTabs.get(position);
        String dayNumberString = info.tag.toString().substring(info.tag.toString().length() - 1);
        int dayNumberInt = Integer.parseInt(dayNumberString);

        mGalleryTabWidget = mGalleryTabHost.getTabWidget();
        for (int i_tab = 0; i_tab < mGalleryTabWidget.getChildCount(); i_tab++) {
            mGalleryTabWidget.getChildAt(i_tab)
                    .setBackgroundColor(Color.parseColor("#666666"));
        }
        mGalleryTabWidget.getChildAt(mGalleryTabHost.getCurrentTab())
                .setBackgroundColor(Color.parseColor("#303334"));
        EventsGalleryFragment eventsGalleryFragment =
                EventsGalleryFragment.newInstance(dayNumberInt);

        return eventsGalleryFragment;
    }

	//реакция на выбор вкладки по заголовку
    @Override
    public void onTabChanged(final String tabId) {
        int position = mGalleryTabHost.getCurrentTab();
        mViewPager.setCurrentItem(position);
        mGalleryTabWidget = mGalleryTabHost.getTabWidget();
        for (int i_tab = 0; i_tab < mGalleryTabWidget.getChildCount(); i_tab++) {
            mGalleryTabWidget.getChildAt(i_tab)
                    .setBackgroundColor(Color.parseColor("#666666"));
        }
        mGalleryTabWidget.getChildAt(mGalleryTabHost.getCurrentTab())
                .setBackgroundColor(Color.parseColor("#303334"));

    }

	//реакция на выбор вкладки горизонтальной прокруткой области основного контента
    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        int tabWidth = mGalleryTabWidget.getChildAt(0).getWidth();

        if (mHorizontalScrollView != null) {
            if (oldDevice) {
                mHorizontalScrollView.scrollTo((position * (tabWidth)) - 40, 0);
            } else {
                mHorizontalScrollView.scrollTo((position * (tabWidth)) + 5, 0);
            }
        }


    }

    @Override
    public void onPageSelected(final int position) {

        // Unfortunately when TabHost changes the current tab, it kindly
        // also takes care of putting focus on it when not in touch mode.
        // The jerk.
        // This hack tries to prevent this from pulling focus out of our
        // ViewPager.



        int oldFocusability = mGalleryTabWidget.getDescendantFocusability();
        mGalleryTabWidget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mGalleryTabHost.setCurrentTab(position);
        mGalleryTabWidget.setDescendantFocusability(oldFocusability);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
