#!/usr/bin/env bash

set -e

# only do deployment, when travis detects a new tag
if [ ! -z "$TRAVIS_TAG" ]
then
    echo "on a tag -> set pom.xml <version> to $TRAVIS_TAG"
    mvn --settings .travis/settings.xml org.codehaus.mojo:versions-maven-plugin:2.3:set -DnewVersion=$TRAVIS_TAG -Prelease

    mvn clean deploy --settings .travis/settings.xml -DskipTests=true -B -U -Prelease

else
    echo "not on a tag -> keep snapshot version in pom.xml"
fi