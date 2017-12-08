package org.itstep.mariupol.afishamariupol;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.itstep.mariupol.afishamariupol.model.EventsItem;
import org.itstep.mariupol.afishamariupol.global.Globals;

import java.io.IOException;
import java.util.ArrayList;

/**
 * класс фрагмента главного экрана (галлереи событий)
*/

public class EventsGalleryFragment extends Fragment {

	private static final String TAG = "EventsGalleryFragment";
	public static final String EXTRA_DAY_NUMBER = "com.itstep.mariupol.afishamariupol.day_number";

	GridView mGridView;
    TextView mEmptyTextView;
	Button mEventsGalleryAllButton;
	Button mEventsGalleryCinemaButton;
	Button mEventsGalleryTheatreButton;
	Button mEventsGalleryClubsButton;
	Button mEventsGalleryShowsButton;
	Button mEventsGalleryBusinessButton;
	Button mEventsGallerySportButton;
	Button mEventsGalleryFreeButton;
	private TextView mDeviceMarkerTextView;
	private String mDeviceMarkerString;
	
	ArrayList<EventsItem> mEventsItems;
	ThumbDownloader<ImageView> mThumbThread;
	
	private int mDayInt = 0;
	private int mEventTypeInt = -1;
	private int mPage = 0;
	private boolean loading = false;
	private boolean refreshing = false;
	private boolean oldDevice = false;
	private boolean mConnectionFail = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// setRetainInstance(true) - при пересоздании сохранить объект класса Fragment
		// не будут вызваны методы onDestroy и onCreate
		setRetainInstance(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			oldDevice = true;
		} else {
			setHasOptionsMenu(true);
		}

		//вызываем метод загрузки данных
		loadEventsData();

		//из аргументов фрагмента получаем порядковый номер дня,
		// за который будут выводиться данные
		mDayInt = getArguments().getInt(EXTRA_DAY_NUMBER);

