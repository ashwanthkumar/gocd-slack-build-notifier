/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.tw.go.plugin;

import com.thoughtworks.go.plugin.api.logging.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static javax.mail.Message.RecipientType.TO;

public class SMTPMailSender {
    private static Logger LOGGER = Logger.getLoggerFor(EmailNotificationPluginImpl.class);

    public static final int DEFAULT_TIMEOUT = 60 * 1000;

    private SMTPSettings smtpSettings;

    public SMTPMailSender(SMTPSettings smtpSettings) {
        this.smtpSettings = smtpSettings;
    }

    public void send(String subject, String body, String toEmailId) {
        Transport transport = null;
        try {
            Properties properties = mailProperties();
            Session session = createSession(properties, smtpSettings.getFromEmailId(), smtpSettings.getPassword());
            transport = session.getTransport();
            transport.connect(smtpSettings.getHostName(), nullIfEmpty(smtpSettings.getFromEmailId()), nullIfEmpty(smtpSettings.getPassword()));
            MimeMessage message = createMessage(session, smtpSettings.getFromEmailId(), toEmailId, subject, body);
            transport.sendMessage(message, message.getRecipients(TO));
        } catch (Exception e) {
            LOGGER.error(String.format("Sending failed for email [%s] to [%s]", subject, toEmailId), e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    LOGGER.error("Failed to close transport", e);
                }
            }
        }
    }

    private Properties mailProperties() {
        Properties properties = new Properties();
        properties.put("mail.from", smtpSettings.getFromEmailId());

        if (!System.getProperties().containsKey("mail.smtp.connectiontimeout")) {
            properties.put("mail.smtp.connectiontimeout", DEFAULT_TIMEOUT);
        }

        if (!System.getProperties().containsKey("mail.smtp.timeout")) {
            properties.put("mail.smtp.timeout", DEFAULT_TIMEOUT);
        }

        if (smtpSettings.isTls()) {
            properties.put("mail.smtp.starttls.enable", "true");
            properties.setProperty("mail.smtp.ssl.enable", "true");
        }

        String mailProtocol = smtpSettings.isTls() ? "smtps" : "smtp";
        properties.put("mail.transport.protocol", mailProtocol);

        return properties;
    }

    private Session createSession(Properties properties, String username, String password) {
        if (isEmpty(username) || isEmpty(password)) {
            return Session.getInstance(properties);
        } else {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtps.auth", "true");
            return Session.getInstance(properties, new SMTPAuthenticator(username, password));
        }
    }

    private final class SMTPAuthenticator extends Authenticator {
        private final String username;
        private final String password;

        public SMTPAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

    private MimeMessage createMessage(Session session, String fromEmailId, String toEmailId, String subject, String body) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmailId));
        message.setRecipients(TO, toEmailId);
        message.setSubject(subject);
        message.setContent(message, "text/plain");
        message.setSentDate(new Date());
        message.setText(body);
        message.setSender(new InternetAddress(fromEmailId));
        message.setReplyTo(new InternetAddress[]{new InternetAddress(fromEmailId)});
        return message;
    }

    private String nullIfEmpty(String str) {
        return isEmpty(str) ? null : str;
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SMTPMailSender that = (SMTPMailSender) o;

        if (smtpSettings != null ? !smtpSettings.equals(that.smtpSettings) : that.smtpSettings != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return smtpSettings != null ? smtpSettings.hashCode() : 0;
    }
}
