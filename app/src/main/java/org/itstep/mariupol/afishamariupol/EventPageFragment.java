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
import androidx.fragment.app.Fragment;
import androidx.core.app.NavUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.itstep.mariupol.afishamariupol.model.EventDetails;
import org.itstep.mariupol.afishamariupol.model.Info;
import org.itstep.mariupol.afishamariupol.model.Session;
import org.itstep.mariupol.afishamariupol.widget.RatioImageView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * класс фрагмента экрана детализации
 */

public class EventPageFragment extends Fragment {

    private FetchEventDetailsTask mFetchEventDetailsTask;
    private EventDetails mEventDetails;
    //ThumbDownloader<ImageView> mThumbThread;

    private TextView mEventContentTextView;
    private ImageView mVideoThumbImageView;
    private TextView mHeaderTextView;
    private Button mShareFbButton;
    private Button mShareVkButton;
    private Button mShareTwButton;
    private Button mShareOkButton;
    private Button mShareMailruButton;
    private LinearLayout mShareLinearLayout;

    private String mEventUrlString;
    private String mVideoUrlString;
    private String mShareUrlString;
    private boolean mConnectionFail = false;
    private boolean oldDevice = false;
    private TextView mDeviceMarkerTextView;
    private String mDeviceMarkerString;

    //имена для приложений намерения
    public static final String EXTRA_EVENT_URL = "com.itstep.mariupol.afishamariupol.event_url";
    public static final String EXTRA_EVENTS_LIST = "com.itstep.mariupol.afishamariupol.events_url_list";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Globals.closeProgressToast();

        super.onCreate(savedInstanceState);

        // setRetainInstance(true) - при пересоздании сохранить объект класса Fragment
        // не будут вызваны методы onDestroy и onCreate
        setRetainInstance(true);

