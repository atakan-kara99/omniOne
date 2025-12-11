package app.omniOne.email.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "activation")
public record ActivationProps(

        String url,

        String path,

        String subject

) {}
