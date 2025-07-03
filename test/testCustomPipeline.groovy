@Library('my-jenkins-shared-lib@dev') _

customPipeline.runTask([:]) {
    stage('Prepare') {
        sh """
        echo GIT_USER: \${GIT_USER}
        echo GIT_PASS: \${GIT_PASS}
        echo SSH_KEY: \${SSH_KEY}
        echo SSH_PASSPHRASE: \${SSH_PASSPHRASE}
        echo SSH_USER: \${SSH_USER}
        """
    }
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