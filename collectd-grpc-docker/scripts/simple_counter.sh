#!/bin/bash

HOSTNAME="${COLLECTD_HOSTNAME:-localhost}"
INTERVAL="${COLLECTD_INTERVAL:-10}"

VALUE=1
while sleep "$INTERVAL"; do
  let "VALUE2=VALUE+1"
  echo "PUTVAL \"$HOSTNAME/56@exec-test/myType\" interval=$INTERVAL N:$VALUE:$VALUE2"
  let "VALUE=VALUE+1"
done