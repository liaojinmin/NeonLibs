package me.neon.libs.service;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.Expose;

public final class Exclude implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return fieldAttributes.getAnnotation(Expose.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {

        return aClass.getAnnotation(Expose.class) != null;
    }
}
