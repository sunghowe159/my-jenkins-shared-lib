@Library('my-jenkins-shared-lib@dev') _

customPipeline.runTask([:]) {
    stage('Build') {
        runCmd('''
        echo 'start building'
        ''')
    }
    stage('Test') {
        runCmd('''
        echo 'start testing'
        ''')
    }
}