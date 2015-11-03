@echo off
echo Compiling....
if exist SerWeb\nul goto compile
md SerWeb

:compile
javac *.java
move *.class serweb
echo Building SerWeb.jar...
jar cvfm SerWeb.jar meta-inf\manifest.mf SerWeb
echo Process finished....

