@Library('my-jenkins-shared-lib') _

// This Jenkins pipeline is used to test the customPipeline.cicd function without Docker.
customPipeline.run([
    nodeLabel: 'ci-x86',
    archiveArtifacts: 'build/*.log, output/**/*.txt'
]) {
    stage('Hello') {
        echo "Hello, Jenkins!"
    }
    stage('Build') {
        echo "Building the project..."
        sh '''
        echo "This is a build stage."
        echo "Build completed!" > build/build_result.txt
        '''
    }
    stage('Test') {
        echo "Running tests..."
        sh '''
        echo "This is a test stage."
        echo "Tests passed!" > output/test_result.txt
        '''
    }
}
