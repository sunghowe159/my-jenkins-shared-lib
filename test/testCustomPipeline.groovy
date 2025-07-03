@Library('my-jenkins-shared-lib@dev') _

customPipeline.runTask([:]) {
    stage('Build') {
        sh 'make'
    }
    stage('Test') {
        sh 'make test'
    }
}