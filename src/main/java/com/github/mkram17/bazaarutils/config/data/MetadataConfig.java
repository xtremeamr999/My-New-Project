package com.github.mkram17.bazaarutils.config.data;

import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;

public class MetadataConfig {
    @ConfigEntry(id = "mod_version")
    public String MOD_VERSION = "";
    @ConfigEntry(id = "is_first_load")
    public boolean isFirstLoad = true;
    @ConfigEntry(id = "resources_sha")
    public String resourcesSha = "";
}
