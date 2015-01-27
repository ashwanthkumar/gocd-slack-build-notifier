package com.tw.go.plugin;

import org.junit.Test;

public class SMTPMailSenderTest {
    @Test
    public void shouldSendEmail() {
        String emailId = "";
        String password = "";
        SMTPSettings settings = new SMTPSettings("smtp.gmail.com", 587, true, emailId, password);
        new SMTPMailSender(settings).send("subject", "body", emailId);
    }
}