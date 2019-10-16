[![Build Status](https://travis-ci.org/ashwanthkumar/gocd-slack-build-notifier.svg?branch=master)](https://travis-ci.org/ashwanthkumar/gocd-slack-build-notifier)
# gocd-slack-build-notifier
Slack based GoCD build notifier

## Setup
Download jar from [releases](https://github.com/ashwanthkumar/gocd-slack-build-notifier/releases) & place it in /plugins/external & restart Go Server.

## Configuration
All configurations are in [HOCON](https://github.com/typesafehub/config) format. Plugin searches for the configuration file in the following order

1. File defined by the environment variable `GO_NOTIFY_CONF`.
2. `go_notify.conf` at the user's home directory. Typically it's the `go` user's home directory (`/var/go`).
3. `go_notify.conf` present at the `CRUISE_SERVER_DIR` environment variable location.

You can find the details on where / how to setup environment variables for GoCD at the [documentation](https://docs.gocd.org/current/installation/install/server/linux.html#location-of-gocd-server-files).

Minimalistic configuration would be something like
```hocon
gocd.slack {
  login = "someuser"
  password = "somepassword"
  api-token = "a-valid-token-from-gocd-server"
  server-host = "http://localhost:8153/"
  api-server-host = "http://localhost:8153/"
  webhookUrl = "https://hooks.slack.com/services/...."

  # optional fields
  channel = "#build"
  slackDisplayName = "gocd-slack-bot"
  slackUserIconURL = "http://example.com/slack-bot.png"
  display-console-log-links = true
  displayMaterialChanges = true
  process-all-rules = true
  proxy {
    hostname = "localhost"
    port = "5555"
    type = "socks" # acceptable values are http / socks
  }
}
```
- `login` - Login for a Go user who is authorized to access the REST API.
- `password` - Password for the user specified above. You might want to create a less privileged user for this plugin.
- `api-token` - Valid GoCD access token. Available starting from v19.2.0 (https://api.gocd.org/current/#bearer-token-authentication). If both login/password and api-token are present, api-token takes precedence.
- `server-host` - FQDN of the Go Server. All links on the slack channel will be relative to this host.
- `api-server-host` - This is an optional attribute. Set this field to localhost so server will use this endpoint to get `PipelineHistory` and `PipelineInstance`  
- `webhookUrl` - Slack Webhook URL
- `channel` - Override the default channel where we should send the notifications in slack. You can also give a value starting with `@` to send it to any specific user.
- `display-console-log-links` - Display console log links in the notification. Defaults to true, set to false if you want to hide.
- `displayMaterialChanges` - Display material changes in the notification (git revisions for example). Defaults to true, set to false if you want to hide.
- `process-all-rules` - If true, all matching rules are applied instead of just the first.
- `truncate-changes` - If true, displays only the latest 5 changes for all the materials. (Default: true)
- `proxy` - Specify proxy related settings for the plugin.
  - `proxy.hostname` - Proxy Host
  - `proxy.port` - Proxy Port
  - `proxy.type` - `socks` or `http` are the only accepted values.

## Pipeline Rules
By default the plugin pushes a note about all failed stages across all pipelines to Slack. You have fine grain control over this operation.
```hocon
gocd.slack {
  server-host = "http://localhost:8153/"
  webhookUrl = "https://hooks.slack.com/services/...."

  pipelines = [{
    name = "gocd-slack-build"
    stage = "build"
    group = ".*"
    state = "failed|passed"
    channel = "#oss-build-group"
    owners = ["ashwanthkumar"]
    webhookUrl = "https://hooks.slack.com/services/another-team-hook-id..."
  },
  {
    name = ".*"
    stage = ".*"
    state = "failed"
  }]
}
```
`gocd.slack.pipelines` contains all the rules for the go-server. It is a list of rules (see below for what the parameters mean) for various pipelines. The plugin will pick the first matching pipeline rule from the pipelines collection above, so your most specific rule should be first, with the most generic rule at the bottom. Alternatively, set the `process-all-rules` option to `true` and all matching rules will be applied.
- `name` - Regex to match the pipeline name
- `stage` - Regex to match the stage name
- `group` - Regex to match the pipeline group name
- `state` - State of the pipeline at which we should send a notification. You can provide multiple values separated by pipe (`|`) symbol. Valid values are passed, failed, cancelled, building, fixed, broken or all.
- `channel` - (Optional) channel where we should send the slack notification. This setting for a rule overrides the global setting
- `owners` - (Optional) list of slack user handles who must be tagged in the message upon notifications
- `webhookUrl` - (Optional) Use this webhook url instead of the global one. Useful if you're using multiple slack teams.

## Configuring the plugin for GoCD on Kubernetes using Helm

### Creating a Kubernetes secret to store the config file

- Create a file that has the config values, for example `go_notify.conf`
- Then create a Kubernetes secret using this file in the proper namespace 

```
kubectl create secret generic slack-config \
--from-file=go_notify.conf=go_notify.conf \
--namespace=gocd
```


### Adding the plugin
- In order to add this plugin, you have to use a local values.yaml file that will override the default [values.yaml](https://github.com/helm/charts/blob/master/stable/gocd/values.yaml) present in the official GoCD helm chart repo.
- Add the .jar file link from the releases section to the `env.extraEnvVars` section as a new environment variable.
- The environment variable name must have `GOCD_PLUGIN_INSTALL` prefixed to it.
- Example

```
env:
  extraEnvVars:
    - name: GOCD_PLUGIN_INSTALL_slack-notification-plugin
      value: https://github.com/ashwanthkumar/gocd-slack-build-notifier/releases/download/v1.3.1/gocd-slack-notifier-1.3.1.jar
    - name: GO_NOTIFY_CONF
      value: /tmp/slack/go_notify.conf
```
- Make sure to add the link of the release you want to use.
- If you want to specify a custom path for the `go_notify.conf` file you can use the `GO_NOTIFY_CONF` environment variable as given above.


### Mounting the config file

- Mount the previously secret to a path by adding the following configuration to the local values.yaml

```
persistence:
  extraVolumes:
    - name: slack-config
      secret:
        secretName: slack-config
        defaultMode: 0744

  extraVolumeMounts:
    - name: slack-config
      mountPath: /tmp/slack
      readOnly: true
```
- If you want to use a custom config location by specifying `GO_NOTIFY_CONF`, then you can use the above `mountPath`. If not, change the `mountPath` to `/var/go` as it is the default `go` user's home directory.
- Then applying the local values.yaml that has these values added to it will result in a new Go Server pod being created that has the plugin installed and running.


## Screenshots
<img src="https://raw.githubusercontent.com/ashwanthkumar/gocd-slack-build-notifier/master/images/gocd-slack-notifier-demo-with-changes.png" width="400"/>
<img src="https://raw.githubusercontent.com/ashwanthkumar/gocd-slack-build-notifier/master/images/gocd-slack-notifier-demo.png" width="400"/>

## License
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
