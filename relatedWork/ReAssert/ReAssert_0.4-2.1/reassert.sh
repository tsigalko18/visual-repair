#!/bin/bash  

# Convenience script used to run ReAssert from the command line
# Run with no arguments for usage

reassertRoot=${0%/*}

classes=$reassertRoot/bin\ `ls $reassertRoot/*.jar`\ `ls $reassertRoot/lib/*.jar`
for cp in $classes; do
    CLASSPATH=$CLASSPATH$cp:
done
export CLASSPATH

java edu.illinois.reassert.ReAssert $@