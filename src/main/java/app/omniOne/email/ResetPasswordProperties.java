package app.omniOne.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reset-password")
public record ResetPasswordProperties(

        String from,

        String url,

        String path,

        String subject

) {}
