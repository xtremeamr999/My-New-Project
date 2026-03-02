package com.github.mkram17.bazaarutils.config;

import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class BUModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigUtil::createGUI;
    }
}