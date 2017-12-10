#!/usr/bin/env bash

echo "--------------------------"
echo "building..."
gradle jar generateAstJar
echo "done."

echo "--------------------------"
echo "running: ./jlox"
