package app.omniOne.configuration;

import app.omniOne.authentication.token.RefreshTokenProps;
import app.omniOne.email.properties.ActivationProps;
import app.omniOne.email.properties.InvitationProps;
import app.omniOne.email.properties.ResetPasswordProps;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        ActivationProps.class, InvitationProps.class,
        ResetPasswordProps.class, RefreshTokenProps.class})
public class PropertiesConfig {
}
