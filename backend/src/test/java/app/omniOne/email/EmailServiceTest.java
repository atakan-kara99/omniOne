package app.omniOne.email;

import app.omniOne.email.properties.ActivationProps;
import app.omniOne.email.properties.InvitationProps;
import app.omniOne.email.properties.ResetPasswordProps;
import app.omniOne.exception.custom.EmailDeliveryException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static app.omniOne.TestFixtures.userEmail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;

    private MimeMessage mimeMessage;

    private EmailService emailService;

    @BeforeEach void setUp() {
        ActivationProps activationProps = new ActivationProps(
                "/activate", "activation-template", "Activate", 60);
        InvitationProps invitationProps = new InvitationProps(
                "/invite", "invitation-template", "Invite", 120);
        ResetPasswordProps resetPasswordProps = new ResetPasswordProps(
                "/reset", "reset-template", "Reset", 15);
        emailService = new EmailService(
                mailSender, templateEngine, activationProps, invitationProps, resetPasswordProps);
        ReflectionTestUtils.setField(emailService, "applicationName", "omniOne");
        ReflectionTestUtils.setField(emailService, "from", "noreply@omni.one");
        ReflectionTestUtils.setField(emailService, "baseUrl", "https://app.omni.one");
        mimeMessage = new MimeMessage((Session) null);
    }

    @Test void sendSimpleMail_buildsAndSendsSimpleMessage() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSimpleMail(userEmail, "Subject", "Body");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertEquals("noreply@omni.one", sent.getFrom());
        assertEquals("Subject", sent.getSubject());
        assertEquals("Body", sent.getText());
        Assertions.assertNotNull(sent.getTo());
        assertEquals(1, sent.getTo().length);
        assertEquals(userEmail, sent.getTo()[0]);
    }

    @Test
    void sendSimpleMail_wrapsFailures() {
        doThrow(new RuntimeException("boom")).when(mailSender).send(any(SimpleMailMessage.class));

        EmailDeliveryException exception = assertThrows(
                EmailDeliveryException.class,
                () -> emailService.sendSimpleMail(userEmail, "Subject", "Body"));

        assertEquals("Failed to send email", exception.getMessage());
        assertEquals("boom", exception.getCause().getMessage());
    }

    @Test void sendActivationMail_rendersTemplateAndSetsMailFields() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html>body</html>");

        emailService.sendActivationMail(userEmail, "jwt-token");

        verify(mailSender).send(mimeMessage);
        assertEquals("Activate", mimeMessage.getSubject());
        assertEquals(userEmail, mimeMessage.getAllRecipients()[0].toString());
        assertEquals("noreply@omni.one", mimeMessage.getFrom()[0].toString());
        Context context = captureContext("activation-template");
        assertEquals("https://app.omni.one", context.getVariable("baseUrl"));
        assertEquals("/activate?token=jwt-token", context.getVariable("urlPath"));
        assertEquals("omniOne", context.getVariable("appName"));
        assertEquals("60 minutes", context.getVariable("ttlText"));
    }

    @Test void sendInvitationMail_usesInvitationProps() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html>body</html>");

        emailService.sendInvitationMail("invitee@omni.one", "invite-token");

        assertEquals("Invite", mimeMessage.getSubject());
        assertEquals("invitee@omni.one", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("noreply@omni.one", mimeMessage.getFrom()[0].toString());
        Context context = captureContext("invitation-template");
        assertEquals("https://app.omni.one", context.getVariable("baseUrl"));
        assertEquals("/invite?token=invite-token", context.getVariable("urlPath"));
        assertEquals("omniOne", context.getVariable("appName"));
        assertEquals("2 hours", context.getVariable("ttlText"));
    }

    @Test
    void sendActivationMail_encodesTokenInUrlPath() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html>body</html>");

        emailService.sendActivationMail(userEmail, "a+b/c==");

        Context context = captureContext("activation-template");
        assertEquals("/activate?token=a%2Bb%2Fc%3D%3D", context.getVariable("urlPath"));
    }

    @Test
    void sendResetPasswordMail_wrapsFailures() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html>body</html>");
        doThrow(new RuntimeException("boom")).when(mailSender).send(mimeMessage);

        EmailDeliveryException exception = assertThrows(
                EmailDeliveryException.class,
                () -> emailService.sendResetPasswordMail(userEmail, "reset-token"));

        assertEquals("Failed to send email", exception.getMessage());
        assertEquals("boom", exception.getCause().getMessage());
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendResetPasswordMail_wrapsTemplateFailures() {
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenThrow(new RuntimeException("template-boom"));

        EmailDeliveryException exception = assertThrows(
                EmailDeliveryException.class,
                () -> emailService.sendResetPasswordMail(userEmail, "reset-token"));

        assertEquals("Failed to send email", exception.getMessage());
        assertEquals("template-boom", exception.getCause().getMessage());
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    void sendResetPasswordMail_wrapsMimeMessageCreationFailures() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("mime-boom"));
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html>body</html>");

        EmailDeliveryException exception = assertThrows(
                EmailDeliveryException.class,
                () -> emailService.sendResetPasswordMail(userEmail, "reset-token"));

        assertEquals("Failed to send email", exception.getMessage());
        assertEquals("mime-boom", exception.getCause().getMessage());
    }

    private Context captureContext(String expectedTemplate) {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(expectedTemplate), contextCaptor.capture());
        return contextCaptor.getValue();
    }
}
