package com.tw.go.plugin;

import org.junit.Test;

public class SMTPMailSenderTest {
    @Test
    public void shouldSendEmail() {
        String hostName = "smtp.gmail.com";
        int port = 587;
        String emailId = "your email-id";
        String username = emailId;
        String password = "your password";
        boolean tls = true;
        String fromEmailId = emailId;
        String toEmailId = emailId;
        new SMTPMailSender(hostName, port, username, password, tls, fromEmailId).send("subject", "body", toEmailId);
    }
}