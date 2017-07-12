#!/bin/sh
if [ -z "${RCT_NO_LAUNCH_PACKAGER+xxx}" ] ; then
  if nc -w 5 -z localhost 8081 ; then
    if ! curl -s "http://localhost:8081/status" | grep -q "packager-status:running" ; then
      echo "Port 8081 already in use, packager is either not running or not running correctly"
      exit 2
    fi
  else
    open "$SRCROOT/../scripts/launchPackager.command" || echo "Can't start packager automatically"
  fi
fi
