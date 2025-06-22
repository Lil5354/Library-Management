// src/main/java/com/uef.library.service/EmailService.java
package com.uef.library.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths; // Thêm import này

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Phương thức gửi email đơn giản (Plain Text)
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("trthanhdo41@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("Email sent successfully to " + toEmail + " (Simple Text)");
        } catch (MailException e) {
            System.err.println("Error sending simple email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Phương thức gửi email HTML
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true cho multipart

            helper.setFrom("trthanhdo41@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true chỉ ra nội dung là HTML

            mailSender.send(message);
            System.out.println("Email sent successfully to " + toEmail + " (HTML)");
        } catch (jakarta.mail.MessagingException e) {
            System.err.println("Error sending HTML email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        } catch (MailException e) {
            System.err.println("Error sending HTML email (Spring MailException) to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Phương thức gửi email với tệp đính kèm (có thể là Plain Text hoặc HTML)
    public void sendEmailWithAttachment(String toEmail, String subject, String body, boolean isHtml, String attachmentFilePath, String attachmentFileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true cho multipart

            helper.setFrom("trthanhdo41@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, isHtml); // set nội dung là Plain Text hay HTML

            // Xử lý tệp đính kèm
            if (StringUtils.hasText(attachmentFilePath)) {
                File attachment = new File(attachmentFilePath);
                if (attachment.exists() && attachment.isFile()) {
                    FileSystemResource file = new FileSystemResource(attachment);
                    helper.addAttachment(StringUtils.hasText(attachmentFileName) ? attachmentFileName : attachment.getName(), file);
                    System.out.println("Attached file: " + file.getFilename());
                } else {
                    System.err.println("Attachment file not found or is not a file: " + attachmentFilePath);
                }
            }

            mailSender.send(message);
            System.out.println("Email sent successfully to " + toEmail + " (With Attachment)");
        } catch (jakarta.mail.MessagingException e) {
            System.err.println("Error sending email with attachment to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        } catch (MailException e) {
            System.err.println("Error sending email with attachment (Spring MailException) to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Phương thức tổng quát để dễ dàng gọi từ controller
    public void sendEmail(String toEmail, String subject, String body, String format, String attachmentPath, String attachmentName) {
        boolean isHtml = "html".equalsIgnoreCase(format);
        if (StringUtils.hasText(attachmentPath)) {
            sendEmailWithAttachment(toEmail, subject, body, isHtml, attachmentPath, attachmentName);
        } else if (isHtml) {
            sendHtmlEmail(toEmail, subject, body);
        } else {
            sendSimpleEmail(toEmail, body, subject); // Truyền đúng tham số
        }
    }
}