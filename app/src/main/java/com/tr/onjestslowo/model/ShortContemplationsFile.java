package com.tr.onjestslowo.model;

import com.tr.tools.DateHelper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bpl2111 on 2016-04-18.
 */
public class ShortContemplationsFile {

    public ShortContemplationsFile(String fullName)
            throws ParseException
    {
        FilePath = fullName;
        FileName = new File(fullName).getName();
        FirstDate = getDateFromName(FileName);
        LastDate = DateHelper.addDay(FirstDate, 6);
    }

    public String FileName;
    public String FilePath;
    public Date FirstDate;
    public Date LastDate;

    @Override
    public String toString() {
        return FileName;
    }


    static String fileNameDatePattern="yyMMdd";
    // rk160526_br
    public static Date getDateFromName(String fileName)
    throws ParseException {
        if (fileName.length()!=11)
            throw new IllegalArgumentException("Incorrect name, must be at like rk160526_br");

        SimpleDateFormat dateFormat = new SimpleDateFormat(fileNameDatePattern);
        String datePart = fileName.substring(2, 6);
        return dateFormat.parse(datePart);
    }

    static String fileNamePattern = "'rk'yyMMdd'_br.pdf'";

    public static String getFileNameFromDate(Date sundayDate) {
        return DateHelper.toString(fileNamePattern, sundayDate);
    }


}
