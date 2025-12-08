package app.omniOne.email.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reset-password")
public record ResetPasswordProps(

        String from,

        String url,

        String path,

        String subject

) {}
