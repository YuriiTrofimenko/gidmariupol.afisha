package org.itstep.mariupol.afishamariupol.model;

/**
 * модель данных для заголовков владок по дням на главном экране
 */
public class Day {

    private int mDayIndex; //порядковый номер вкладки, с 0
    private String mDayName; //название дня
    private String mDayNumber; // число - номер дня месяца + номер месяца в году

    public Day(int dayIndex, String dayName, String dayNumber) {
        mDayIndex = dayIndex;
        mDayName = dayName;
        mDayNumber = dayNumber;
    }

    public Day() {
        new Day(0, "", "");
    }

    public int getDayIndex() {
        return mDayIndex;
    }

    public void setDayIndex(int dayIndex) {
        mDayIndex = dayIndex;
    }

    public String getDayName() {
        return mDayName;
    }

    public void setDayName(String dayName) {
        mDayName = dayName;
    }

    public String getDayNumber() {
        return mDayNumber;
    }

    public void setDayNumber(String dayNumber) {
        mDayNumber = dayNumber;
    }
}
