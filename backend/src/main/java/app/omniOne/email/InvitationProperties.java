package app.omniOne.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invitation")
public record InvitationProperties(

        String from,

        String url,

        String text,

        String subject

) {}
