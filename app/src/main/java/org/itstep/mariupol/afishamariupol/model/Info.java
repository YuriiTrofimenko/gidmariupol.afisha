package org.itstep.mariupol.afishamariupol.model;

/**
 * модель данных для одной строки для вкладки Инфо экрана детализации (для фильмов)
 */
public class Info {

    private String mLabel; //характеристика
    private String mData; //значение характеристики

    public Info(String label, String data) {
        mLabel = label;
        mData = data;
    }

    public Info() {
        new Info("", "");
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }
}
