package com.safarnama.backend.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        System.out.println("EmailService: preparing to send to " + toEmail);
        System.out.println("EmailService: from address = " + fromEmail);
        System.out.println("EmailService: api key present = " + (apiKey != null && !apiKey.isEmpty()));

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        String subject = "Reset your Safarnama password";
        String body = "Use this code to reset your password: " + resetToken +
                "\n\nThis code expires in 1 hour. If you didn't request this, ignore this email.";
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("EmailService: SendGrid status = " + response.getStatusCode());
            System.out.println("EmailService: SendGrid body = " + response.getBody());

            if (response.getStatusCode() >= 300) {
                throw new RuntimeException("SendGrid rejected the email: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception ex) {
            System.out.println("EmailService: EXCEPTION occurred: " + ex.getMessage());
            throw new RuntimeException("Failed to send email", ex);
        }
    }
}