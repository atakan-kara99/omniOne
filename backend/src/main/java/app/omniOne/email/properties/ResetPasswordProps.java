package app.omniOne.email.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reset-password")
public record ResetPasswordProps(

        String urlPath,

        String filePath,

        String subject,

        int ttlMins

) {}
