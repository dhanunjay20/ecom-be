package com.tcon.ecom.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${app.email.verification-url}")
    private String verificationUrl;

    @Value("${app.email.reset-password-url}")
    private String resetPasswordUrl;

    public void sendVerificationEmail(String to, String token, String firstName) {
        String subject = "Verify Your Email - E-Commerce Platform";
        String verifyLink = verificationUrl + "?token=" + token;

        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Welcome to E-Commerce Platform, %s!</h2>
                    <p>Thank you for registering. Please verify your email address by clicking the link below:</p>
                    <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 14px 20px; text-decoration: none; border-radius: 4px;">Verify Email</a></p>
                    <p>Or copy and paste this link in your browser:</p>
                    <p>%s</p>
                    <p>This link will expire in 24 hours.</p>
                    <p>If you didn't create an account, please ignore this email.</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Team</p>
                </body>
                </html>
                """, firstName, verifyLink, verifyLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String to, String token, String firstName) {
        String subject = "Reset Your Password - E-Commerce Platform";
        String resetLink = resetPasswordUrl + "?token=" + token;

        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>Hi %s,</p>
                    <p>We received a request to reset your password. Click the button below to reset it:</p>
                    <p><a href="%s" style="background-color: #f44336; color: white; padding: 14px 20px; text-decoration: none; border-radius: 4px;">Reset Password</a></p>
                    <p>Or copy and paste this link in your browser:</p>
                    <p>%s</p>
                    <p>This link will expire in 1 hour.</p>
                    <p>If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Team</p>
                </body>
                </html>
                """, firstName, resetLink, resetLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Welcome to E-Commerce Platform!";

        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Welcome aboard, %s!</h2>
                    <p>Your email has been successfully verified.</p>
                    <p>You can now enjoy shopping for luxury clothing and jewelry on our platform.</p>
                    <p>Start exploring our collections now!</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Team</p>
                </body>
                </html>
                """, firstName);

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendPasswordChangedEmail(String to, String firstName) {
        String subject = "Password Changed Successfully";

        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Password Changed</h2>
                    <p>Hi %s,</p>
                    <p>This is a confirmation that your password has been changed successfully.</p>
                    <p>If you didn't make this change, please contact our support team immediately.</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Team</p>
                </body>
                </html>
                """, firstName);

        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Use authenticated SMTP username as MAIL FROM (some providers reject different MAIL FROM)
            String effectiveFrom = (smtpUsername != null && !smtpUsername.isBlank()) ? smtpUsername : fromEmail;

            helper.setFrom(effectiveFrom);

            // If configured 'app.email.from' differs from authenticated account, set it as Reply-To
            if (fromEmail != null && !fromEmail.isBlank() && !fromEmail.equalsIgnoreCase(effectiveFrom)) {
                helper.setReplyTo(fromEmail);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email sent successfully to: {} (from: {})", to, effectiveFrom);
        } catch (MessagingException e) {
            logger.error("Failed to compose email to: {}", to, e);
            throw new MailSendException("Failed to compose or send email: " + e.getMessage());
        } catch (Exception ex) {
            logger.error("Failed to send email to: {}", to, ex);
            throw new MailSendException("Failed to send email: " + ex.getMessage());
        }
    }
}
