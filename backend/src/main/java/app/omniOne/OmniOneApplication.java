package app.omniOne;

import app.omniOne.email.ActivationProperties;
import app.omniOne.email.InvitationProperties;
import app.omniOne.email.ResetPasswordProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ActivationProperties.class, InvitationProperties.class, ResetPasswordProperties.class})
public class OmniOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(OmniOneApplication.class, args);
	}

}
