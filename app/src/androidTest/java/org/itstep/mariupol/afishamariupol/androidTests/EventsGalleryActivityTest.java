package org.itstep.mariupol.afishamariupol.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.Button;

import org.itstep.mariupol.afishamariupol.EventsGalleryActivity;
import org.itstep.mariupol.afishamariupol.EventsGalleryFragment;
import org.itstep.mariupol.afishamariupol.R;

import java.util.ArrayList;

/**
 * Андроид-тест наличия кнопок типа события на главном экране и наличия надписей на них
 */
public class EventsGalleryActivityTest extends ActivityInstrumentationTestCase2 <EventsGalleryActivity> {

    private EventsGalleryActivity mEventsGalleryActivity;
    private EventsGalleryFragment mEventsGalleryFragment;

    private  Button mEventsGalleryAllButton;
    private Button mEventsGalleryCinemaButton;
    private Button mEventsGalleryTheatreButton;
    private Button mEventsGalleryClubsButton;
    private Button mEventsGalleryShowsButton;
    private Button mEventsGalleryBusinessButton;
    private Button mEventsGallerySportButton;
    private Button mEventsGalleryFreeButton;

    private ArrayList<Button> mEventGalleryButtonsList;

    private boolean mEventsGalleryButtonsNotNull = true;
    private boolean mEventsGalleryButtonsTextsNotEmpty = true;

    public EventsGalleryActivityTest() {
        super(EventsGalleryActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mEventsGalleryActivity = (EventsGalleryActivity) getActivity();
        mEventsGalleryFragment = EventsGalleryFragment.newInstance(0);

        //View parentView = mEventsGalleryFragment.getView();
        View view = getActivity()
                .getLayoutInflater()
                .inflate(R.layout.fragment_events_gallery, null);

        mEventsGalleryAllButton = (Button)view.findViewById(R.id.eventsGalleryAllButton);
        mEventsGalleryCinemaButton = (Button)view.findViewById(R.id.eventsGalleryCinemaButton);
        mEventsGalleryTheatreButton = (Button)view.findViewById(R.id.eventsGalleryTheatreButton);
        mEventsGalleryClubsButton = (Button)view.findViewById(R.id.eventsGalleryClubsButton);
        mEventsGalleryShowsButton = (Button)view.findViewById(R.id.eventsGalleryShowsButton);
        mEventsGalleryBusinessButton = (Button)view.findViewById(R.id.eventsGalleryBusinessButton);
        mEventsGallerySportButton = (Button)view.findViewById(R.id.eventsGallerySportButton);
        mEventsGalleryFreeButton = (Button)view.findViewById(R.id.eventsGalleryFreeButton);

        mEventGalleryButtonsList = new ArrayList<>();
        mEventGalleryButtonsList.add(mEventsGalleryAllButton);
        mEventGalleryButtonsList.add(mEventsGalleryCinemaButton);
        mEventGalleryButtonsList.add(mEventsGalleryTheatreButton);
        mEventGalleryButtonsList.add(mEventsGalleryClubsButton);
        mEventGalleryButtonsList.add(mEventsGalleryShowsButton);
        mEventGalleryButtonsList.add(mEventsGalleryBusinessButton);
        mEventGalleryButtonsList.add(mEventsGallerySportButton);
        mEventGalleryButtonsList.add(mEventsGalleryFreeButton);
    }

    @SmallTest
    public void testEventsGalleryButtonsNotNullNotEmpty () {
        if (mEventGalleryButtonsList != null) {
            for (Button button : mEventGalleryButtonsList) {
                if (button == null) {
                    mEventsGalleryButtonsNotNull = false;
                } else {
                    if (button.getText().toString().equals("")) {
                        mEventsGalleryButtonsTextsNotEmpty = false;
                    }
                }
            }
        } else {
            mEventsGalleryButtonsNotNull = false;
        }

        assertTrue(mEventsGalleryButtonsNotNull);
        if (mEventsGalleryButtonsNotNull == true) {
            assertTrue(mEventsGalleryButtonsTextsNotEmpty);
        }
    }
}
