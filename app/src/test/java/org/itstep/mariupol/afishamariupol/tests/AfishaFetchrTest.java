package org.itstep.mariupol.afishamariupol.tests;

import org.itstep.mariupol.afishamariupol.AfishaFetchr;
import org.itstep.mariupol.afishamariupol.model.EventDetails;
import org.itstep.mariupol.afishamariupol.model.EventsItem;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

/**
 * Модульный тест получения объекта "детализация события"
 */

public class AfishaFetchrTest {

    @Test
    public void testEventGetter() throws IOException {
        //Пытаемся получить по сети страницу детализации события
        EventDetails eventDetails = new AfishaFetchr()
                .fetchEventDetails("http://afisha.gidmariupol.com/films/view/id/2132/day/0");
        assertNotNull(eventDetails);
    }

    @Test
    public void testEventsGetter() throws IOException {
        //Пытаемся получить по сети страницу со списком событий
        ArrayList<EventsItem> eventsItemsArrayList = new AfishaFetchr()
                .fetchEventsItems(0, -1, 0);
        assertNotNull(eventsItemsArrayList);
    }
}
