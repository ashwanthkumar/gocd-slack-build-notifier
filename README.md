# gocd-slack-build-notifier
Slack based GoCD build notifier

## Configuration
Create a file `go_notify.conf` in the server's home directory. Minimalistic configuration is
```hocon
gocd.slack {
  server-host = "http://localhost:8153/"
  webhookUrl = "https://hooks.slack.com/services/...."
}
```

## License

http://www.apache.org/licenses/LICENSE-2.0