        // если версия SDK старая - устанавливаем соответствующий флаг, если нет -
        // включаем поддержку динамического меню для верхней панели экрана
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            oldDevice = true;
        } else {
            setHasOptionsMenu(true);
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //получаем из приложения намерения адрес страницы сайта с детализацией события
        mEventUrlString = (String) getArguments().getSerializable(EXTRA_EVENT_URL);

        //запуск потока получения данных детализации
        mFetchEventDetailsTask = new FetchEventDetailsTask();
        mFetchEventDetailsTask.execute();

        //создание и запуск экземпляра загрузчика изображений
//        mThumbThread = new ThumbDownloader<ImageView>(new Handler());
//        mThumbThread.setListener(new ThumbDownloader.Listener<ImageView>() {
//            @Override
//            public void onThumbDownloaded(ImageView imageView, Bitmap thumbnail) {
//                if (isVisible()) {
//                    imageView.setImageBitmap(thumbnail);
//                }
//            }
//        });
//        mThumbThread.start();
//        mThumbThread.getLooper();
        mShareUrlString = "";
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mThumbThread.quit();
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        mThumbThread.clearQueue();
//    }

    // создание меню на верхней панели для современных версий ОС
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event_page, menu);
    }

    // функция отображения/скрытия панели кнопок репостов в "социальные сети"
    private void toggleSharePanel() {
        if (mShareLinearLayout.getVisibility() == View.GONE) {
            Animation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f);
            scaleAnimation.setDuration(500);
            //scaleAnimation.setFillAfter(true);
            Animation alphaAnimation = new AlphaAnimation(0.0f, 0.7f);
            alphaAnimation.setDuration(500);
            //alphaAnimation.setFillAfter(true);
            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setFillAfter(true);
            mShareLinearLayout.setAnimation(animationSet);
            mShareLinearLayout.setVisibility(View.VISIBLE);
        } else {
            Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f);
            scaleAnimation.setDuration(500);
            //scaleAnimation.setFillAfter(true);
            Animation alphaAnimation = new AlphaAnimation(0.7f, 0.0f);
            alphaAnimation.setDuration(500);
            //alphaAnimation.setFillAfter(true);
            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setFillAfter(true);
            mShareLinearLayout.setAnimation(animationSet);
            mShareLinearLayout.setVisibility(View.GONE);
        }
    }

    // обработчик выбора пункта меню (для значка "обновить") на верхней панели
    // для современных версий ОС
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share: {
                toggleSharePanel();
                return true;
            }
            case android.R.id.home: {
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View view;

        //останавливаем главный поток выполнения до тех пор, пока фоновый поток
        //не заполнит данными объект модели детализации события
        try {
            mEventDetails = mFetchEventDetailsTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



        if (mEventDetails != null) {
            //если событие - кино, то заполняем макет детализации с вкладками - fragment_event_tab_page
            if (mEventDetails.isCinema()) {

                view = inflater.inflate(R.layout.fragment_event_tab_page, parent, false);


                mDeviceMarkerTextView = (TextView) view.findViewById(R.id.deviceMarker);
                mDeviceMarkerString = "";
                if (mDeviceMarkerTextView != null) {
                    mDeviceMarkerString = mDeviceMarkerTextView.getText().toString();
                }

                if (!(mDeviceMarkerString.equals("sw600") && (getResources()
                        .getConfiguration()
                        .orientation == Configuration.ORIENTATION_LANDSCAPE))) {
                    final TabHost tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
                    tabHost.setup();
                    TabHost.TabSpec tabSpec;

                    tabSpec = tabHost.newTabSpec("tab_sessions_today");
                    tabSpec.setIndicator(getResources().getString(R.string.tab_sessions_today));
                    tabSpec.setContent(R.id.tabSessionsToday);
                    tabHost.addTab(tabSpec);

                    tabSpec = tabHost.newTabSpec("tab_contents");
                    tabSpec.setIndicator(getResources().getString(R.string.tab_contents));
                    tabSpec.setContent(R.id.tabContents);
                    tabHost.addTab(tabSpec);

                    tabSpec = tabHost.newTabSpec("tab_info");
                    tabSpec.setIndicator(getResources().getString(R.string.tab_info));
                    tabSpec.setContent(R.id.tabInfo);
                    tabHost.addTab(tabSpec);
                    //вкладка по умолчанию
                    tabHost.setCurrentTabByTag("tag_sessions_today");

                    final TabWidget tabWidget = tabHost.getTabWidget();

                    for (int i_tab = 0; i_tab < tabWidget.getChildCount(); i_tab++) {
                        TextView tabTextView = (TextView) tabWidget.getChildAt(i_tab)
                                .findViewById(android.R.id.title);
                        tabTextView.setTextColor(Color.parseColor("#43BFD6"));
                        tabWidget.getChildAt(i_tab)
                                .setBackgroundColor(Color.parseColor("#303334"));
                        if (oldDevice && (mDeviceMarkerString.equals("hdpi") || mDeviceMarkerString.equals("mdpi") || mDeviceMarkerString.equals("ldpi"))) {
                            int height = Integer.parseInt(getResources()
                                    .getString(R.string.event_tabs_page_tabWidgetHeight));
                            tabWidget.getChildAt(i_tab).getLayoutParams().height = height;
                            tabTextView.getLayoutParams().height = height;
                            tabTextView.setGravity(Gravity.CENTER);
                        } else if (mDeviceMarkerString.equals("sw600")) {
                            tabTextView.setGravity(Gravity.CENTER);
                            int textSize = (getResources()
                                    .getConfiguration()
                                    .orientation == Configuration.ORIENTATION_PORTRAIT) ? 20 : 12;
                            tabTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                            tabTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                        }
                    }
                    TextView tabTextView = (TextView) tabWidget.getChildAt(tabHost.getCurrentTab())
                            .findViewById(android.R.id.title);
                    tabTextView.setTextColor(Color.parseColor("#999999"));

                    tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                        @Override
                        public void onTabChanged(String tabId) {
                            for (int i_tab = 0; i_tab < tabWidget.getChildCount(); i_tab++) {
                                TextView tabTextView = (TextView) tabWidget.getChildAt(i_tab)
                                        .findViewById(android.R.id.title);
                                tabTextView.setTextColor(Color.parseColor("#43BFD6"));
                            }
                            TextView tabTextView = (TextView) tabWidget.getChildAt(tabHost.getCurrentTab())
                                    .findViewById(android.R.id.title);
                            tabTextView.setTextColor(Color.parseColor("#999999"));
                        }
                    });
                }

                //общий элемент для всех вкладок - заголовок
                mHeaderTextView = (TextView) view.findViewById(R.id.event_tab_page_headerTextView);
                mHeaderTextView.setText(mEventDetails.getHeader() + " (" + mEventDetails.getAgeLimit() + ")");

            /* tab Sessions today (1) - вкладка "сеансы на сегодня" */
                RatioImageView imageView = (RatioImageView) view.findViewById(R.id.imageView);
                //превью постера кино загружаем при помощи библиотеки Picasso
                //и при необходимости обрезаем методом transform
                if (!(oldDevice && (mDeviceMarkerString.equals("hdpi")))) {
                    Picasso.with(getActivity())
                            .load(mEventDetails.getImageUrlString())
                            .placeholder(R.drawable.dowloading)
                            .error(R.drawable.dowloading_error)
                            .into(imageView);
                } else {
                    Picasso.with(getActivity())
                            .load(mEventDetails.getImageUrlString())
                            .transform(new halvedTransformation())
                            .placeholder(R.drawable.dowloading)
                            .error(R.drawable.dowloading_error)
                            .into(imageView);
                }
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), BigImageActivity.class);
                        intent.putExtra(BigImageFragment.EXTRA_IMAGE_URL,
                                mEventDetails.getImageUrlString());
                        startActivity(intent);
                    }
                });
                // список сеансов на сегодня
                ArrayList<Session> sessionList = mEventDetails.getSessionList();
                TableLayout sessionTableLayout = (TableLayout) view.findViewById(R.id.tableSessions);
                for (Session sessionListItem : sessionList) {
                    TableRow sessionItemTableRow = new TableRow(getActivity());
                    TextView sessionPlaceTextView = new TextView(getActivity());
                    TextView sessionTimesTextView = new TextView(getActivity());
                    TableRow.LayoutParams sessionTableRowParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT);
                    TableRow.LayoutParams sessionTextViewParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT);
                    sessionItemTableRow.setLayoutParams(sessionTableRowParams);
                    sessionPlaceTextView.setLayoutParams(sessionTextViewParams);
                    sessionTimesTextView.setLayoutParams(sessionTextViewParams);
                    sessionItemTableRow.addView(sessionPlaceTextView);
                    sessionItemTableRow.addView(sessionTimesTextView);
                    sessionTableLayout.addView(sessionItemTableRow);
                    sessionPlaceTextView.setPadding(5, 10, 5, 5); //left top right bottom
                    sessionTimesTextView.setPadding(10, 5, 5, 5);
                    sessionPlaceTextView.setTextColor(Color.parseColor("#999999"));
                    sessionTimesTextView.setTextColor(Color.parseColor("#FFFFFF"));
                    sessionPlaceTextView.setText(sessionListItem.getPlace());
                    sessionTimesTextView.setText(sessionListItem.getTimesString());
                    if (mDeviceMarkerString.equals("sw600")) {
                        int textSize = (getResources()
                                .getConfiguration()
                                .orientation == Configuration.ORIENTATION_PORTRAIT) ? 24 : 16;
                        sessionPlaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                        sessionTimesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    } else if (mDeviceMarkerString.equals("ldpi")) {
                        int textSize = (getResources()
                                .getConfiguration()
                                .orientation == Configuration.ORIENTATION_PORTRAIT) ? 12 : 14;
                        sessionPlaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                        sessionTimesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    }
                }
                // список сеансов на следующие дни
                ArrayList<ArrayList<Session>> nextSessionBlockList =
                        mEventDetails.getNextSessionBlockList();
                if (nextSessionBlockList != null) {
                    ArrayList<String> nextSessionHeaderList = mEventDetails.getNextSessionHeaderList();
                    int headerCounter = 0;
                    TableLayout nextSessionsTableLayout = (TableLayout) view.findViewById(R.id.tableNextSessions);
                    for (ArrayList<Session> nextSessionListItem : nextSessionBlockList) {
                        TableLayout nextSessionBlockTableLayout = new TableLayout(getActivity());
                        TableLayout.LayoutParams sessionBlockTableParams = new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT);
                        nextSessionBlockTableLayout.setColumnShrinkable(1, true);
                        // заголовок - дата сеансов
                        TableRow.LayoutParams sessionHeaderTextViewParams = new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT);
                        TextView nextSessionHeaderTextView = new TextView(getActivity());
                        nextSessionHeaderTextView.setLayoutParams(sessionHeaderTextViewParams);
                        nextSessionHeaderTextView.setTextColor(Color.parseColor("#999999"));
                        if (mDeviceMarkerString.equals("sw600")) {
                            int textSize = (getResources()
                                    .getConfiguration()
                                    .orientation == Configuration.ORIENTATION_PORTRAIT) ? 24 : 16;
                            nextSessionHeaderTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                            nextSessionHeaderTextView.setPadding(0, 10, 0, 0);
                        }
                        nextSessionHeaderTextView.setText(nextSessionHeaderList.get(headerCounter));
                        headerCounter++;
                        // блок сеансов на определенную дату
                        nextSessionBlockTableLayout.setLayoutParams(sessionBlockTableParams);
                        nextSessionBlockTableLayout.addView(nextSessionHeaderTextView);
                        nextSessionsTableLayout.addView(nextSessionBlockTableLayout);
                        for (Session sessionListItem : nextSessionListItem) {
                            TableRow sessionItemTableRow = new TableRow(getActivity());
                            TextView sessionPlaceTextView = new TextView(getActivity());
                            TextView sessionTimesTextView = new TextView(getActivity());
                            TableRow.LayoutParams sessionTableRowParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT);
                            TableRow.LayoutParams sessionTextViewParams = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT);
                            sessionItemTableRow.setLayoutParams(sessionTableRowParams);
                            sessionPlaceTextView.setLayoutParams(sessionTextViewParams);
                            sessionTimesTextView.setLayoutParams(sessionTextViewParams);
                            sessionItemTableRow.addView(sessionPlaceTextView);
                            sessionItemTableRow.addView(sessionTimesTextView);
                            nextSessionBlockTableLayout.addView(sessionItemTableRow);
                            sessionPlaceTextView.setPadding(5, 10, 5, 5); //left top right bottom
                            sessionTimesTextView.setPadding(10, 5, 5, 5);
                            sessionPlaceTextView.setTextColor(Color.parseColor("#FFFFFF"));
                            sessionTimesTextView.setTextColor(Color.parseColor("#E4A04C"));
                            sessionPlaceTextView.setText(sessionListItem.getPlace());
                            sessionTimesTextView.setText(sessionListItem.getTimesString());
                            if (mDeviceMarkerString.equals("sw600")) {
                                int textSize = (getResources()
                                        .getConfiguration()
                                        .orientation == Configuration.ORIENTATION_PORTRAIT) ? 24 : 16;
                                sessionPlaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                                sessionTimesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                            }
                        }
                    }
                } else {
                    TextView nextSessionsTitleTextView =
                            (TextView) view.findViewById(R.id.nextSessionsTitleTextView);
                    nextSessionsTitleTextView.setVisibility(View.GONE);
                }

                //tab Contents (2) - вкладка "содержание"
                mEventContentTextView = (TextView) view.findViewById(R.id.eventContentTextView);
                mEventContentTextView.setText(mEventDetails.getContent());
                mVideoThumbImageView = (ImageView) view.findViewById(R.id.event_page_videoThumbImageView);
                //по коду видео получаем адрес его превью
                String videoThumbUrlString = "http://img.youtube.com/vi/"
                        + mEventDetails.getVideoThumbCodeString()
                        + "/mqdefault.jpg";
