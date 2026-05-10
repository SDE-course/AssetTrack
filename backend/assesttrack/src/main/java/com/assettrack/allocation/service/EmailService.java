package com.assettrack.allocation.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notifications.email.from:noreply@assettrack.com}")
    private String fromAddress;

    @Value("${notifications.email.to}")
    private String toAddress;

    @Value("${notifications.email.enabled:true}")
    private boolean emailEnabled;

    // -----------------------------------------------------------------------
    // Public API — called by the scheduler
    // -----------------------------------------------------------------------

    /**
     * Sends a warranty-expiry alert email.
     *
     * @param assetName     display name of the asset
     * @param serialNumber  serial / asset tag
     * @param expiryDate    formatted expiry date string
     * @param daysRemaining days until expiry
     */
    @Async
    public void sendWarrantyExpiryAlert(String assetName, String serialNumber,
                                        String expiryDate, long daysRemaining) {
        String subject = String.format("⚠️ Warranty Expiry Alert — %s (%s)", assetName, serialNumber);
        String body = buildWarrantyEmailBody(assetName, serialNumber, expiryDate, daysRemaining);
        send(subject, body);
    }

    /**
     * Sends a low-stock alert email.
     *
     * @param assetType     the monitored asset type (e.g. "mouse")
     * @param currentCount  number of available items
     * @param threshold     the configured threshold
     */
    @Async
    public void sendLowStockAlert(String assetType, long currentCount, int threshold) {
        String subject = String.format("⚠️ Low Stock Alert — %s", capitalize(assetType));
        String body = buildLowStockEmailBody(assetType, currentCount, threshold);
        send(subject, body);
    }

    /**
     * Sends a plain test email to verify SMTP configuration.
     */
    @Async
    public void sendTestEmail() {
        String subject = "✅ AssetTrack — Email notification test";
        String body = "<h2>Email notifications are working!</h2>"
                + "<p>Your SMTP configuration is correct. "
                + "AssetTrack will now send warranty and low-stock alerts to this inbox.</p>";
        send(subject, body);
    }

    // -----------------------------------------------------------------------
    // Email body builders
    // -----------------------------------------------------------------------

    private String buildWarrantyEmailBody(String assetName, String serialNumber,
                                           String expiryDate, long daysRemaining) {
        return """
                <html><body style="font-family:Arial,sans-serif;color:#333;">
                  <h2 style="color:#e67e22;">⚠️ Warranty Expiry Alert</h2>
                  <p>The following asset's warranty is expiring soon:</p>
                  <table style="border-collapse:collapse;width:100%%;max-width:480px;">
                    <tr style="background:#f5f5f5;">
                      <td style="padding:8px 12px;border:1px solid #ddd;font-weight:bold;">Asset</td>
                      <td style="padding:8px 12px;border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px 12px;border:1px solid #ddd;font-weight:bold;">Serial Number</td>
                      <td style="padding:8px 12px;border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr style="background:#f5f5f5;">
                      <td style="padding:8px 12px;border:1px solid #ddd;font-weight:bold;">Expiry Date</td>
                      <td style="padding:8px 12px;border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px 12px;border:1px solid #ddd;font-weight:bold;">Days Remaining</td>
                      <td style="padding:8px 12px;border:1px solid #ddd;color:#e74c3c;font-weight:bold;">%d day%s</td>
                    </tr>
                  </table>
                  <p style="margin-top:16px;">Please take action before the warranty expires.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin-top:24px;"/>
                  <p style="font-size:12px;color:#999;">AssetTrack Notification System</p>
                </body></html>
                """.formatted(assetName, serialNumber, expiryDate,
                daysRemaining, daysRemaining == 1 ? "" : "s");
    }

    private String buildLowStockEmailBody(String assetType, long currentCount, int threshold) {
        return """
                <html><body style="font-family:Arial,sans-serif;color:#333;">
                  <h2 style="color:#e67e22;">⚠️ Low Stock Alert</h2>
                  <p>Stock has dropped to or below the configured threshold:</p>
                  <table style="border-collapse:collapse;width:100%%;max-width:480px;">
                    <tr style="background:#f5f5f5;">
                      <td style="padding:8px 12px;border:1px solid #ddd;font-weight:bold;">Asset Type</td>
                      <td style="padding:8px 12px;border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px 12px;border:1px solid #ddd;font-weight:bold;">Available Items</td>
                      <td style="padding:8px 12px;border:1px solid #ddd;color:#e74c3c;font-weight:bold;">%d</td>
                    </tr>
                    <tr style="background:#f5f5f5;">
                      <td style="padding:8px 12px;border:1px solid #ddd;font-weight:bold;">Threshold</td>
                      <td style="padding:8px 12px;border:1px solid #ddd;">%d</td>
                    </tr>
                  </table>
                  <p style="margin-top:16px;">Please restock %s items as soon as possible.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin-top:24px;"/>
                  <p style="font-size:12px;color:#999;">AssetTrack Notification System</p>
                </body></html>
                """.formatted(capitalize(assetType), currentCount, threshold, assetType);
    }

    // -----------------------------------------------------------------------
    // Core send — shared by all methods above
    // -----------------------------------------------------------------------

    private void send(String subject, String htmlBody) {
      if (!emailEnabled) {
        log.info("Email sending is disabled (notifications.email.enabled=false). Skipping subject: {}", subject);
        return;
      }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
            log.info("Email sent — to: {}, subject: {}", toAddress, subject);

      } catch (Exception e) {
        // Never fail business flow on SMTP/network issues.
        log.error("Failed to send email — subject: {}, error: {}", subject, e.getMessage(), e);
        }
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) return value;
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}