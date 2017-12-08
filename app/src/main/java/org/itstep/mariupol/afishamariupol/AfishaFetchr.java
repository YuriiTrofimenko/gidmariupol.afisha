package org.itstep.mariupol.afishamariupol;

import android.text.Html;
import android.util.Log;

import org.itstep.mariupol.afishamariupol.model.EventDetails;
import org.itstep.mariupol.afishamariupol.model.EventsItem;
import org.itstep.mariupol.afishamariupol.global.Globals;
import org.itstep.mariupol.afishamariupol.model.Info;
import org.itstep.mariupol.afishamariupol.model.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * класс загрузки данных из сети и их парсинга
 */

public class AfishaFetchr {
    public static final String TAG = "AfishaFetchr";
    protected static final String AFISHA_GID_MARIUPOL_URL = "http://afisha.gidmariupol.com";

	//метод загрузки страницы сайта по ее адресу в виде массива байт
    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                return null;
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0)
                out.write(buffer, 0, bytesRead);
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

	//метод получения строки из массива байт
    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //метод вызова загрузки данных для главного экрана (галлереи событий) в коллекцию-список с указанием дня и типа событий
    public ArrayList<EventsItem> fetchEventsItems(int day, int type, int page) throws IOException {
        String url;
        if (type == -1) {
			//адрес страницы событий, когда выбраны все типы
            url = AFISHA_GID_MARIUPOL_URL + "/index/index/day/" + day;
        } else {
			//адрес страницы событий, когда выбран один из типов
            url = AFISHA_GID_MARIUPOL_URL + "/rubr/view/id/" + type + "/day/" + day;
        }
        return downloadEventsItems(url);
    }

	//метод ВЫЗОВА загрузки данных для экрана детализации в единичный объект модели "детализация события"
	//с указанием адреса страницы детализации на сайте
    public EventDetails fetchEventDetails(String eventURL) throws IOException {
        //String url = Uri.parse(eventURL).buildUpon().build().toString();
        String url = eventURL;
        return downloadEventDetails(url);
    }

	//метод загрузки данных для главного экрана (галлереи событий)
    public ArrayList<EventsItem> downloadEventsItems(String url) throws IOException {
        ArrayList<EventsItem> eventItems = new ArrayList<EventsItem>();
        try {
			//получаем результат запроса в виде строки
            String htmlString = getUrl(url);
			//создаем объект-документ из всего результата запроса
            Document document = Jsoup.parse(htmlString);
			//создаем объект-коллекцию элементов Jsoup
            Elements elementEventsItems = document.select("div#graph-content > div");
			//ищем в полном документе строку об отсутствии событий, если нашли -
			//записываем ее в статичное поле класса глобальных переменных,
			//если нет - запускаем заполнение коллекции-списка объектами модели "событие для главного экрана"
			//из коллекции элементов Jsoup
            String noEventsString = document.select("div.l-content > div.b-text").text();
            if (noEventsString == "") {
				//передаем методу-парсеру пустую коллекцию-список событий и
				//заполненную коллекцию элементов Jsoup
                parseEventsItems(eventItems, elementEventsItems);
            } else {
                Globals.setNoEventsString(noEventsString);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch events items", ioe);
            throw ioe;
        }
        return eventItems;
    }

	//метод загрузки данных для экрана детализации
    public EventDetails downloadEventDetails(String url) throws IOException {
        EventDetails eventDetails = new EventDetails();
        try {
            String htmlString = getUrl(url);
            Document documentEventDetails = Jsoup.parse(htmlString);
			//передаем методу-парсеру пустой объект модели "событие для экрана детализации" и
			//заполненную коллекцию элементов Jsoup
            parseEventDetails(eventDetails, documentEventDetails);
        } catch (ConnectException cex) {
            throw cex;
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch event details", ioe);
            throw ioe;
        }
        return eventDetails;
    }

	//метод парсинга для главного экрана (галлереи событий)
    void parseEventsItems(ArrayList<EventsItem> eventsItemsArrayList, Elements eventElements) {
        //флаг присутствия строки "скоро"
		boolean soonEventsCaptionDetected = false;
        for (int i_elements = 1; i_elements < eventElements.size(); i_elements++) {
            if (eventElements.get(i_elements).select("h1").text() != "") {
                if (soonEventsCaptionDetected == false) {
                    soonEventsCaptionDetected = true;
                } else {
                    break;
                }
            }
			//если элемент содержит превью постера, значит он не пустой - заполняем из него один объект модели
            String noVoidString = eventElements.get(i_elements).select("img").attr("src");
            if (noVoidString != "") {
                EventsItem eventsItem = new EventsItem();
                eventsItem.setCaption(eventElements.get(i_elements).select("table.type td a").text());
                eventsItem.setContentUrlString(eventElements.get(i_elements).select("table.type td a").attr("href"));
                eventsItem.setImageUrlString(eventElements.get(i_elements).select("img").attr("src"));
                eventsItem.setEventType(eventElements.get(i_elements).className());
                eventsItem.setAgeLimit(eventElements.get(i_elements).select("div.b-card__age_limit").text());
                eventsItem.setTitle(eventElements.get(i_elements).select("div.body div.title").text());
				//добавляем заполненный объект модели в коллекцию-список
                eventsItemsArrayList.add(eventsItem);
            }
        }
    }

	//метод парсинга для экрана детализации
    void parseEventDetails(EventDetails eventDetails, Document documentEventDetails) {

        //общие элементы - заголовок и ограничение по возрасту
        eventDetails.setHeader(documentEventDetails.select("div.b-card-full__header h1").text());
        eventDetails.setAgeLimit(documentEventDetails.select("div.b-card-full__age_limit").text());

        //элементы для детализации кино
        String tabTitleElementString = documentEventDetails.select("a.m-session").text();
        if (tabTitleElementString != "") {
            eventDetails.setIsCinema(true);

            // вкладка Info
            Elements infoElements = documentEventDetails.select("table.right > tbody > tr");
            ArrayList<Info> infoList = new ArrayList<>();
            for (int i_elements = 0; i_elements < infoElements.size(); i_elements++) {
                Info info = new Info();
                info.setLabel(infoElements.get(i_elements).select("td.label").text());
                info.setData(infoElements.get(i_elements).select("td.data").text());
                infoList.add(info);
            }
            eventDetails.setInfoList(infoList);

            // вкладка содержание
            String fullContentString = documentEventDetails.select("div.content").text();
            int indexOfIframe = fullContentString.indexOf("<iframe");
            String outContentString = "";
            if (indexOfIframe > -1 && indexOfIframe > 0) {
				//убираем лишнее содержимое из строки контента
                outContentString = fullContentString.substring(0, indexOfIframe);
            } else {
                outContentString = fullContentString;
            }
            eventDetails.setContent(outContentString);
			//выделяем из адреса видео только его код
            String videoCodeString = documentEventDetails
                    .select("iframe")
                    .attr("src")
                    .replaceAll("https://www.youtube.com/embed/", "");
            eventDetails.setVideoThumbCodeString(videoCodeString);

            // вкладка сеансы
            eventDetails.setImageUrlString(documentEventDetails
                    .select("div.session > div.image > img")
                    .attr("src"));
            Elements sessionElements = documentEventDetails.select("div.session tbody > tr");
            ArrayList<Session> sessionList = new ArrayList<>();
            for (int i_elements = 0; i_elements < sessionElements.size(); i_elements++) {
                Session session = new Session();
                session.setPlace(sessionElements.get(i_elements).select("td.place a").text());
                Elements timeElements = sessionElements.get(i_elements).select("td.time > div");
                StringBuilder timesString = new StringBuilder();
                for (int j_elements = 0; j_elements < timeElements.size(); j_elements++) {
                    String fullString = timeElements.get(j_elements).text();
                    int fullStringLength = fullString.length();
                    String outString = "";
					//удаляем лишние элементы из строк сеансов
                    if (fullStringLength >= 5) {
                        outString = fullString.substring(0, 5);
                    } else {
                        outString = fullString.substring(0, 2);
                    }
                    timesString.append(outString);
                }
                session.setTimesString(timesString.toString());
                sessionList.add(session);
            }
            eventDetails.setSessionList(sessionList);
			//если найден заголовок "расписание" - парсим расписание на следующие дни
            if (documentEventDetails.select("div.list-days > div.title").text() != "") {
                //собираем подзаголовки - даты сеансов в коллекцию-список
                Elements nextSessionHeaderElements = documentEventDetails.select("div.list-days h3");
                ArrayList<String> nextSessionHeaderList = new ArrayList<>();
                for (int i_elements = 0; i_elements < nextSessionHeaderElements.size(); i_elements++) {
                    nextSessionHeaderList.add(nextSessionHeaderElements.get(i_elements).text());
                }
                eventDetails.setNextSessionHeaderList(nextSessionHeaderList);
                //собираем блоки сеансов в коллекцию-список объектов модели "сеанс"
                Elements nextSessionBlockElements = documentEventDetails
                        .select("div.list-days > table");
                ArrayList<ArrayList<Session>> nextSessionBlockList = new ArrayList<>();
                for (int i_sessionBlockElements = 0;
                     i_sessionBlockElements < nextSessionBlockElements.size();
                     i_sessionBlockElements++) {
                    Elements nextSessionElements = nextSessionBlockElements.select("tbody > tr");
                    ArrayList<Session> nextSessionList = new ArrayList<>();
                    for (int j_sessionElements = 0; j_sessionElements < sessionElements.size(); j_sessionElements++) {
                        Session session = new Session();
                        session.setPlace(sessionElements.get(j_sessionElements).select("td.place a").text());
                        Elements timeElements = sessionElements.get(j_sessionElements).select("td.time > div");
                        StringBuilder timesString = new StringBuilder();
                        for (int k_timeElements = 0; k_timeElements < timeElements.size(); k_timeElements++) {
                            String fullString = timeElements.get(k_timeElements).text();
                            int fullStringLength = fullString.length();
                            String outString = "";
                            if (fullStringLength >= 5) {
                                outString = fullString.substring(0, 5);
                            } else {
                                outString = fullString.substring(0, 2);
                            }
                            timesString.append(outString);
                        }
                        session.setTimesString(timesString.toString());
                        nextSessionList.add(session);
                    }
                    nextSessionBlockList.add(nextSessionList);
                }
                eventDetails.setNextSessionList(nextSessionBlockList);
            }
        } else {
            // элементы для детализации не-кино
            eventDetails.setIsCinema(false);
            eventDetails.setImageUrlString(documentEventDetails
                    .select("div.image.body > img")
                    .attr("src"));
            ArrayList<Session> sessionList = new ArrayList<>();
            Session session = new Session();
            session.setPlace(documentEventDetails
                    .select("div.image.body div.place a")
                    .text());
            session.setTimesString(documentEventDetails.select("div.time").text());
            sessionList.add(session);
            eventDetails.setSessionList(sessionList);
            eventDetails.setSpannedContent(Html
                    .fromHtml(documentEventDetails
                            .select("div.image.body > p")
                            .toString()
                            .replaceAll("<br><br> <br><br>", "")
                            .replaceAll("<br><p>", "<p>")
                            .replaceAll("<p>", "")
                            .replaceAll("</p>", "")));
            eventDetails.setAdditionalString(documentEventDetails
                    .select("div[style=float:left; margin: 10px 0 0 10px; width: 250px;]")
                    .text());
        }
    }
}
