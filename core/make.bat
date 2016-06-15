set BAT_DIR=%~dp0
%BAT_DIR:~0,2%
cd %BAT_DIR%
cmd /c mvn clean install -Dmaven.test.skip=true -e -U
pause
