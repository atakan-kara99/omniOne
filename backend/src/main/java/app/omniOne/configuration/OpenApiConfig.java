package app.omniOne.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;


@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer")
@OpenAPIDefinition(
        info = @Info(title = "omniOne API", version = "0.0.1"),
        tags = {@Tag(name = "Auth"),
                @Tag(name = "User"),
                @Tag(name = "Coach"),
                @Tag(name = "Coach - Client"),
                @Tag(name = "Coach - Nutrition Plan"),
                @Tag(name = "Coach - Questionnaire"),
                @Tag(name = "Client"),
                @Tag(name = "Client - Coach"),
                @Tag(name = "Client - Nutrition Plan"),
                @Tag(name = "Client - Questionnaire")
        },
        security = @SecurityRequirement(name = "bearerAuth"))
public class OpenApiConfig {

}
