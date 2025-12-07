package app.omniOne.email;

import app.omniOne.exception.SendEmailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.application.name}")
    private String applicationName;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ActivationProperties activationProps;
    private final InvitationProperties invitationProps;
    private final ResetPasswordProperties resetPasswordProps;

    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendActivationMail(String to, String jwt) {
        sendTemplateMail(to, jwt,
                activationProps.from(), activationProps.url(), activationProps.path(), activationProps.subject());
    }

    public void sendResetPasswordMail(String to, String jwt) {
        sendTemplateMail(to, jwt,
                resetPasswordProps.from(), resetPasswordProps.url(), resetPasswordProps.path(), resetPasswordProps.subject());
    }

    public void sendInvitationMail(String to, String jwt) {
        sendTemplateMail(to, jwt,
                invitationProps.from(), invitationProps.url(), invitationProps.path(), invitationProps.subject());
    }

    private void sendTemplateMail(String to, String jwt, String from, String url, String path, String subject) {
        String link = url + "?token=" + jwt;
        String text = render(path, Map.of("link", link, "appName", applicationName));
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new SendEmailException(ex.getMessage());
        }
    }

    private String render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

}
