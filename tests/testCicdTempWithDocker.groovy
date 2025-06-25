@Library('my-jenkins-shared-lib') _

// This Jenkins pipeline is used to test the customPipeline.cicd function with Docker.
customPipeline.run([
    nodeLabel: 'ci-x86',
    dockerImage: 'python:3.10-slim',
    dockerArgs: '-v /cache:/root/.cache',
    archiveArtifacts: 'build/*.log, output/**/*.txt'
]) {
    stage('Hello') {
        echo "Hello, Jenkins!"
    }
    stage('Build') {
        echo "Building the project..."
        sh '''
        echo "This is a build stage."
        echo "Build completed in Docker!" > build/build_result.txt
        '''
    }
    stage('Test') {
        echo "Running tests..."
        sh '''
        echo "This is a test stage."
        echo "Tests passed in Docker!" > output/test_result.txt
        '''
    }
}