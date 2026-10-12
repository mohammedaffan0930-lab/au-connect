package edu.anurag.auconnect.service;

import edu.anurag.auconnect.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, @Value("${app.mail-from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    /**
     * Sends the verification email.
     *
     * @return true if an email was actually sent, false if SMTP is not configured or sending failed
     */
    public boolean sendVerification(User user, String link) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("SMTP not configured. Verification link for {}: {}", user.getEmail(), link);
            return false;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(user.getEmail());
            msg.setSubject("Verify your AU Connect account");
            msg.setText("Hi " + user.getFullName() + ",\n\n"
                    + "Welcome to AU Connect! Please confirm your Anurag University email by opening this link "
                    + "(valid for 24 hours):\n\n" + link + "\n\n"
                    + "If you did not create this account, you can ignore this email.\n\n- AU Connect");
            sender.send(msg);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
            log.warn("Verification link for {}: {}", user.getEmail(), link);
            return false;
        }
    }
}
