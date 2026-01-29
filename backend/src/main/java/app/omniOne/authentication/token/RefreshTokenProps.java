package app.omniOne.authentication.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "refresh-token")
public record RefreshTokenProps(

        int ttlDays,

        boolean secure,

        String sameSite

) {}