		//создаем и запускаем объект - динамический загрузчик изображений
		mThumbThread = new ThumbDownloader<ImageView>(new Handler());
		mThumbThread.setListener(new ThumbDownloader.Listener<ImageView>() {
			@Override
			public void onThumbDownloaded(ImageView imageView, Bitmap thumbnail) {
				if (isVisible()) {
					imageView.setImageBitmap(thumbnail);
				}
			}
		});
		mThumbThread.start();
		mThumbThread.getLooper();
		//Log.i(TAG, "Background thread started");
	}

	//при уничтожении фрагмента прекращаем работу загрузчика изображений
	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbThread.quit();
		//Log.i(TAG, "Background thread destroyed");
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mThumbThread.clearQueue();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_events_gallery, menu);
	}
	
	//обработчик выбора пункта меню (для значка "обновить") на верхней панели
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_refresh_results: {
				refreshEvents();
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	//переопределение метода настройки представления во время его создания
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
				
		//подключение разметки - содержимого контейнера
		View view = inflater.inflate(R.layout.fragment_events_gallery, container, false);

		//поиск скрытого текстового виджета, в котором указан тип экрана
		mDeviceMarkerTextView = (TextView) view.findViewById(R.id.deviceMarker);
		if (mDeviceMarkerTextView != null) {
			mDeviceMarkerString = mDeviceMarkerTextView.getText().toString();
		}

		mGridView = (GridView)view.findViewById(R.id.gridView);
        mEmptyTextView = (TextView) view.findViewById(R.id.events_gallery_emptyTextView);

		mEventsGalleryAllButton = (Button)view.findViewById(R.id.eventsGalleryAllButton);
		mEventsGalleryCinemaButton = (Button)view.findViewById(R.id.eventsGalleryCinemaButton);
		mEventsGalleryTheatreButton = (Button)view.findViewById(R.id.eventsGalleryTheatreButton);
		mEventsGalleryClubsButton = (Button)view.findViewById(R.id.eventsGalleryClubsButton);
		mEventsGalleryShowsButton = (Button)view.findViewById(R.id.eventsGalleryShowsButton);
		mEventsGalleryBusinessButton = (Button)view.findViewById(R.id.eventsGalleryBusinessButton);
		mEventsGallerySportButton = (Button)view.findViewById(R.id.eventsGallerySportButton);
		mEventsGalleryFreeButton = (Button)view.findViewById(R.id.eventsGalleryFreeButton);

		//вызов метода загрузки данных (коллекции событий для главного экрана) в виджет-сетку
		setupEventsAdapter();

		//обработчик нажатия на элементе-событии на главном экране
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> gridView, View view, int pos,
									long id) {

				//выбираем объект-событие из коллекции, по которому нажал пользователь
				EventsItem eventsItem = mEventsItems.get(pos);
				//из выбранного события получаем урл-адрес страницы детализации на сайте
				Uri eventPageUri = Uri.parse(eventsItem.getContentUrlString());
				//переписываем из коллекции событий в коллекцию строк все
				// урл-адреса страниц детализаций на сайте
				ArrayList<String> eventsUrlList = new ArrayList<>();
				for (EventsItem eventsItem1 : mEventsItems) {
					eventsUrlList.add(eventsItem1.getContentUrlString());
				}
				//создаем явное намерение - от класса активности главного экрана (getActivity()) -
				//к классу активности детализации (EventPagerActivity.class)
				Intent intent = new Intent(getActivity(), EventPagerActivity.class);
				//прикрепляем к интенту дополнения - весь список адресов детализаций
				// и адрес страницы детализации на сайте, с которой нужно будет получить данные
				intent.putExtra(EventPageFragment.EXTRA_EVENTS_LIST, eventsUrlList);
				intent.putExtra(EventPageFragment.EXTRA_EVENT_URL, eventPageUri.toString());
				//запускаем активность детализации события
				startActivity(intent);
			}
		});

		//в обработчиках нажатий по кнопкам типов событий выставляем кнопкам нужные цвета,
		// устанавливаем параметр mEventTypeInt - какой тип события выбран,
		// вызываем метод перезагрузки содержимого экрана
		mEventsGalleryAllButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEventsGalleryAllButton.setBackgroundColor(Color.parseColor("#555555"));
				mEventsGalleryCinemaButton.setBackgroundColor(Color.parseColor("#80BCDA"));
				mEventsGalleryTheatreButton.setBackgroundColor(Color.parseColor("#E5DC81"));
				mEventsGalleryClubsButton.setBackgroundColor(Color.parseColor("#C99CE3"));
				mEventsGalleryShowsButton.setBackgroundColor(Color.parseColor("#AFC972"));
				mEventsGalleryBusinessButton.setBackgroundColor(Color.parseColor("#F08B9E"));
				mEventsGallerySportButton.setBackgroundColor(Color.parseColor("#656BDD"));
				mEventsGalleryFreeButton.setBackgroundColor(Color.parseColor("#C44EA7"));
				mEventTypeInt = -1;
				refreshEvents();
			}
		});

		mEventsGalleryCinemaButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEventsGalleryAllButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryCinemaButton.setBackgroundColor(Color.parseColor("#80BCDA"));
				mEventsGalleryTheatreButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryClubsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryShowsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryBusinessButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGallerySportButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryFreeButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventTypeInt = 0;
				refreshEvents();
			}
		});

		mEventsGalleryTheatreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEventsGalleryAllButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryCinemaButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryTheatreButton.setBackgroundColor(Color.parseColor("#E5DC81"));
				mEventsGalleryClubsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryShowsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryBusinessButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGallerySportButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryFreeButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventTypeInt = 1;
				refreshEvents();
			}
		});

		mEventsGalleryClubsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEventsGalleryAllButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryCinemaButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryTheatreButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryClubsButton.setBackgroundColor(Color.parseColor("#C99CE3"));
				mEventsGalleryShowsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryBusinessButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGallerySportButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryFreeButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventTypeInt = 2;
				refreshEvents();
			}
		});

		mEventsGalleryShowsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEventsGalleryAllButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryCinemaButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryTheatreButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryClubsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryShowsButton.setBackgroundColor(Color.parseColor("#AFC972"));
				mEventsGalleryBusinessButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGallerySportButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryFreeButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventTypeInt = 3;
				refreshEvents();
			}
		});

		mEventsGalleryBusinessButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEventsGalleryAllButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryCinemaButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryTheatreButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryClubsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryShowsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryBusinessButton.setBackgroundColor(Color.parseColor("#F08B9E"));
				mEventsGallerySportButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryFreeButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventTypeInt = 4;
				refreshEvents();
			}
		});

		mEventsGallerySportButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEventsGalleryAllButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryCinemaButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryTheatreButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryClubsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryShowsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryBusinessButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGallerySportButton.setBackgroundColor(Color.parseColor("#656BDD"));
				mEventsGalleryFreeButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventTypeInt = 5;
				refreshEvents();
			}
		});

		mEventsGalleryFreeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEventsGalleryAllButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryCinemaButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryTheatreButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryClubsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryShowsButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryBusinessButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGallerySportButton.setBackgroundColor(Color.parseColor("#303334"));
				mEventsGalleryFreeButton.setBackgroundColor(Color.parseColor("#C44EA7"));
				mEventTypeInt = 6;
				refreshEvents();
			}
		});

		return view;
	}

	//переопределяем метод действий, выполняемых после создания представления 
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		//собираем ссылки на виджеты кнопок в коллекцию, и в зависимости от
		// типа устройства корректируем размеры кнопок
		ArrayList<Button> eventGalleryButtonsList = new ArrayList<>();
		eventGalleryButtonsList.add(mEventsGalleryAllButton);
		eventGalleryButtonsList.add(mEventsGalleryCinemaButton);
		eventGalleryButtonsList.add(mEventsGalleryTheatreButton);
		eventGalleryButtonsList.add(mEventsGalleryClubsButton);
		eventGalleryButtonsList.add(mEventsGalleryShowsButton);
		eventGalleryButtonsList.add(mEventsGalleryBusinessButton);
		eventGalleryButtonsList.add(mEventsGallerySportButton);
		eventGalleryButtonsList.add(mEventsGalleryFreeButton);

		if (oldDevice && (mDeviceMarkerString.equals("hdpi") || mDeviceMarkerString.equals("mdpi") || mDeviceMarkerString.equals("ldpi"))) {
			int maxButtonHeight = (getResources()
					.getConfiguration()
					.orientation == Configuration.ORIENTATION_PORTRAIT) ? 90 : 70;
			if (mDeviceMarkerString.equals("mdpi")) {
				maxButtonHeight = (getResources()
						.getConfiguration()
						.orientation == Configuration.ORIENTATION_PORTRAIT) ? 50 : 40;
			} else if (mDeviceMarkerString.equals("ldpi")) {
				maxButtonHeight = (getResources()
						.getConfiguration()
						.orientation == Configuration.ORIENTATION_PORTRAIT) ? 40 : 30;
			}
			for (Button currentButton : eventGalleryButtonsList) {
				currentButton.setHeight(maxButtonHeight);
			}
 		} else if (mDeviceMarkerString.equals("sw600")) {
			int maxButtonHeight = 70;
			for (Button currentButton : eventGalleryButtonsList) {
				currentButton.setHeight(maxButtonHeight);
			}
		}
	}

	//класс стандартного фонового потока выполнения, в котором запускается получение данных
	//с сайта, их парсинг и возврат в виде коллекции объектов с указанием в аргументах
	//метода AfishaFetchr().fetchEventsItems() кодов дня и типа события mDayInt и mEventTypeInt
	private class FetchEventsItemsTask extends AsyncTask<Void,Void,ArrayList<EventsItem>> {

		@Override
		protected void onPreExecute() {
			Globals.showProgressToastInstance(getActivity());
		}

		@Override
		protected ArrayList<EventsItem> doInBackground(Void... params) {
			Activity activity = getActivity();
			if (activity == null) {
				return new ArrayList<EventsItem>();
			}
			try {
				return new AfishaFetchr().fetchEventsItems(mDayInt, mEventTypeInt, mPage);
			} catch (IOException ioe) {
				mConnectionFail = true;
				return new ArrayList<EventsItem>();
			}
		}

		@Override
		protected void onPostExecute(ArrayList<EventsItem> eventsItems) {
			if(mEventsItems == null || refreshing) {
				mEventsItems = eventsItems;
				refreshing = false;
				setupEventsAdapter();
			} else {
				mEventsItems.addAll(eventsItems);
				((BaseAdapter)mGridView.getAdapter()).notifyDataSetChanged();
			}
			loading = false;
			Globals.closeProgressToast();

			if (mConnectionFail == true) {
				Toast.makeText(
						getActivity(),
						"Ошибка соединения с ресурсом http://afisha.gidmariupol.com",
						Toast.LENGTH_SHORT).show();
			}

			//если в классе глобальных переменных заполнена строка, сообщающая, что
			//для данного дня и даного типа событий нет - скрываем пустой виджет-сетку
			//и показваем виджет-текст с этой строкой
            if (Globals.getNoEventsString() != "") {
                mGridView.setVisibility(View.INVISIBLE);
                mEmptyTextView.setText(Globals.getNoEventsString());
                mEmptyTextView.setVisibility(View.VISIBLE);
                Globals.setNoEventsString("");
            } else {
                if (mGridView != null && mEmptyTextView != null) {
					mGridView.setVisibility(View.VISIBLE);
					mEmptyTextView.setVisibility(View.INVISIBLE);
				}
            }
		}
	}

	//метод запуска адаптера, заполняющего виджет-сетку
	//данными из коллекции событий
	void setupEventsAdapter() {
		if (getActivity() == null || mGridView == null) return;

		if (mEventsItems != null) {
			mGridView.setAdapter(new EventsItemAdapter(mEventsItems));
		} else {
			mGridView.setAdapter(null);
		}
	}

	//метод, запускающий фоновый поток получения данных
	private void loadEventsData() {
		if(loading)return;
		loading = true;
		mPage++;
		new FetchEventsItemsTask().execute();
	}

	//метод, запускающий фоновый поток для обновления данных
	public void refreshEvents() {
		if(loading)return;
		loading = true;
		refreshing = true;
		mPage = 1;
		mThumbThread.clearQueue();
		new FetchEventsItemsTask().execute();
	}

	//внутренний класс-адаптер для заполнения виджета-сетки,
	//в качестве аргумента принимает коллекцию событий
	private class EventsItemAdapter extends ArrayAdapter<EventsItem> {
		public EventsItemAdapter(ArrayList<EventsItem> eventsItems) {
			super(getActivity(), 0, eventsItems);
		}

		//метод заполнения данными одного элемента виджета-сетки
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			//подключение представления с разметкой одного элемента виджета-сетки
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.gallery_item, parent, false);
			}
			final ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
			//заставка на время заполнения данными
			imageView.setImageResource(R.drawable.dowloading);

			//номер элемента и общее число элементов в выборке
			TextView galleryItemIndexTextView = (TextView)convertView.findViewById(R.id.gallery_item_index);
			galleryItemIndexTextView.setText(String.format("%d/%d", position + 1, mEventsItems.size()));

			EventsItem eventsItem = getItem(position);

			//заголовок события
			TextView galleryItemCaptionTextView = (TextView)convertView.findViewById(R.id.gallery_item_caption);
			galleryItemCaptionTextView.setText(eventsItem.getCaption());

			//фон элемента по умолчанию
			ImageView backgroundImageView = (ImageView) convertView.findViewById(R.id.gallery_item_background_imageView);

			//тип события
            TextView galleryItemTypeTextView = (TextView) convertView.findViewById(R.id.gallery_item_type);

			//фон элемента в зависимости от типа события
            switch (eventsItem.getEventType()) {
                case "b-card c-cinema": {
                    backgroundImageView.setImageResource(R.drawable.c_cinema);
                    galleryItemTypeTextView.setText("КИНО");
                    galleryItemTypeTextView.setTextColor(Color.parseColor("#80BCDA"));
                    break;
                }
                case "b-card c-theatre": {
                    backgroundImageView.setImageResource(R.drawable.c_theatre);
                    galleryItemTypeTextView.setText("ТЕАТР");
                    galleryItemTypeTextView.setTextColor(Color.parseColor("#E5DC81"));
                    break;
                }
                case "b-card c-clubs": {
                    backgroundImageView.setImageResource(R.drawable.c_clubs);
                    galleryItemTypeTextView.setText("КЛУБЫ И КОНЦЕРТЫ");
                    galleryItemTypeTextView.setTextColor(Color.parseColor("#C99CE3"));
                    break;
                }
                case "b-card c-shows": {
                    backgroundImageView.setImageResource(R.drawable.c_shows);
                    galleryItemTypeTextView.setText("ВЫСТАВКИ");
                    galleryItemTypeTextView.setTextColor(Color.parseColor("#AFC972"));
                    break;
                }
                case "b-card c-business": {
                    backgroundImageView.setImageResource(R.drawable.c_business);
                    galleryItemTypeTextView.setText("БИЗНЕС-СОБЫТИЯ");
                    galleryItemTypeTextView.setTextColor(Color.parseColor("#F08B9E"));
                    break;
                }
                case "b-card c-sport": {
                    backgroundImageView.setImageResource(R.drawable.c_sport);
                    galleryItemTypeTextView.setText("СПОРТ");
                    galleryItemTypeTextView.setTextColor(Color.parseColor("#656BDD"));
                    break;
                }
                case "b-card c-free": {
                    backgroundImageView.setImageResource(R.drawable.c_free);
                    galleryItemTypeTextView.setText("FREE");
                    galleryItemTypeTextView.setTextColor(Color.parseColor("#C44EA7"));
                    break;
                }
                default: {
                    backgroundImageView.setImageResource(R.drawable.c_cinema);
                    galleryItemTypeTextView.setText("");
                    break;
                }
            }

			//ограничение по возрасту
            TextView galleryItemAgeLimit = (TextView) convertView.findViewById(R.id.gallery_item_ageLimit);
            galleryItemAgeLimit.setText(eventsItem.getAgeLimit());

			//теги - подвиды типов событий
            TextView galleryItemTitle = (TextView) convertView.findViewById(R.id.gallery_item_title);
            galleryItemTitle.setText(eventsItem.getTitle());

			//если в кэше есть нужное изображение для превью постера события - заполняем виджет-картинку из кэша,
			//иначе - сначала загружаем изображение
			Bitmap cacheHit = mThumbThread.checkCache(eventsItem.getImageUrlString());
			if (cacheHit != null) {
				imageView.setImageBitmap(cacheHit);
			} else {
				mThumbThread.queueThumbnail(imageView, eventsItem.getImageUrlString());
			}

			// предзагрузка изображений
			for (int i=Math.max(0, position-10); i< Math.min(mEventsItems.size()-1, position+10); i++) {
				mThumbThread.queuePreload(eventsItem.getImageUrlString());
			}

			// Для бесконечной циклической дозагрузки элементов