//                Bitmap cacheHit = mThumbThread.checkCache(videoThumbUrlString);
//                if (cacheHit != null) {
//                    mVideoThumbImageView.setImageBitmap(cacheHit);
//                } else {
//                    mThumbThread.queueThumbnail(mVideoThumbImageView, videoThumbUrlString);
//                }
                Picasso.with(getActivity())
                        .load(videoThumbUrlString)
                        .placeholder(R.drawable.dowloading)
                        .error(R.drawable.dowloading_error)
                        .into(mVideoThumbImageView);
                //по нажатию на превью видео вызываем неявное намерение
                //ОС находит подходящее приложение и открывает в нем видео
                mVideoThumbImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        mVideoUrlString = "https://www.youtube.com/embed/" + mEventDetails.getVideoThumbCodeString();
                        intent.setData(Uri.parse(mVideoUrlString));
                        startActivity(intent);
                    }
                });

                //tab Info (3) - вкладка "Info"
                ArrayList<Info> infoArrayList = mEventDetails.getInfoList();
                TableLayout infoTableLayout = (TableLayout) view.findViewById(R.id.tableInfo);
                for (Info infoArrayListItem : infoArrayList) {
                    TableRow infoItemTableRow = new TableRow(getActivity());
                    TextView infoLabelTextView = new TextView(getActivity());
                    TextView infoDataTextView = new TextView(getActivity());
                    TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT);
                    TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT);
                    infoItemTableRow.setLayoutParams(tableRowParams);
                    infoLabelTextView.setLayoutParams(textViewParams);
                    infoDataTextView.setLayoutParams(textViewParams);
                    infoItemTableRow.addView(infoLabelTextView);
                    infoItemTableRow.addView(infoDataTextView);
                    infoTableLayout.addView(infoItemTableRow);
                    infoLabelTextView.setPadding(5, 10, 5, 5); //left top right bottom
                    infoDataTextView.setPadding(10, 5, 5, 5);
                    infoLabelTextView.setTextColor(Color.parseColor("#FFFFFF"));
                    infoDataTextView.setTextColor(Color.parseColor("#E4A04C"));
                    infoLabelTextView.setText(infoArrayListItem.getLabel());
                    infoDataTextView.setText(infoArrayListItem.getData());
                    if (mDeviceMarkerString.equals("sw600")) {
                        int textSize = (getResources()
                                .getConfiguration()
                                .orientation == Configuration.ORIENTATION_PORTRAIT) ? 24 : 16;
                        infoLabelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                        infoDataTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    }
                }
            } else {
                //если событие - не кино, то подключаем макет fragment_event_whole_page без вкладок
                view = inflater.inflate(R.layout.fragment_event_whole_page, parent, false);

                mDeviceMarkerTextView = (TextView) view.findViewById(R.id.deviceMarker);
                mDeviceMarkerString = "";
                if (mDeviceMarkerTextView != null) {
                    mDeviceMarkerString = mDeviceMarkerTextView.getText().toString();
                }

                mHeaderTextView = (TextView) view.findViewById(R.id.event_whole_page_headerTextView);
                mHeaderTextView.setText(mEventDetails.getHeader() + " (" + mEventDetails.getAgeLimit() + ")");

                //для сохранения пропорций изображения при масштабировании используем класс RatioImageView,
                //наследованый от ImageView и переопределяющий его метод
                RatioImageView imageView = (RatioImageView) view.findViewById(R.id.not_cinema_event_imageView);
                if (!(oldDevice && (mDeviceMarkerString.equals("hdpi")))
                        && !mDeviceMarkerString.equals("sw600")) {
                    Picasso.with(getActivity())
                            .load(mEventDetails.getImageUrlString())
                            .placeholder(R.drawable.dowloading)
                            .error(R.drawable.dowloading_error)
                            .into(imageView);
                } else {
                    Picasso.with(getActivity())
                            .load(mEventDetails.getImageUrlString())
                            .transform(new halvedNotCinemaTransformation())
                            .placeholder(R.drawable.dowloading)
                            .error(R.drawable.dowloading_error)
                            .into(imageView);
                }
                imageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), BigImageActivity.class);
                        intent.putExtra(BigImageFragment.EXTRA_IMAGE_URL,
                                mEventDetails.getImageUrlString());
                        startActivity(intent);
                    }
                });

                TextView placeTextView = (TextView) view.findViewById(R.id.event_whole_page_place);
                TextView timeTextView = (TextView) view.findViewById(R.id.event_whole_page_time);

                placeTextView.setText(mEventDetails.getSessionList().get(0).getPlace());
                timeTextView.setText(mEventDetails.getSessionList().get(0).getTimesString());

                TextView contentTextView = (TextView) view.findViewById(R.id.event_whole_page_content);
                contentTextView.setText(mEventDetails.getSpannedContent());

                TextView additionalTextView = (TextView) view.findViewById(R.id.event_whole_page_additional);
                additionalTextView.setText(mEventDetails.getAdditionalString());
            }
            // инициализация панели и кнопок репостов в "социальные сети"
            mShareLinearLayout =
                    (LinearLayout) view.findViewById(R.id.event_page_shareLinearLayout);
            mShareFbButton = (Button) view.findViewById(R.id.event_page_shareFbButton);
            mShareVkButton = (Button) view.findViewById(R.id.event_page_shareVkButton);
            mShareTwButton = (Button) view.findViewById(R.id.event_page_shareTwButton);
            mShareOkButton = (Button) view.findViewById(R.id.event_page_shareOkButton);
            mShareMailruButton = (Button) view.findViewById(R.id.event_page_shareMailruButton);
            // обработчик нажатий кнопок репостов в социальные сети
            View.OnClickListener shareButtonsOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.event_page_shareFbButton: {
                            try {
                                mShareUrlString =
                                        "https://www.facebook.com/sharer/sharer.php?sdk=joey&u="
                                                + URLEncoder.encode(mEventUrlString, "UTF-8")
                                                + "&t="
                                                + URLEncoder.encode(mEventDetails.getHeader(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.event_page_shareVkButton: {
                            try {
                                mShareUrlString = "http://vk.com/share.php?url="
                                        + URLEncoder.encode(mEventUrlString, "UTF-8")
                                        + "&title="
                                        + URLEncoder.encode(mEventDetails.getHeader(), "UTF-8")
                                        + "&description="
                                        + URLEncoder.encode((mEventDetails.isCinema())
                                        ? ((mEventDetails.getContent().length() > 100)
                                        ? mEventDetails.getContent().substring(0, 100)
                                        : mEventDetails.getContent()) + "..."
                                        : ((mEventDetails.getSpannedContent().toString().length() > 100)
                                        ? mEventDetails.getSpannedContent().toString().substring(0, 100)
                                        : mEventDetails.getSpannedContent().toString()) + "...", "UTF-8")
                                        + "&image="
                                        + URLEncoder.encode(mEventDetails.getImageUrlString(),
                                        "UTF-8")
                                        + "&noparse=true";
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.event_page_shareTwButton: {
                            try {
                                mShareUrlString = "https://twitter.com/intent/tweet?url="
                                        + URLEncoder.encode(mEventUrlString, "UTF-8")
                                        + "&text="
                                        + URLEncoder.encode(mEventDetails.getHeader(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.event_page_shareOkButton: {
                            try {
                                mShareUrlString =
                                        "http://www.odnoklassniki.ru/dk?st.cmd=addShare&st.s=1&st._surl="
                                                + URLEncoder.encode(mEventUrlString, "UTF-8")
                                                + "&st.comments="
                                                + URLEncoder.encode(mEventDetails.getHeader(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.event_page_shareMailruButton: {
                            try {
                                mShareUrlString =
                                        "http://connect.mail.ru/share?url="
                                                + URLEncoder.encode(mEventUrlString, "UTF-8")
                                                + "&title="
                                                + URLEncoder.encode(mEventDetails.getHeader(),
                                                "UTF-8")
                                                + "&description="
                                                + URLEncoder.encode((mEventDetails.isCinema())
                                                ? mEventDetails.getContent()
                                                : mEventDetails.getSpannedContent()
                                                .toString()
                                                .substring(0, 100) + "...", "UTF-8")
                                                + "&imageurl="
                                                + URLEncoder.encode(mEventDetails.getImageUrlString(),
                                                "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    Intent shareIntent = new Intent(getActivity(), ShareActivity.class);
                    shareIntent.putExtra(ShareFragment.EXTRA_SHARE_URL, mShareUrlString);
                    startActivity(shareIntent);
                }
            };
            mShareFbButton.setOnClickListener(shareButtonsOnClickListener);
            mShareVkButton.setOnClickListener(shareButtonsOnClickListener);
            mShareTwButton.setOnClickListener(shareButtonsOnClickListener);
            mShareOkButton.setOnClickListener(shareButtonsOnClickListener);
            mShareMailruButton.setOnClickListener(shareButtonsOnClickListener);

            // задание обработчика нажатий кнопке открытия/закрытия панели репостов
            // в "социальные сети" для старых версий ОС (вместо кнопки меню на верхней панели)
            if (oldDevice) {
                if (view.findViewById(R.id.event_page_toggleSharePanelButton) != null) {
                    Button toggleSharePanelButton =
                            (Button) view.findViewById(R.id.event_page_toggleSharePanelButton);
                    toggleSharePanelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            toggleSharePanel();
                        }
                    });
                }
            }

            /*view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.i("epf", "Touch was detected");
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });*/

        } else {
            if (mConnectionFail == true) {
                Toast.makeText(
                        getActivity(),
                        "Ошибка соединения с ресурсом http://afisha.gidmariupol.com",
                        Toast.LENGTH_SHORT).show();
                mConnectionFail = false;
            }
            view = new View(getActivity());
            getActivity().finish();
        }

        return view;
    }

    //класс фонового потока выполнения, в котором работает загрузка данных детализации
    private class FetchEventDetailsTask extends AsyncTask<Void, Void, EventDetails> {

        @Override
        protected void onPreExecute() {
            //Globals.showProgressToastInstance(getActivity());
        }

        @Override
        protected EventDetails doInBackground(Void... params) {
            Activity activity = getActivity();
            if (activity == null) {
                return new EventDetails();
            }
            try {
                return new AfishaFetchr().fetchEventDetails(mEventUrlString);
            } catch (IOException ioe) {
                mConnectionFail = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(EventDetails eventDetails) {
            //Globals.closeProgressToast();
        }
    }

    //классы трансформации для обрезки изображений библиотекой Picasso
    public class halvedTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int oldWidth = source.getWidth();
            int oldHeight = source.getHeight();
            int newWidth = (int) (oldWidth / 1.5);
            int newHeight = (int) (oldHeight / 1.5);

            Bitmap result = Bitmap.createScaledBitmap(source, newWidth, newHeight, false);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "halved()";
        }
    }

    public class halvedNotCinemaTransformation implements Transformation {

        @Override
        public Bitmap transform(Bitmap source) {
            int oldWidth = source.getWidth();
            int oldHeight = source.getHeight();
            int newWidth = oldWidth;
            int newHeight = oldHeight;
            if (oldHeight >= 300) {
                newWidth = (int) (oldWidth / 1.3);
                newHeight = (int) (oldHeight / 1.3);
            }
            Bitmap result = Bitmap.createScaledBitmap(source, newWidth, newHeight, false);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "halvedNotCinema()";
        }
    }

    //статичный метод для получения фрагментом адреса страницы детализации от активности
    public static EventPageFragment newInstance(String contentUrl) {

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EVENT_URL, contentUrl);
        EventPageFragment fragment = new EventPageFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
