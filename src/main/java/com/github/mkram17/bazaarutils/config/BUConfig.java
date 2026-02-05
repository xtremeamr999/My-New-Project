package com.github.mkram17.bazaarutils.config;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.data.GeneralDataConfig;
import com.github.mkram17.bazaarutils.config.data.MetadataConfig;
import com.github.mkram17.bazaarutils.config.developer.DeveloperConfig;
import com.github.mkram17.bazaarutils.config.feature.FeatureConfig;
import com.teamresourceful.resourcefulconfig.api.annotations.Config;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.loader.Configurator;

@ConfigInfo(title = "Bazaar Utils",
        description = "A QOL mod for Hypixel Skyblock focused on the bazaar.",
        links = {
                @ConfigInfo.Link(
                        value = "https://modrinth.com/mod/bazaar-utils",
                        icon = "modrinth",
                        text = "Modrinth"
                )})
@Config(value = BazaarUtils.MODID)
public final class BUConfig {

    private static final BUConfig INSTANCE = new BUConfig();
    public static final Configurator CONFIGURATOR = new Configurator(BazaarUtils.MODID);

    public GeneralDataConfig general = new GeneralDataConfig();
    public MetadataConfig metadata = new MetadataConfig();
    public DeveloperConfig developer = new DeveloperConfig();
    public FeatureConfig feature = new FeatureConfig();


    public BUConfig get(){
        return INSTANCE;
    }
}
