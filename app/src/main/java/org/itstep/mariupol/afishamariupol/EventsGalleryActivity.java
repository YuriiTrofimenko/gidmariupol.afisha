package org.itstep.mariupol.afishamariupol;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.itstep.mariupol.afishamariupol.model.Day;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * класс активности главного экрана (галлереи событий)
*/

public class EventsGalleryActivity extends FragmentActivity {

	//private static final String TAG = "EventsGalleryActivity";

	private TabHost mGalleryTabHost;
	private TabWidget mGalleryTabWidget;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private TextView mGalleryTabWidgetTextView;
	private HorizontalScrollView mHorizontalScrollView;

	private ArrayList<Day> mDayList;
	private boolean oldDevice = false;

	//переопределяем метод создания активности
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//подключаем представление к активности: контейнер для разметки фрагмента
		setContentView(R.layout.activity_fragment);

		//устанавливаем флаг, если старая версия ОС
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			oldDevice = true;
		}

		//заполняем коллекцию объектов модели "день"
		// порядковыми номерами будущих заголовков закладок, названиями дней и числовыми датами
		mDayList = new ArrayList<Day>();
		Calendar calendar = Calendar.getInstance();

		SimpleDateFormat simpleDayOfWeekNameFormat = new SimpleDateFormat("EEEE");
		SimpleDateFormat simpleDayOfMonthNumberFormat = new SimpleDateFormat("dd");
		SimpleDateFormat simpleMonthNumberFormat = new SimpleDateFormat("MM");

		Date date = calendar.getTime();

		mDayList.add(new Day(0,
				getResources().getString(R.string.events_gallery_tab_today).toUpperCase(),
				"    ->"));
		calendar.add(Calendar.DATE, 1);
		date = calendar.getTime();
		mDayList.add(new Day(1,
				getResources().getString(R.string.events_gallery_tab_tomorrow).toUpperCase(),
				simpleDayOfMonthNumberFormat.format(date)
				+ "." + simpleMonthNumberFormat.format(date) + "    ->"));
		for (int i_day = 2; i_day <= 8; i_day++) {
			calendar.add(Calendar.DATE, 1);
			date = calendar.getTime();
			mDayList.add(new Day(i_day, simpleDayOfWeekNameFormat.format(date).toUpperCase(),
					simpleDayOfMonthNumberFormat.format(date)
					+ "." + simpleMonthNumberFormat.format(date) + ((i_day != 8)?" ->":"")));
		}

		//создаем объекты виджетов хоста закладок, пагинатора, горизонтального скролла,
		//и передаем их в качестве аргументов адаптиеру
		mGalleryTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mGalleryTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.gallery_horizontalScrollView);

		mTabsAdapter = new TabsAdapter(this, mGalleryTabHost, mViewPager, mHorizontalScrollView);

		//создаем вкладки дней и заполняем их заголовки
		for (Day day : mDayList) {
			mTabsAdapter.addTab(
					mGalleryTabHost.newTabSpec(
							"tab_events_" + day.getDayIndex())
							.setIndicator((day.getDayName()
									+ ((day.getDayNumber() != "    ->")
									? (", " + day.getDayNumber())
									: ""))),
								EventsGalleryFragment.class, null);
		}

		//закрашиваем заголвки вкладок, а для старых устройств - устанавливаем их высоту
		mGalleryTabWidget = mGalleryTabHost.getTabWidget();
		for (int i_tab = 0; i_tab < mGalleryTabWidget.getChildCount(); i_tab++) {
			mGalleryTabWidgetTextView = (TextView) mGalleryTabWidget
					.getChildAt(i_tab)
					.findViewById(android.R.id.title);
			mGalleryTabWidgetTextView.setTextColor(Color.parseColor("#43BFD6"));
			if (oldDevice) {
				int height = Integer.parseInt(getResources()
						.getString(R.string.event_tabs_page_tabWidgetHeight));
				mGalleryTabWidget.getChildAt(i_tab).getLayoutParams().height = height;
				mGalleryTabWidgetTextView.getLayoutParams().height = height;
				mGalleryTabWidgetTextView.setGravity(Gravity.CENTER);
				int widhtChild = Integer.parseInt(getResources()
						.getString(R.string.event_tabs_page_tabWidgetChildWidth));
				mGalleryTabWidget.getChildAt(i_tab).getLayoutParams().height = height;
				mGalleryTabWidget.getChildAt(i_tab).setMinimumWidth(widhtChild);
			}
			mGalleryTabWidget.getChildAt(i_tab)
					.setBackgroundColor(Color.parseColor("#666666"));
		}
		mGalleryTabWidget.getChildAt(mGalleryTabHost.getCurrentTab())
				.setBackgroundColor(Color.parseColor("#303334"));
	}

	//при получении нового интента создаем новый фрагмент и перезагружаем содержимое экрана
	@Override
	protected void onNewIntent(Intent intent) {
		EventsGalleryFragment fragment;
			fragment = (EventsGalleryFragment)
					getSupportFragmentManager().findFragmentById(android.R.id.tabhost);
		if (fragment != null) {
			fragment.refreshEvents();
		}
	}

}
