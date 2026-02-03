package app.omniOne.configuration;

import app.omniOne.email.properties.ActivationProps;
import app.omniOne.email.properties.InvitationProps;
import app.omniOne.email.properties.ResetPasswordProps;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        ActivationProps.class, InvitationProps.class, ResetPasswordProps.class})
public class PropertiesConfig {
}
