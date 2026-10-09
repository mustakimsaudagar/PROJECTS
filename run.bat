@echo off
setlocal
echo ========================================================
echo    Starting Library Management System (SmartLib)
echo ========================================================

set "JAVA_HOME=C:\Program Files\Java\jdk-23"
set "PROJECT_DIR=%~dp0"
set "MAVEN_CMD=%PROJECT_DIR%tools\apache-maven-3.9.9\bin\mvn.cmd"
set "CATALINA_HOME=%PROJECT_DIR%tools\apache-tomcat-10.1.34"

echo.
echo [1/3] Building Project with Maven...
call "%MAVEN_CMD%" clean package
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Deploying library.war to Apache Tomcat...
copy /Y "%PROJECT_DIR%target\library.war" "%CATALINA_HOME%\webapps\library.war"
copy /Y "%PROJECT_DIR%target\library.war" "%CATALINA_HOME%\webapps\ROOT.war"

echo.
echo [3/3] Launching Apache Tomcat 10.1 Server...
echo Server running at: http://localhost:8080/library/
echo Default credentials:
echo   - Admin:   username 'admin'   / password 'admin123'
echo   - Student: username 'student' / password 'student123'
echo.

start "" "http://localhost:8080/library/"
call "%CATALINA_HOME%\bin\catalina.bat" run
