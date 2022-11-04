#!/bin/sh

`source scripts/env.sh`

export SYSLOG_VERSION=$(cat VERSION)
ant package
