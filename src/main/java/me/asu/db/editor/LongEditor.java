package me.asu.db.editor;

import java.beans.PropertyEditorSupport;

public class LongEditor extends PropertyEditorSupport {
        @Override
        public void setValue(Object value) {
            if (value instanceof Long) {
                super.setValue(value);
            } else if (value instanceof Number) {
                super.setValue(Long.valueOf(((Number) value).longValue()));
            } else {
                throw new java.lang.IllegalArgumentException("" + value);
            }
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            final Long a = Long.valueOf(text);
            setValue(a);
        }
    }
