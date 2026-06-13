package org.daylight.replacements;

import org.daylight.config.SimpleConfig;
import org.daylight.replacements.common.IBooleanConfigValue;
import org.daylight.replacements.common.IIntConfigValue;

public class FabricBooleanConfigValue extends FabricConfigValue<Boolean> implements IBooleanConfigValue {
    public FabricBooleanConfigValue(SimpleConfig config, String key, Boolean defaultValue) {
        super(config, key, defaultValue);
    }
}