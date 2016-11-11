#!/bin/ksh

mkdir -p bin
JAVAC="$JAVA_HOME/bin/javac"
export JAVAC
CLASSPATH=./:../Shared:../Examples:../../Libs/upa.jar:../../Libs/upaValueAdd.jar
export CLASSPATH
SHARED_PATH=../Shared/com/thomsonreuters/upa/shared
export SHARED_PATH
TRAINING_EXAMPLE_PATH=com/thomsonreuters/upa/training
export TRAINING_EXAMPLE_PATH

rm -f `find . -name *.class`

$JAVAC -version -target 1.7 -source 1.7 -d bin $SHARED_PATH/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin $SHARED_PATH/provider/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin $SHARED_PATH/rdm/marketbyorder/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin $SHARED_PATH/rdm/marketbyprice/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin $SHARED_PATH/rdm/marketprice/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin $SHARED_PATH/rdm/symbollist/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin $SHARED_PATH/rdm/yieldcurve/*.java 
$JAVAC -version -target 1.7 -source 1.7 -d bin $TRAINING_EXAMPLE_PATH/consumer/*.java
$JAVAC -version -target 1.7 -source 1.7 -d bin $TRAINING_EXAMPLE_PATH/niprovider/*.java
$JAVAC -version -target 1.7 -source 1.7 -d bin $TRAINING_EXAMPLE_PATH/provider/*.java

