package net.ripe.rpki.validator3.config;

import lombok.extern.slf4j.Slf4j;
import net.ripe.rpki.commons.validation.ValidationOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.format.DateTimeParseException;

@Slf4j
@Configuration
public class ValidationConfig {
    /**
     * Configured using JDK 8 time period syntax because that is what the other config properties use.
     */
    @Bean
    public ValidationOptions validationOptions(
            @Value("${rpki.validator.stalecrl.grace.duration:P2D}") String crlGracePeriodString,
            @Value("${rpki.validator.stalemft.grace.duration:P5D}") String manifestGracePeriodString
    ) {
        final Duration crlGracePeriod = Duration.parse(crlGracePeriodString);
        final Duration manifestGracePeriod = Duration.parse(manifestGracePeriodString);

        final ValidationOptions options = new ValidationOptions();
        options.setCrlMaxStalePeriod(org.joda.time.Duration.millis(crlGracePeriod.toMillis()));
        options.setManifestMaxStalePeriod(org.joda.time.Duration.millis(manifestGracePeriod.toMillis()));

        return options;
    }
}
