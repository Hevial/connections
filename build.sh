#!/bin/bash

# Configura versione Gson
GSON_VERSION=2.13.2
GSON_JAR="lib/gson-$GSON_VERSION.jar"
JAVA_RELEASE=24

# Crea cartella lib se non esiste
if [ ! -d "lib" ]; then
    mkdir lib
fi

# Scarica Gson se manca
if [ ! -f "$GSON_JAR" ]; then
    echo "Gson $GSON_VERSION non trovato, scarico in lib..."

    curl -fLo "$GSON_JAR" "https://repo1.maven.org/maven2/com/google/code/gson/gson/$GSON_VERSION/gson-$GSON_VERSION.jar"

    if [ $? -ne 0 ]; then
        echo "Errore durante il download di Gson."
        echo "Scarica manualmente:"
        echo "https://repo1.maven.org/maven2/com/google/code/gson/gson/$GSON_VERSION/gson-$GSON_VERSION.jar"
        exit 1
    fi
fi

echo "Compiling..."

javac --release $JAVA_RELEASE -d bin -cp "$GSON_JAR" \
src/models/*.java \
src/server/*.java \
src/server/handlers/*.java \
src/server/db/*.java \
src/client/*.java \
src/client/handlers/*.java \
src/client/menus/*.java \
src/models/enums/*.java

if [ $? -eq 0 ]; then
    echo "Build successful!"
else
    echo "Build failed!"
fi