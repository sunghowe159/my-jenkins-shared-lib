@Library('my-jenkins-shared-lib@dev') _

customPipeline.runTask([
    archiveArtifacts : 'build/**,test/*.log',
    ]) {
    stage('Prepare') {
        sh """
        echo GIT_USER: \${GIT_USER}
        echo GIT_PASS: \${GIT_PASS}
        echo SSH_KEY: \${SSH_KEY}
        echo SSH_PASSPHRASE: \${SSH_PASSPHRASE}
        echo SSH_USER: \${SSH_USER}
        """
    }
    codeSync([
      repoUrl: 'https://github.com/sunghowe159/FOA-IVFA-for-WSN',
      branch: 'master',
      credentialsId: 'haosong_github_user'
    ])
    stage('Build') {
        runCmd('''
        mkdir -p build test
        echo 'start building' > build/build.log
        uname -a
        ''')
    }
    stage('Test') {
        runCmd('''
        echo 'start testing' > build/test.log
        ''')
    }
}