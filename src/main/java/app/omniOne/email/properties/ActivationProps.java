package app.omniOne.email.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "activation")
public record ActivationProps(

        String urlPath,

        String filePath,

        String subject,

        int ttlMins

) {}
