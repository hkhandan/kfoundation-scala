#!/usr/bin/env sh
sbt --supershell=false compile package "+publishLocal" "+publishM2"