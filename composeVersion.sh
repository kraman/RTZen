#!/bin/bash

CheckSVN=`svn --version | grep not`
RevDate="Unknown date"
RevNum=-1

if [ "x${CheckSVN}" = "x" ]; then
    echo "No subversion found";
else
    RevDate=`svn info | grep "Last Changed Date" | awk -F'[()]' '{ print $2 }'`
    RevNum=`svn info | grep "Last Changed Rev" | awk -F'[ :]+' '{ print $4 }'`
fi

DevJVM=$1
echo "Building RTZen version ${RevNum} , Snapshot from ${RevDate}";
echo "RTZen will be compiled for the ${DevJVM} platform";

echo "
/******************************************************************************\\
  | This file is auto generated by the build script. Do not modify manually.     |
  |                                                                              |
  | @author Krishna Raman                                                        |
  | @since 6 Jun 2004                                                            |
  |                                                                              |
  \\******************************************************************************/
package edu.uci.ece.zen.utils;

public class VersionStamp{
    public static final String versionDate=\"${RevDate}\";
    public static final int versionRev=${RevNum};
    public static final String devJVM=\"${DevJVM}\";
}" > packages/src/edu/uci/ece/zen/utils/VersionStamp.java
