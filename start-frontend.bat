@echo off
title Angular Frontend - dispense_app_ang
echo ========================================
echo   Demarrage du frontend Angular
echo   URL : http://localhost:4200
echo ========================================
echo.

cd /d "%~dp0dispense_app_ang"

if not exist "node_modules" (
    echo [INFO] node_modules absent - installation des dependances...
    npm install
    if errorlevel 1 (
        echo [ERREUR] npm install a echoue.
        pause
        exit /b 1
    )
    echo.
)

echo [INFO] Lancement de ng serve ^(Ctrl+C pour arreter^)...
echo.
npm start
pause
