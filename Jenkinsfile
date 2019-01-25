@Library('idp@0.17.0')

String repo = "ssh://git@code.eu.idealo.com:7999/uds/deckard.git"
String mailTo = 'team-postman@idealo.de'

idp_notifyRun mailTo, {
    node('java') {
        String version = idp_createBuildVersion {
            message = 'increasing version number'
            gitRepoUrl = 'ssh://git@code.eu.idealo.com:7999/uds/deckard.git'
            branch = 'master'
        }
        idp_buildReleasable {
            mavenVersion = 'apache-maven-3.5.0'
            artifactoryTargetRepo = 'libs-release-local'
            stashArtifactsIncludePattern = 'target/*.jar'
            stashArtifactsExcludePattern = 'target/*-stubs.jar, target/*-sources.jar'
        }
    }
}