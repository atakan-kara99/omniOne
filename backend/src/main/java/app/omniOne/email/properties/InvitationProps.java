package app.omniOne.email.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invitation")
public record InvitationProps(

        String urlPath,

        String filePath,

        String subject,

        int ttlMins

) {}
