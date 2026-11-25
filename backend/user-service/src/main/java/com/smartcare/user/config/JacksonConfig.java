package com.smartcare.user.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

/**
 * Configures Jackson deserialization rules for the User Service.
 * Registers {@link StrictStringDeserializer} so non-string JSON values
 * (numbers, booleans, arrays, objects) are rejected with HTTP 400 instead of
 * being coerced into {@link String} fields.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer strictStringCoercionCustomizer() {
        SimpleModule strictStringModule = new SimpleModule();
        strictStringModule.addDeserializer(String.class, new StrictStringDeserializer());
        return builder -> builder.addModule(strictStringModule);
    }
}
