package app.omniOne.email;

import app.omniOne.exception.SendEmailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ActivationProperties activationProps;
    private final InvitationProperties invitationProps;

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
                activationProps.from(), activationProps.url(), activationProps.text(), activationProps.subject());
    }

    public void sendInvitationMail(String to, String jwt) {
        sendTemplateMail(to, jwt,
                invitationProps.from(), invitationProps.url(), invitationProps.text(), invitationProps.subject());
    }

    private void sendTemplateMail(String to, String jwt, String from, String url, String textTemp, String subject) {
        String link = url + "?token=" + jwt;
        String text = textTemp.formatted(link);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new SendEmailException(ex.getMessage());
        }
    }

}
