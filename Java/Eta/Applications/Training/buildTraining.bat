mkdir bin
set JAVAC="%JAVA_HOME%\bin\javac"
set CLASSPATH=.\;..\Shared;..\Examples;..\..\Libs\upa.jar;..\..\Libs\upaValueAdd.jar;

%JAVAC% -d bin ..\Shared\com\thomsonreuters\upa\shared\*.java 
%JAVAC% -d bin ..\Shared\com\thomsonreuters\upa\shared\provider\*.java 
%JAVAC% -d bin ..\Shared\com\thomsonreuters\upa\shared\rdm\marketbyorder\*.java 
%JAVAC% -d bin ..\Shared\com\thomsonreuters\upa\shared\rdm\marketbyprice\*.java 
%JAVAC% -d bin ..\Shared\com\thomsonreuters\upa\shared\rdm\marketprice\*.java 
%JAVAC% -d bin ..\Shared\com\thomsonreuters\upa\shared\rdm\symbollist\*.java 
%JAVAC% -d bin ..\Shared\com\thomsonreuters\upa\shared\rdm\yieldcurve\*.java 
%JAVAC% -d bin com\thomsonreuters\upa\training\consumer\*.java 
%JAVAC% -d bin com\thomsonreuters\upa\training\niprovider\*.java
%JAVAC% -d bin com\thomsonreuters\upa\training\provider\*.java

