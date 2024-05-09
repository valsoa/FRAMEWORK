set "name=FrontController"
set "package=util\*"
javac -d . java/*.java
md jar
jar -cvf jar/%name%.jar  %package%.class

pause
