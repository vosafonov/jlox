#!/usr/bin/env bash

echo "--------------------------"
echo "building..."
gradle jar
echo "done."

echo "--------------------------"
echo "running: ./jlox"
