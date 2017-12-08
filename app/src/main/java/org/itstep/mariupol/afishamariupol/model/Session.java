package org.itstep.mariupol.afishamariupol.model;

/**
 * модель данных одного сеанса для экрана детализации
 */
public class Session {

    private String mPlace; //место события
    private String mTimesString; //время событияы

    public String getPlace() {
        return mPlace;
    }

    public void setPlace(String place) {
        mPlace = place;
    }

    public String getTimesString() {
        return mTimesString;
    }

    public void setTimesString(String timesString) {
        mTimesString = timesString;
    }
}