//			if (position == mEventsItems.size() -1)
//				loadEventsData();

			//обработка нажатия на элементе-событии с анимацией
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Globals.closeProgressToast();
					Globals.showProgressToastInstance(getActivity());

					AlphaAnimation alphaAnimation = new AlphaAnimation(1.0F, 0.5F);
					alphaAnimation.setDuration(50);
					alphaAnimation.setFillAfter(false);
					imageView.startAnimation(alphaAnimation);

					EventsItem eventsItem = mEventsItems.get(pos);
					Uri eventPageUri = Uri.parse(eventsItem.getContentUrlString());

					ArrayList<String> eventsUrlList = new ArrayList<>();
					for (EventsItem eventsItem1 : mEventsItems) {
						eventsUrlList.add(eventsItem1.getContentUrlString());
					}

					Intent intent = new Intent(getActivity(), EventPagerActivity.class);
					intent.putExtra(EventPageFragment.EXTRA_EVENTS_LIST, eventsUrlList);
					intent.putExtra(EventPageFragment.EXTRA_EVENT_URL, eventPageUri.toString());
					Globals.closeProgressToast();
					startActivity(intent);
				}
			});

			return convertView;
		}
	}

	//статичный метод, вызываемый активностью и принимающий из нее номер дня для фрамента
	public static EventsGalleryFragment newInstance(int dayNumberInt) {
		Bundle args = new Bundle();
		args.putInt(EXTRA_DAY_NUMBER, dayNumberInt);
		EventsGalleryFragment fragment = new EventsGalleryFragment();
		fragment.setArguments(args);
		return fragment;
	}
}
