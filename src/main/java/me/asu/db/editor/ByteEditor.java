package me.asu.db.editor;

import java.beans.PropertyEditorSupport;

public class ByteEditor extends PropertyEditorSupport {
        @Override
        public void setValue(Object value) {
            if (value instanceof Byte) {
                super.setValue(value);
            } else if (value instanceof Number) {
                super.setValue(Byte.valueOf(((Number) value).byteValue()));
            } else {
                throw new java.lang.IllegalArgumentException(""+value);
            }
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            final Byte a = Byte.valueOf(text);
            setValue(a);
        }
    }
