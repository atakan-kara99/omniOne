package app.omniOne.email;

import app.omniOne.email.properties.ActivationProps;
import app.omniOne.email.properties.InvitationProps;
import app.omniOne.email.properties.ResetPasswordProps;
import app.omniOne.exception.custom.EmailDeliveryException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${email.from}")
    private String from;
    @Value("${frontend.base.url}")
    private String baseUrl;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ActivationProps activationProps;
    private final InvitationProps invitationProps;
    private final ResetPasswordProps resetPasswordProps;

    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendActivationMail(String to, String jwt) {
        sendTemplateMail(to, jwt,
                activationProps.urlPath(), activationProps.filePath(), activationProps.subject(), activationProps.ttlMins());
        log.info("Sent activation email (template={})", activationProps.filePath());
    }

    public void sendResetPasswordMail(String to, String jwt) {
        sendTemplateMail(to, jwt,
                resetPasswordProps.urlPath(), resetPasswordProps.filePath(), resetPasswordProps.subject(),
                resetPasswordProps.ttlMins());
        log.info("Sent reset-password email (template={})", resetPasswordProps.filePath());
    }

    public void sendInvitationMail(String to, String jwt) {
        sendTemplateMail(to, jwt,
                invitationProps.urlPath(), invitationProps.filePath(), invitationProps.subject(), invitationProps.ttlMins());
        log.info("Sent invitation email (template={})", invitationProps.filePath());
    }

    private void sendTemplateMail(String to, String jwt, String urlPath, String filePath, String subject, int ttlMins) {
        urlPath += "?token=" + jwt;
        String text = render(filePath, Map.of(
                "baseUrl", baseUrl,
                "urlPath", urlPath,
                "appName", applicationName,
                "ttlText", formatTtl(ttlMins)));
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new EmailDeliveryException("Failed to send email", ex);
        }
    }

    private String formatTtl(int ttlMins) {
        if (ttlMins > 48 * 60) {
            int days = (int) Math.ceil(ttlMins / (60.0 * 24.0));
            return days + " days";
        }
        if (ttlMins > 60) {
            int hours = (int) Math.ceil(ttlMins / 60.0);
            return hours + " hours";
        }
        return ttlMins == 1 ? "1 minute" : ttlMins + " minutes";
    }

    private String render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

}
