@Library('my-jenkins-shared-lib@master') _

customPipeline.runTask([]) {
    stage('Build') {
        sh 'make'
    }
    stage('Test') {
        sh 'make test'
    }
}