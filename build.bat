@echo off
echo Compiling...
javac -d bin -cp lib\gson-2.13.2.jar src\models\*.java src\server\*.java src\server\handlers\*.java src\server\db\*.java src\client\*.java src\client\handlers\*.java
if %errorlevel% == 0 (
    echo Build successful!
) else (
    echo Build failed!
)
