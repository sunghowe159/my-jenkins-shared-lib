@Library('my-jenkins-shared-lib') _

// This Jenkins pipeline is used to test the sqaTools_CodeSync function.
sqaTools.cicd([
    nodeLabel: 'ci-x86',
    dockerImage: 'python:3.10-slim',
    dockerArgs: '-v /cache:/root/.cache',
    archiveArtifacts: 'build/*.log, output/**/*.txt'
]) {
    stage('Hello') {
        echo "Hello, Jenkins!"
    }
    sqaTools_CodeSync([
        repoUrl: 'https://gitlab.example.com/my-group/my-repo.git',
        branch: 'main',
        credentialsId: 'my-git-cred',
        shallow: true,
        cleanBefore: true
    ])
    sqaTools_CodeSync([
        manifest: 'config/manifest/gitlab-manifest.yaml',
        credentialsId: 'my-git-cred',
        shallow: true,
        cleanBefore: true
    ])

}