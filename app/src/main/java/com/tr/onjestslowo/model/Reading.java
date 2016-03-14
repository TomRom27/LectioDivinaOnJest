package com.tr.onjestslowo.model;

import java.util.Date;
import java.util.List;

/**
 * Created by bpl2111 on 2014-05-29.
 */
public class Reading implements Comparable {
    public Long Id;
    public String Content;
    public Date DateParsed;
    public String Title;

    @Override
    public int compareTo(Object o) {
        return this.DateParsed.compareTo(((Reading)o).DateParsed);
    }

}
