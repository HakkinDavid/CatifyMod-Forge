package org.daylight.replacements;

import org.daylight.config.SimpleConfig;
import org.daylight.replacements.common.IIntConfigValue;

public class FabricIntegerConfigValue extends FabricConfigValue<Integer> implements IIntConfigValue {
    public FabricIntegerConfigValue(SimpleConfig config, String key, Integer defaultValue) {
        super(config, key, defaultValue);
    }
}