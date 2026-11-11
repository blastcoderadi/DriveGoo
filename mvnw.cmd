@REM ----------------------------------------------------------------------------
@REM DriveGo Maven Wrapper forwarding script
@REM ----------------------------------------------------------------------------
@echo off
set "MAVEN_BIN=C:\antigravity\vechical-rental-system\apache-maven-3.9.6\bin\mvn.cmd"
if exist "%MAVEN_BIN%" (
    "%MAVEN_BIN%" %*
) else (
    mvn %*
)

