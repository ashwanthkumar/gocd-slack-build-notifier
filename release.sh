#!/bin/bash -xe
rm -rf dist/
mkdir dist

mvn clean package -Pdist
cp target/gocd-slack-notifier*.jar dist/
