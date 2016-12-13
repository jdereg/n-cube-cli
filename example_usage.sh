#!/bin/bash

javaPath="c:/Program Files/Java/jdk1.8.0_66/jre/bin"

USERNAME=$1
PASSWORD=$2

cookie=`"${javaPath}"/java -jar target/n-cube-cli-1.0.0-SNAPSHOT.jar nce-auth --u ${USERNAME} --p ${PASSWORD}`

echo "${cookie}"

version=`"${javaPath}"/java -jar target/n-cube-1.0.0-SNAPSHOT.jar nce-getappversion --cookie ${cookie} --app Devops.Test`

echo "${version}"
