package com.github.mkram17.bazaarutils.config.data;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;

@Category("metadata_config")
@ConfigOption.Hidden
public final class MetadataConfig {
    @ConfigEntry(
            id = "mod_version",
            translation = "bazaarutils.config.meta.modVersion.value"
    )
    @ConfigOption.Hidden
    public static String MOD_VERSION = "";

    @ConfigEntry(
            id = "resources_sha",
            translation = "bazaarutils.config.meta.resourcesSha.name"
    )
    @ConfigOption.Hidden
    public static String RESOURCES_SHA = "";

    @ConfigEntry(
            id = "is_first_load",
            translation = "bazaarutils.config.meta.isFirstLoad.name"
    )
    @ConfigOption.Hidden
    public static boolean isFirstLoad = true;
}
