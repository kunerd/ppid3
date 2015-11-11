package de.henku.example.id3.weather;

import java.util.HashMap;

public class ListRow extends HashMap<String, String> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ListRow(
            String day,
            String outlook,
            String temperature,
            String humidity,
            String wind,
            String playBall) {

        this.put("day", day);
        this.put("outlook", outlook);
        this.put("temperature", temperature);
        this.put("humidity", humidity);
        this.put("wind", wind);
        this.put("playBall", playBall);
    }
}
