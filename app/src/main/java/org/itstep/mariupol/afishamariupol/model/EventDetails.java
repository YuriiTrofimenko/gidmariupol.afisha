package org.itstep.mariupol.afishamariupol.model;

import android.text.Spanned;

import java.util.ArrayList;

/**
 * модель данных одного события для экрана детализации
 */
public class EventDetails {
    private String mHeader; //заголовок
    private String mContent; //описание
    private Spanned mSpannedContent; //описание без потери форматирования
    private String mAdditionalString; //дополнительное описание
    private String mImageUrlString; //адрес превью постера
    private String mVideoThumbCodeString; //адрес превью видео
    private boolean mIsCinema; //признак типа детализации "КИНО"
    private String mAgeLimit; //ограничение по возрасту
    private ArrayList<Info> mInfoList; //список строк с характеристиками кино
    private ArrayList<Session> mSessionList; //список объектов модели "сеанс"
    private ArrayList<String> mNextSessionHeaderList; //список строк с подзаголовками блоков сеансов на следующие дни
    private ArrayList<ArrayList<Session>> mNextSessionBlockList; //список блоков сеансов на следующие дни

    public ArrayList<String> getNextSessionHeaderList() {
        return mNextSessionHeaderList;
    }

    public void setNextSessionHeaderList(ArrayList<String> nextSessionHeaderList) {
        mNextSessionHeaderList = nextSessionHeaderList;
    }

    public ArrayList<ArrayList<Session>> getNextSessionBlockList() {
        return mNextSessionBlockList;
    }

    public void setNextSessionList(ArrayList<ArrayList<Session>> nextSessionBlockListList) {
        mNextSessionBlockList = nextSessionBlockListList;
    }

    public String getAdditionalString() {
        return mAdditionalString;
    }

    public void setAdditionalString(String additionalString) {
        mAdditionalString = additionalString;
    }

    public Spanned getSpannedContent() {
        return mSpannedContent;
    }

    public void setSpannedContent(Spanned spannedContent) {
        mSpannedContent = spannedContent;
    }

    public ArrayList<Session> getSessionList() {
        return mSessionList;
    }

    public void setSessionList(ArrayList<Session> sessionList) {
        mSessionList = sessionList;
    }

    public ArrayList<Info> getInfoList() {
        return mInfoList;
    }

    public void setInfoList(ArrayList<Info> infoList) {
        mInfoList = infoList;
    }

    public String getAgeLimit() {
        return mAgeLimit;
    }

    public void setAgeLimit(String ageLimit) {
        mAgeLimit = ageLimit;
    }

    public boolean isCinema() {
        return mIsCinema;
    }

    public void setIsCinema(boolean isCinema) {
        mIsCinema = isCinema;
    }

    public String getVideoThumbCodeString() {
        return mVideoThumbCodeString;
    }

    public void setVideoThumbCodeString(String videoThumbCodeString) {
        mVideoThumbCodeString = videoThumbCodeString;
    }

    public String getHeader() {
        return mHeader;
    }

    public void setHeader(String header) {
        mHeader = header;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getImageUrlString() {
        return mImageUrlString;
    }

    public void setImageUrlString(String imageUrlString) {
        mImageUrlString = imageUrlString;
    }
}
