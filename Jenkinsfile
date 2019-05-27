library identifier: 'jpf@0.9.0', changelog: false, retriever: modernSCM([$class: 'GitSCMSource', remote: 'ssh://git@code.eu.idealo.com:7999/hpi/jenkins-pipeline-functions.git'])

String sourceCloneUrl = 'ssh://git@code.eu.idealo.com:7999/uds/deckard.git'
String mailto = 'team-postman@idealo.de'

jpf_notifyRun mailto, {

    node('java-ephemeral') {
        def pom = jpf_getPomProperties {
            gitRepoUrl = sourceCloneUrl
            branch = 'spring-kafka-2.2'
        }

        String inputVersion = jpf_getUserInput {
            message = 'asking release version'
            defaultValue = ' '
            description = "Which version shall be used for the release (current: ${pom.version})?"
        }

        jpf_maven {
            message = "setting property version to ${inputVersion}"
            mavenGoals = "versions:set -DartifactId=deckard -DnewVersion=${inputVersion}"
        }

        jpf_buildReleasable {
            message = "building the releasable for version ${inputVersion}"
            branch = 'spring-kafka-2.2'
            artifactoryTargetRepo = 'libs-release-local'
            stashArtifacts = 'false'
            mavenSkipClean = 'true'
            sonarqubeHostUrl = 'https://analysis.eu.idealo.com'
        }

        jpf_createBuildVersion {
        	message = 'creating tag'
            branch = 'spring-kafka-2.2'
        }
    }
}