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

    private String host;
    private int port;
    private String username;
    private String password;
    private Boolean tls;
    private String fromEmailId;

    public SMTPMailSender(String hostName, int port, String username, String password, boolean tls, String fromEmailId) {
        this.host = hostName;
        this.port = port;
        this.username = username;
        this.password = password;
        this.tls = tls;
        this.fromEmailId = fromEmailId;
    }

    public void send(String subject, String body, String toEmailId) {
        Transport transport = null;
        try {
            Properties properties = mailProperties();
            Session session = createSession(properties, username, password);
            transport = session.getTransport();
            transport.connect(host, nullIfEmpty(username), nullIfEmpty(password));
            MimeMessage message = createMessage(session, fromEmailId, toEmailId, subject, body);
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
        properties.put("mail.from", fromEmailId);

        if (!System.getProperties().containsKey("mail.smtp.connectiontimeout")) {
            properties.put("mail.smtp.connectiontimeout", DEFAULT_TIMEOUT);
        }

        if (!System.getProperties().containsKey("mail.smtp.timeout")) {
            properties.put("mail.smtp.timeout", DEFAULT_TIMEOUT);
        }

        if (tls) {
            properties.put("mail.smtp.starttls.enable", "true");
            properties.setProperty("mail.smtp.ssl.enable", "true");
        }

        String mailProtocol = tls ? "smtps" : "smtp";
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

        if (port != that.port) return false;
        if (fromEmailId != null ? !fromEmailId.equals(that.fromEmailId) : that.fromEmailId != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (tls != null ? !tls.equals(that.tls) : that.tls != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (tls != null ? tls.hashCode() : 0);
        result = 31 * result + (fromEmailId != null ? fromEmailId.hashCode() : 0);
        return result;
    }
}
