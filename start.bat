@echo off

SET JAR_PACKAGE="./live-recording-0.0.1-SNAPSHOT.jar"

if exist %JAR_PACKAGE% (
 cmd /c "java -jar %JAR_PACKAGE%"
) 


