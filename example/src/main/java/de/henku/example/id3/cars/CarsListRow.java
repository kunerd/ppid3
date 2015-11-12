package de.henku.example.id3.cars;

import java.util.HashMap;

/**
 * Created by kunerd on 15.10.15.
 */
public class CarsListRow extends HashMap<String, String> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CarsListRow(
            String buying,
            String maint,
            String doors,
            String persons,
            String lug_boot,
            String safety,
            String classValue) {

        this.put("buying", buying);
        this.put("maint", maint);
        this.put("doors", doors);
        this.put("persons", persons);
        this.put("lug_boot", lug_boot);
        this.put("safety", safety);
        this.put("classValue", classValue);

    }
}
