#!/bin/bash

# 设置 Java 17 环境
export JAVA_HOME=/Users/thisfeng/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "Java 17 环境已设置:"
echo "JAVA_HOME: $JAVA_HOME"
java -version

# 如果使用 jenv，也设置 jenv
if command -v jenv &> /dev/null; then
    jenv global 17 2>/dev/null || echo "jenv not configured for Java 17"
fi