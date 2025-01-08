set -e # Exit on failure

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
echo "Check jdk version used below"
java -version

echo "compiling ..."
rm -rf $SCRIPTPATH/build-jar/
mkdir -p $SCRIPTPATH/build-jar/
javac $SCRIPTPATH/ingescape/src/com/ingescape/*.java -cp "$SCRIPTPATH/ingescape/libs/*" -d "$SCRIPTPATH/build-jar/"

echo "copying libs..."
mkdir $SCRIPTPATH/build-jar/libs
cp $SCRIPTPATH/ingescape/libs/*.jar $SCRIPTPATH/build-jar/libs/

echo "creating jar ..."
cd $SCRIPTPATH/build-jar
jar cvf ../ingescape/jar/ingescape.jar ./com/ ./libs/
