package com.uliamar.minilock.ui;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PhraseDictionaryParser {

    private Context mContext;

    public PhraseDictionaryParser(final Context context) {
        mContext = context;
    }

    public String[] getDictionary() {
        String s = readFileContent();
        return s.split(",");
    }

    private String readFileContent() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(mContext.getAssets().open("word_catalog.txt")));

            return reader.readLine();

        } catch (IOException e) {
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
