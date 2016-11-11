#!/bin/ksh

mkdir -p bin
JAVAC="$JAVA_HOME/bin/javac"
export JAVAC
CLASSPATH=./:../Shared:./xpp3-1.1.3_8.jar:./xpp3_min-1.1.3_8.jar:../../Libs/upa.jar:../../Libs/upaValueAdd.jar:../../../../Elektron-SDK-BinaryPack/Java/Eta/Libs/upa.jar
export CLASSPATH

rm -f `find . -name *.class`

$JAVAC -version -target 1.7 -source 1.7 -d bin ../Shared/com/thomsonreuters/upa/shared/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin ../Shared/com/thomsonreuters/upa/shared/provider/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin ../Shared/com/thomsonreuters/upa/shared/rdm/marketbyorder/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin ../Shared/com/thomsonreuters/upa/shared/rdm/marketbyprice/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin ../Shared/com/thomsonreuters/upa/shared/rdm/marketprice/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin ../Shared/com/thomsonreuters/upa/shared/rdm/symbollist/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin ../Shared/com/thomsonreuters/upa/shared/rdm/yieldcurve/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin com/thomsonreuters/upa/perftools/common/*.java
$JAVAC -version -target 1.7 -source 1.7 -d bin com/thomsonreuters/upa/perftools/upajconsperf/*.java
$JAVAC -version -target 1.7 -source 1.7 -d bin com/thomsonreuters/upa/perftools/upajniprovperf/*.java
$JAVAC -version -target 1.7 -source 1.7 -d bin com/thomsonreuters/upa/perftools/upajprovperf/*.java
$JAVAC -version -target 1.7 -source 1.7 -d bin com/thomsonreuters/upa/perftools/upajtransportperf/*.java
