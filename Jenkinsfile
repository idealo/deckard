@Library('idp@0.17.0')

String repo = "ssh://git@code.eu.idealo.com:7999/uds/deckard.git"
String mailTo = 'team-postman@idealo.de'
String mvnGoalSetVersion = "versions:set -DnewVersion=${BUILD_TIMESTAMP}"
String version

idp_notifyRun mailTo, {
    node('java') {
        idp_maven {
            gitRepoUrl = repo
            mavenGoals = mvnGoalSetVersion
            stage = 'set version number'
        }
        version = idp_buildReleasable {
            mavenVersion = 'apache-maven-3.5.0'
            artifactoryTargetRepo = 'libs-release-local'
            stashArtifactsIncludePattern = 'target/*.jar'
            stashArtifactsExcludePattern = 'target/*-stubs.jar, target/*-sources.jar'
        }
        idp_repoTag {
            tagName = version
        }
    }
}