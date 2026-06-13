package org.daylight.replacements;

import org.daylight.config.SimpleConfig;
import org.daylight.replacements.common.IIntConfigValue;
import org.daylight.replacements.common.IStringConfigValue;

public class FabricStringConfigValue extends FabricConfigValue<String> implements IStringConfigValue {
    public FabricStringConfigValue(SimpleConfig config, String key, String defaultValue) {
        super(config, key, defaultValue);
    }
}