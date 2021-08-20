package me.asu.db.editor;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;

public class DateEditor extends PropertyEditorSupport {
        @Override
        public void setValue(Object value) {
            if (value instanceof java.util.Date) {
                super.setValue(value);
            } else {
                java.util.Date d = parseDate(value.toString());
                super.setValue(d);
            }
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            java.util.Date d = parseDate(text);
            setValue(d);
        }
        private java.util.Date parseDate(String arg) {
            SimpleDateFormat[] sdfArr = {
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                    new SimpleDateFormat("yyyy-MM-dd"),
                    new SimpleDateFormat("HH:mm:ss"),
                    };
            java.util.Date d = null;
            for (int j = 0, sdfArrLength = sdfArr.length; j < sdfArrLength; j++) {
                SimpleDateFormat sdf = sdfArr[j];
                try {
                    d = sdf.parse(arg);
                } catch (Exception e) {
                    // ignore
                }
            }
            return d;
        }
    }