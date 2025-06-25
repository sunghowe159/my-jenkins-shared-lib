@Library('my-jenkins-shared-lib') _

// This Jenkins pipeline is used to test the codeSync function.
env.SINGLE_REPO_CFG = [
    repoUrl: 'https://gitlab.example.com/my-group/my-repo.git',
    branch: 'main'
]

env.MULTI_REPO_CFG = [
    manifest: 'config/manifest/gitlab-manifest.yaml'
]

customPipeline.run([
    nodeLabel: 'ci-x86',
    dockerImage: 'python:3.10-slim',
    dockerArgs: '-v /cache:/root/.cache',
    archiveArtifacts: 'build/*.log, output/**/*.txt'
]) {
    stage('Hello') {
        echo "Hello, Jenkins!"
    }
    codeSync(SINGLE_REPO_CFG)
    codeSync(MULTI_REPO_CFG)
}