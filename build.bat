@echo off
setlocal

rem Configura versione Gson qui (modifica se necessario)
set GSON_VERSION=2.13.2
set GSON_JAR=lib\gson-%GSON_VERSION%.jar
set JAVA_RELEASE=24

rem Crea cartella lib se non esiste
if not exist lib (
    mkdir lib
)

rem Scarica Gson se manca
if not exist "%GSON_JAR%" (
    echo Gson %GSON_VERSION% non trovato, scarico in lib...
    powershell -Command "try { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/%GSON_VERSION%/gson-%GSON_VERSION%.jar' -OutFile '%GSON_JAR%' } catch { exit 1 }"
    if %errorlevel% neq 0 (
        echo Errore durante il download di Gson. Controlla la connessione o scarica manualmente:
        echo https://repo1.maven.org/maven2/com/google/code/gson/gson/%GSON_VERSION%/gson-%GSON_VERSION%.jar
        goto :EOF
    )
)

echo Compiling...
javac --release %JAVA_RELEASE% -d bin -cp "%GSON_JAR%" src\models\*.java src\server\*.java src\server\handlers\*.java src\server\db\*.java src\client\*.java src\client\handlers\*.java src\client\menus\*.java src\models\enums\*.java
if %errorlevel% == 0 (
    echo Build successful!
) else (
    echo Build failed!
)

endlocal
