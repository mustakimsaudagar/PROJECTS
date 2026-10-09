@echo off
setlocal
echo Stopping Apache Tomcat...
set "JAVA_HOME=C:\Program Files\Java\jdk-23"
set "CATALINA_HOME=%~dp0tools\apache-tomcat-10.1.34"
call "%CATALINA_HOME%\bin\catalina.bat" stop
echo Tomcat stopped.
