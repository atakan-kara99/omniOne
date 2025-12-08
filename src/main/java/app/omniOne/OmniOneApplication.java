package app.omniOne;

import app.omniOne.email.properties.ActivationProps;
import app.omniOne.email.properties.InvitationProps;
import app.omniOne.email.properties.ResetPasswordProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ActivationProps.class, InvitationProps.class, ResetPasswordProps.class})
public class OmniOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(OmniOneApplication.class, args);
	}

}
