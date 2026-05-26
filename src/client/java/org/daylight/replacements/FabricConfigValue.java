package org.daylight.replacements;

import org.daylight.config.SimpleConfig;
import org.daylight.replacements.common.IConfigValue;

public class FabricConfigValue<T> implements IConfigValue<T> {
    private final SimpleConfig config;
    private final String key;
    private final T defaultValue;
    private T cachedValue = null;

    public T getCached() {
        if(cachedValue == null) cachedValue = get();
        return cachedValue;
    }

    public FabricConfigValue(SimpleConfig config, String key, T defaultValue) {
        this.config = config;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        Object raw = config.get(key, defaultValue);
        if (raw == null) {
            return defaultValue;
        }

        if (defaultValue instanceof Integer && raw instanceof Number num) {
            cachedValue = (T) Integer.valueOf(num.intValue());
            return cachedValue;
        }
        if (defaultValue instanceof Boolean && raw instanceof Boolean b) {
            cachedValue = (T) b;
            return cachedValue;
        }
        if (defaultValue instanceof Double && raw instanceof Number num) {
            cachedValue = (T) Double.valueOf(num.doubleValue());
            return cachedValue;
        }
        if (defaultValue instanceof String && !(raw instanceof String)) {
            cachedValue = (T) raw.toString();
            return cachedValue;
        }
        if (defaultValue instanceof Enum<?> defaultEnum && !(raw.getClass().isEnum())) {
            cachedValue = (T) Enum.valueOf(defaultEnum.getDeclaringClass(), raw.toString());
            return cachedValue;
        }

        // fallback
        try {
            cachedValue = (T) raw;
            return cachedValue;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    @Override
    public void set(T value) {
        config.set(key, value);
        cachedValue = value;
    }

    @Override
    public void save() {
        config.save();
    }
}
