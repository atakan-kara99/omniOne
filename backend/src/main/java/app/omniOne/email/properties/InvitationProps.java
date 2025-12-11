package app.omniOne.email.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invitation")
public record InvitationProps(

        String url,

        String path,

        String subject

) {}
