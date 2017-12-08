package org.itstep.mariupol.afishamariupol.model;

/**
 * модель данных одного события для главного экрана
 */
public class EventsItem {
    private String mCaption; //заголовок
    private String mContentUrlString; // адрес страницы детализации события
    private String mImageUrlString; //адрес превью постера
    private String mTitle; //тэги одтипов события - Horror, action, comedy, ...
    private String mEventType; //тип события - КИНО, ТЕАТР и т.д.
    private String mAgeLimit; //ограничение по возрасту

    public String getAgeLimit() {
        return mAgeLimit;
    }

    public void setAgeLimit(String ageLimit) {
        mAgeLimit = ageLimit;
    }

    public String getEventType() {
        return mEventType;
    }

    public void setEventType(String eventType) {
        mEventType = eventType;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getContentUrlString() {
        return mContentUrlString;
    }

    public void setContentUrlString(String contentUrlString) {
        mContentUrlString = contentUrlString;
    }

    public String getImageUrlString() {
        return mImageUrlString;
    }

    public void setImageUrlString(String imageUrlString) {
        mImageUrlString = imageUrlString;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
