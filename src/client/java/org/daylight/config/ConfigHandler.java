package org.daylight.config;

import org.daylight.CatSize;
import org.daylight.InvisibilityBehaviour;
import org.daylight.replacements.*;
import org.daylight.replacements.common.*;
import org.daylight.util.WhitelistedScreensUtil;

public class ConfigHandler {
    public static final SimpleConfig CONFIG = new SimpleConfig();

    public static IStringConfigValue catVariant;
    public static IBooleanConfigValue catVariantVanilla;
    public static IBooleanConfigValue replacementActive;
    public static IBooleanConfigValue catDamageVisible;
    public static IBooleanConfigValue catHandActive;
    public static IEnumConfigValue catSize;
    public static IEnumConfigValue invisibilityBehaviour;
    public static IListConfigValue<String> whitelistedScreenNames;

    public static void init() {
        CONFIG.load();

        catVariant = new FabricStringConfigValue(CONFIG, "catVariant", "JELLIE");
        catVariantVanilla = new FabricBooleanConfigValue(CONFIG, "catVariantIsVanilla", true);
        replacementActive = new FabricBooleanConfigValue(CONFIG, "replacementActive", true);
        catHandActive = new FabricBooleanConfigValue(CONFIG, "catHandActive", true);
        catDamageVisible = new FabricBooleanConfigValue(CONFIG, "catDamageVisible", true);
        catSize = new FabricEnumConfigValue(CONFIG, "catSize", CatSize.NORMAL);
        invisibilityBehaviour = new FabricEnumConfigValue(CONFIG, "invisibilityBehaviour", InvisibilityBehaviour.VANILLA);
        whitelistedScreenNames = new FabricListConfigValue<>(CONFIG, "whitelistedAffectedScreens", null);

        if(whitelistedScreenNames.get() == null) WhitelistedScreensUtil.initDefaultWhitelistedScreens();
        else WhitelistedScreensUtil.deserializeClassNames();
    }
}
