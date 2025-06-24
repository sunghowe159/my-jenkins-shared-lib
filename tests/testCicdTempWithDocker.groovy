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
    stage('Hello') {
        echo "Hello, Jenkins!"
        sh '''
            echo "This is a CI/CD pipeline example using Docker."
        '''
    }
}
