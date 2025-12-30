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
        String subject = "✨ Verify Your Email - Welcome to Our Store!";
        String verifyLink = verificationUrl + "?token=" + token;

        String htmlContent = String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verify Your Email</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f7fa;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 40px 0;">
                        <tr>
                            <td align="center">
                                <!-- Main Container -->
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); overflow: hidden;">
                                    
                                    <!-- Header with gradient -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 40px 30px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                                🎉 Welcome to Our Store!
                                            </h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px 40px 30px;">
                                            <h2 style="margin: 0 0 16px; color: #1a202c; font-size: 24px; font-weight: 600;">
                                                Hi %s! 👋
                                            </h2>
                                            <p style="margin: 0 0 24px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Thank you for joining our community! We're excited to have you on board.
                                            </p>
                                            <p style="margin: 0 0 32px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                To get started, please verify your email address by clicking the button below:
                                            </p>
                                            
                                            <!-- CTA Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0">
                                                <tr>
                                                    <td align="center" style="padding: 0 0 32px;">
                                                        <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; text-decoration: none; padding: 16px 48px; border-radius: 8px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4); transition: all 0.3s ease;">
                                                            Verify Email Address
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Alternative Link -->
                                            <div style="background-color: #f7fafc; border-radius: 8px; padding: 20px; margin-bottom: 24px;">
                                                <p style="margin: 0 0 8px; color: #4a5568; font-size: 14px; font-weight: 600;">
                                                    Or copy and paste this link:
                                                </p>
                                                <p style="margin: 0; color: #667eea; font-size: 13px; word-break: break-all; font-family: monospace;">
                                                    %s
                                                </p>
                                            </div>
                                            
                                            <!-- Info Box -->
                                            <div style="border-left: 4px solid #fbbf24; background-color: #fffbeb; padding: 16px; border-radius: 4px; margin-bottom: 24px;">
                                                <p style="margin: 0; color: #92400e; font-size: 14px; line-height: 1.5;">
                                                    ⏰ <strong>Important:</strong> This verification link will expire in 24 hours.
                                                </p>
                                            </div>
                                            
                                            <p style="margin: 0; color: #718096; font-size: 14px; line-height: 1.6;">
                                                If you didn't create an account with us, you can safely ignore this email.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f7fafc; padding: 32px 40px; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 8px; color: #4a5568; font-size: 16px; font-weight: 600;">
                                                Best regards,
                                            </p>
                                            <p style="margin: 0 0 24px; color: #667eea; font-size: 16px; font-weight: 600;">
                                                The E-Commerce Team
                                            </p>
                                            <p style="margin: 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                You're receiving this email because you created an account on our platform.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                                
                                <!-- Footer Text -->
                                <p style="margin: 24px 0 0; color: #a0aec0; font-size: 12px; text-align: center;">
                                    © 2025 E-Commerce Platform. All rights reserved.
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, firstName, verifyLink, verifyLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String to, String token, String firstName) {
        String subject = "🔐 Reset Your Password - E-Commerce Platform";
        String resetLink = resetPasswordUrl + "?token=" + token;

        String htmlContent = String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Reset Your Password</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f7fa;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 40px 0;">
                        <tr>
                            <td align="center">
                                <!-- Main Container -->
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); overflow: hidden;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); padding: 40px 40px 30px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                                🔐 Password Reset Request
                                            </h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px 40px 30px;">
                                            <h2 style="margin: 0 0 16px; color: #1a202c; font-size: 24px; font-weight: 600;">
                                                Hi %s,
                                            </h2>
                                            <p style="margin: 0 0 24px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                We received a request to reset your password. Don't worry, we're here to help! 💪
                                            </p>
                                            <p style="margin: 0 0 32px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Click the button below to create a new password:
                                            </p>
                                            
                                            <!-- CTA Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0">
                                                <tr>
                                                    <td align="center" style="padding: 0 0 32px;">
                                                        <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: #ffffff; text-decoration: none; padding: 16px 48px; border-radius: 8px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 14px rgba(245, 87, 108, 0.4);">
                                                            Reset Password
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Alternative Link -->
                                            <div style="background-color: #f7fafc; border-radius: 8px; padding: 20px; margin-bottom: 24px;">
                                                <p style="margin: 0 0 8px; color: #4a5568; font-size: 14px; font-weight: 600;">
                                                    Or copy and paste this link:
                                                </p>
                                                <p style="margin: 0; color: #f5576c; font-size: 13px; word-break: break-all; font-family: monospace;">
                                                    %s
                                                </p>
                                            </div>
                                            
                                            <!-- Warning Box -->
                                            <div style="border-left: 4px solid #ef4444; background-color: #fef2f2; padding: 16px; border-radius: 4px; margin-bottom: 24px;">
                                                <p style="margin: 0 0 8px; color: #991b1b; font-size: 14px; line-height: 1.5;">
                                                    ⏰ <strong>Time Sensitive:</strong> This link will expire in 1 hour for security reasons.
                                                </p>
                                                <p style="margin: 0; color: #991b1b; font-size: 14px; line-height: 1.5;">
                                                    🔒 <strong>Didn't request this?</strong> Your account is safe. Simply ignore this email.
                                                </p>
                                            </div>
                                            
                                            <!-- Security Tip -->
                                            <div style="background-color: #ecfdf5; border-radius: 8px; padding: 16px; border: 1px solid #6ee7b7;">
                                                <p style="margin: 0 0 8px; color: #065f46; font-size: 14px; font-weight: 600;">
                                                    💡 Security Tip
                                                </p>
                                                <p style="margin: 0; color: #047857; font-size: 13px; line-height: 1.5;">
                                                    Never share your password with anyone. Our team will never ask for your password via email or phone.
                                                </p>
                                            </div>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f7fafc; padding: 32px 40px; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 8px; color: #4a5568; font-size: 16px; font-weight: 600;">
                                                Best regards,
                                            </p>
                                            <p style="margin: 0 0 24px; color: #f5576c; font-size: 16px; font-weight: 600;">
                                                The E-Commerce Team
                                            </p>
                                            <p style="margin: 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                If you have any questions, please don't hesitate to contact our support team.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                                
                                <p style="margin: 24px 0 0; color: #a0aec0; font-size: 12px; text-align: center;">
                                    © 2025 E-Commerce Platform. All rights reserved.
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, firstName, resetLink, resetLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "🎊 Welcome! Your Email is Verified";

        String htmlContent = String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome to E-Commerce Platform</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f7fa;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 40px 0;">
                        <tr>
                            <td align="center">
                                <!-- Main Container -->
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); overflow: hidden;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #4facfe 0%%, #00f2fe 100%%); padding: 40px 40px 30px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">
                                                🎊 You're All Set!
                                            </h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 24px; color: #1a202c; font-size: 26px; font-weight: 600; text-align: center;">
                                                Welcome aboard, %s! 🚀
                                            </h2>
                                            
                                            <!-- Success Icon -->
                                            <div style="text-align: center; margin-bottom: 32px;">
                                                <div style="display: inline-block; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); width: 80px; height: 80px; border-radius: 50%%; line-height: 80px; font-size: 40px;">
                                                    ✓
                                                </div>
                                            </div>
                                            
                                            <p style="margin: 0 0 24px; color: #4a5568; font-size: 16px; line-height: 1.6; text-align: center;">
                                                Your email has been successfully verified! 🎉
                                            </p>
                                            
                                            <!-- Features Grid -->
                                            <div style="margin: 32px 0;">
                                                <table width="100%%" cellpadding="0" cellspacing="0">
                                                    <tr>
                                                        <td style="padding: 16px; width: 50%%; vertical-align: top;">
                                                            <div style="text-align: center;">
                                                                <div style="font-size: 32px; margin-bottom: 8px;">🛍️</div>
                                                                <p style="margin: 0; color: #1a202c; font-size: 14px; font-weight: 600;">Shop Premium Products</p>
                                                            </div>
                                                        </td>
                                                        <td style="padding: 16px; width: 50%%; vertical-align: top;">
                                                            <div style="text-align: center;">
                                                                <div style="font-size: 32px; margin-bottom: 8px;">💎</div>
                                                                <p style="margin: 0; color: #1a202c; font-size: 14px; font-weight: 600;">Exclusive Collections</p>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 16px; width: 50%%; vertical-align: top;">
                                                            <div style="text-align: center;">
                                                                <div style="font-size: 32px; margin-bottom: 8px;">🚚</div>
                                                                <p style="margin: 0; color: #1a202c; font-size: 14px; font-weight: 600;">Fast Delivery</p>
                                                            </div>
                                                        </td>
                                                        <td style="padding: 16px; width: 50%%; vertical-align: top;">
                                                            <div style="text-align: center;">
                                                                <div style="font-size: 32px; margin-bottom: 8px;">🎁</div>
                                                                <p style="margin: 0; color: #1a202c; font-size: 14px; font-weight: 600;">Special Offers</p>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </div>
                                            
                                            <!-- Promo Box -->
                                            <div style="background: linear-gradient(135deg, #fbbf24 0%%, #f59e0b 100%%); border-radius: 12px; padding: 24px; text-align: center; margin: 32px 0;">
                                                <p style="margin: 0 0 8px; color: #ffffff; font-size: 18px; font-weight: 700;">
                                                    🎉 Special Welcome Gift!
                                                </p>
                                                <p style="margin: 0; color: #ffffff; font-size: 14px;">
                                                    Get <strong>10%% OFF</strong> on your first purchase
                                                </p>
                                            </div>
                                            
                                            <p style="margin: 24px 0 0; color: #718096; font-size: 14px; line-height: 1.6; text-align: center;">
                                                Start exploring our amazing collections and find your perfect style! ✨
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f7fafc; padding: 32px 40px; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 8px; color: #4a5568; font-size: 16px; font-weight: 600;">
                                                Happy Shopping! 🛒
                                            </p>
                                            <p style="margin: 0 0 24px; color: #00f2fe; font-size: 16px; font-weight: 600;">
                                                The E-Commerce Team
                                            </p>
                                            <p style="margin: 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                Need help? Contact our support team anytime!
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                                
                                <p style="margin: 24px 0 0; color: #a0aec0; font-size: 12px; text-align: center;">
                                    © 2025 E-Commerce Platform. All rights reserved.
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, firstName);

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendPasswordChangedEmail(String to, String firstName) {
        String subject = "🔒 Your Password Has Been Changed";

        String htmlContent = String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Changed</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f7fa;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 40px 0;">
                        <tr>
                            <td align="center">
                                <!-- Main Container -->
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); overflow: hidden;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); padding: 40px 40px 30px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                                🔒 Password Changed
                                            </h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <!-- Success Icon -->
                                            <div style="text-align: center; margin-bottom: 32px;">
                                                <div style="display: inline-block; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); width: 80px; height: 80px; border-radius: 50%%; line-height: 80px; font-size: 40px;">
                                                    ✓
                                                </div>
                                            </div>
                                            
                                            <h2 style="margin: 0 0 16px; color: #1a202c; font-size: 24px; font-weight: 600; text-align: center;">
                                                Hi %s,
                                            </h2>
                                            <p style="margin: 0 0 24px; color: #4a5568; font-size: 16px; line-height: 1.6; text-align: center;">
                                                This is a confirmation that your password has been changed successfully.
                                            </p>
                                            
                                            <!-- Info Box -->
                                            <div style="background-color: #ecfdf5; border-radius: 8px; padding: 20px; margin-bottom: 24px; border: 1px solid #6ee7b7;">
                                                <p style="margin: 0 0 12px; color: #065f46; font-size: 15px; font-weight: 600;">
                                                    ✅ Your account is secure
                                                </p>
                                                <p style="margin: 0; color: #047857; font-size: 14px; line-height: 1.6;">
                                                    Your password was successfully updated. You can now use your new password to sign in to your account.
                                                </p>
                                            </div>
                                            
                                            <!-- Warning Box -->
                                            <div style="border-left: 4px solid #ef4444; background-color: #fef2f2; padding: 16px; border-radius: 4px; margin-bottom: 24px;">
                                                <p style="margin: 0 0 8px; color: #991b1b; font-size: 14px; font-weight: 600;">
                                                    ⚠️ Didn't make this change?
                                                </p>
                                                <p style="margin: 0; color: #991b1b; font-size: 14px; line-height: 1.5;">
                                                    If you didn't request a password change, please contact our support team immediately. Your account security is our top priority.
                                                </p>
                                            </div>
                                            
                                            <!-- Security Tips -->
                                            <div style="background-color: #f7fafc; border-radius: 8px; padding: 20px;">
                                                <p style="margin: 0 0 12px; color: #1a202c; font-size: 15px; font-weight: 600;">
                                                    🛡️ Security Tips:
                                                </p>
                                                <ul style="margin: 0; padding-left: 20px; color: #4a5568; font-size: 14px; line-height: 1.8;">
                                                    <li>Use a unique password for this account</li>
                                                    <li>Never share your password with anyone</li>
                                                    <li>Enable two-factor authentication for extra security</li>
                                                    <li>Review your account activity regularly</li>
                                                </ul>
                                            </div>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f7fafc; padding: 32px 40px; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 8px; color: #4a5568; font-size: 16px; font-weight: 600;">
                                                Stay secure,
                                            </p>
                                            <p style="margin: 0 0 24px; color: #10b981; font-size: 16px; font-weight: 600;">
                                                The E-Commerce Team
                                            </p>
                                            <p style="margin: 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                Questions? Contact our security team at security@ecommerce.com
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                                
                                <p style="margin: 24px 0 0; color: #a0aec0; font-size: 12px; text-align: center;">
                                    © 2025 E-Commerce Platform. All rights reserved.
                                </p>
                            </td>
                        </tr>
                    </table>
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