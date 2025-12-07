package app.omniOne.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "activation")
public record ActivationProperties(

        String from,

        String url,

        String path,

        String subject

) {}
