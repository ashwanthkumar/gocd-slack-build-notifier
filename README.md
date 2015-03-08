Go CD plugin to send Email notifications.

*Usage:*

* Clone repository
* Update [code](https://github.com/srinivasupadhya/email-notifier/blob/master/src/com/tw/go/plugin/EmailNotificationPluginImpl.java#L67) with 'hostname', 'port', 'from email-id', 'password' & 'to email-id'
* You can make sure everything works by providing these details in [test](https://github.com/srinivasupadhya/email-notifier/blob/master/test/com/tw/go/plugin/SMTPMailSenderTest.java) and running it
* Customize when & whom to send emails for different events (Stage - Passed, Failed, Cancelled)
* Run `mvn clean package -DskipTests` which will create plugin jar in 'dist' folder
* Place the jar in `<go-server-location>/plugins/external` & restart Go Server
