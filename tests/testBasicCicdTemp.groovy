@Library('my-shared-library') _

sqaTools.cicd([
    nodeLabel: 'ci-x86'
]) {
    stage('Hello') {
        echo "Hello, Jenkins!"
        sh '''
            echo "This is a basic CI/CD pipeline example."
        '''
    }
}
