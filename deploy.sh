#!/usr/bin/env sh
export GPG_TTY=$(tty)
sbt --no-colors --supershell=false "+publishLocal" "+publishM2" "+publishSigned" "fullOptJS"