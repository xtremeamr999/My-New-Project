package com.github.mkram17.bazaarutils.features.util;

import dev.isxander.yacl3.api.ConfigCategory;

public interface ConfigurableFeature {
    /**
     * Adds this feature's settings to the provided category builder.
     * Implementations can choose to add a single .option(), multiple .options(),
     * or a .group() depending on complexity.
     */
    void createOption(ConfigCategory.Builder builder);
}
