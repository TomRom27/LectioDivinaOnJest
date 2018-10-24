package com.tr.onjestslowo.service;

import android.content.Context;

import com.tr.onjestslowo.model.ShortContemplationsFile;
import com.tr.tools.IOHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by bpl2111 on 2016-04-19.
 */
public class ShortContemplationDataSource {
    private Context context;

    public ShortContemplationDataSource(Context context) {
        this.context = context;
    }

    public String defaultDestinationFolder() {
        String folder = IOHelper.getExternalPublicDownloadDir(context);
        if (folder!= null)
            return folder;
        else
            return IOHelper.getSystemDefaultDownloadDir(context);
    }

    public ArrayList<ShortContemplationsFile> getAllFrom(String path)
            throws Exception {


        ArrayList<ShortContemplationsFile> list = new ArrayList<ShortContemplationsFile>();

        File directory = new File(path);
        if (directory.exists() && directory.isDirectory()) {
            final Pattern p = Pattern.compile(ShortContemplationsFile.FileNameRegEx);
            File[] flists = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    p.matcher(file.getName()).matches();


                    return p.matcher(file.getName()).matches();

                }
            });
            for (File f : flists)
                list.add(new ShortContemplationsFile(f.getPath()));
        }
        return list;
    }

    public ArrayList<ShortContemplationsFile> getAll()
            throws Exception {
        return getAllFrom(defaultDestinationFolder());
    }

    public void saveFromStream(String fileName, InputStream input) throws IOException {
        String destinationPath = defaultDestinationFolder();

        saveFromStream(fileName, destinationPath, input);
    }

    public void saveFromStream(String fileName, String destinationPath, InputStream input) throws IOException {

        OutputStream output = null;
        try {
            output = new FileOutputStream(combine(destinationPath, fileName));

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (Exception ignored) {
            }

        }
    }

    private String combine(String path, String fileName) {
        return new File(path, fileName).toString();
    }
}
