@echo off
SETLOCAL
echo ===============================================
echo Instalador definitivo de yt-dlp y ffmpeg
echo ===============================================

REM Carpeta destino
set "TOOLS_DIR=C:\Tools"

REM Crear carpeta si no existe
if not exist "%TOOLS_DIR%" (
    echo Creando carpeta %TOOLS_DIR%...
    mkdir "%TOOLS_DIR%"
) else (
    echo Carpeta %TOOLS_DIR% ya existe.
)

REM ===================== yt-dlp =====================
if exist "%TOOLS_DIR%\yt-dlp.exe" (
    echo yt-dlp.exe ya está instalado.
) else (
    echo Descargando yt-dlp.exe...
    powershell -Command "Invoke-WebRequest 'https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe' -OutFile '%TOOLS_DIR%\yt-dlp.exe'"
    if exist "%TOOLS_DIR%\yt-dlp.exe" (
        echo yt-dlp.exe instalado correctamente.
    ) else (
        echo ERROR: No se pudo descargar yt-dlp.exe
    )
)

REM ===================== ffmpeg =====================
if exist "%TOOLS_DIR%\ffmpeg.exe" (
    echo ffmpeg.exe ya está instalado.
) else (
    echo Descargando ffmpeg 64-bit...
    powershell -Command "Invoke-WebRequest 'https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip' -OutFile '%TOOLS_DIR%\ffmpeg.zip'"

    if exist "%TOOLS_DIR%\ffmpeg.zip" (
        echo Descomprimiendo ffmpeg...
        powershell -Command "Expand-Archive -Path '%TOOLS_DIR%\ffmpeg.zip' -DestinationPath '%TOOLS_DIR%' -Force"

        REM Mover ffmpeg.exe a la carpeta principal
        for /r "%TOOLS_DIR%" %%i in (ffmpeg.exe) do move /Y "%%i" "%TOOLS_DIR%\ffmpeg.exe"

        REM Limpiar archivos temporales
        rmdir /S /Q "%TOOLS_DIR%\ffmpeg-release-essentials" 2>nul
        del "%TOOLS_DIR%\ffmpeg.zip" 2>nul

        if exist "%TOOLS_DIR%\ffmpeg.exe" (
            echo ffmpeg.exe instalado correctamente.
        ) else (
            echo ERROR: No se pudo mover ffmpeg.exe
        )
    ) else (
        echo ERROR: No se pudo descargar ffmpeg.zip
    )
)

REM ===================== Añadir PATH =====================
echo Verificando PATH...
echo %PATH% | findstr /I "%TOOLS_DIR%" >nul
if %errorlevel%==0 (
    echo %TOOLS_DIR% ya está en el PATH del sistema.
) else (
    echo Añadiendo %TOOLS_DIR% al PATH del sistema...
    setx PATH "%PATH%;%TOOLS_DIR%" /M >nul
    echo PATH actualizado. Reinicia tu terminal o VS Code para aplicar los cambios.
)

echo ===============================================
echo Instalación finalizada.
echo yt-dlp: %TOOLS_DIR%\yt-dlp.exe
echo ffmpeg : %TOOLS_DIR%\ffmpeg.exe
echo ===============================================
pause
ENDLOCAL

