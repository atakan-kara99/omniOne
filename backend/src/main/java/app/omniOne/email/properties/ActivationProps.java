package app.omniOne.email.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "activation")
public record ActivationProps(

        @NotBlank
        String urlPath,

        @NotBlank
        String filePath,

        @NotBlank
        String subject,

        @Positive
        int ttlMins

) {}
