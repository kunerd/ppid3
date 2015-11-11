package de.henku.example.id3.tic_tac_toe;

import java.util.HashMap;

/**
 * Created by kunerd on 15.10.15.
 */
public class TTTListRow extends HashMap<String, String> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TTTListRow(
            String tl,
            String tm,
            String tr,
            String ml,
            String mm,
            String mr,
            String bl,
            String bm,
            String br,
            String result) {

        this.put("tl", tl);
        this.put("tm", tm);
        this.put("tr", tr);
        this.put("ml", ml);
        this.put("mm", mm);
        this.put("mr", mr);
        this.put("bl", bl);
        this.put("bm", bm);
        this.put("br", br);
        this.put("result", result);

    }
}
