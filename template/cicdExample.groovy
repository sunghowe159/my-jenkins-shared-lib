@Library('my-shared-library') _

sqaTools.cicd([
    nodeLabel: 'ci-x86',
    dockerImage: 'python:3.10-slim',
    dockerArgs: '-v /cache:/root/.cache',
    credentials: [
        string(credentialsId: 'feishu-api-token', variable: 'FEISHU_TOKEN')
    ],
    archiveArtifactsPatterns: 'build/libs/*.jar, reports/**/*.html'
]) {
    timeout(time: 30, unit: 'MINUTES') {
        stage('Checkout') {
            sh '''
                git clone https://$GIT_USER:$GIT_PASS@git.example.com/my/repo.git
            '''
        }
        stage('Test') {
            sh 'pytest tests/'
        }
    }
}
