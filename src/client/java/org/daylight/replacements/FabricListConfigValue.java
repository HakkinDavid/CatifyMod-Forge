package org.daylight.replacements;

import org.daylight.config.SimpleConfig;
import org.daylight.replacements.common.IListConfigValue;

import java.util.List;

public class FabricListConfigValue<T> extends FabricConfigValue<List<T>> implements IListConfigValue<T> {
    public FabricListConfigValue(SimpleConfig config, String key, List<T> defaultValue) {
        super(config, key, defaultValue);
    }
}