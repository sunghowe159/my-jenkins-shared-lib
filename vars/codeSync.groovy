/**
 * codeSync.groovy
 *
 * 拉取 GitLab 仓库代码：
 *  - 支持单仓拉取（repoUrl + branch）
 *  - 支持多仓拉取（manifest 模式，支持并发）
 *
 * 参数（单仓）：
 *   repoUrl        : 仓库地址（必填）
 *   branch         : 分支名（默认 main）
 *   isMr           : 是否为 MR 模式（默认 false）
 *   mrSourceBranch : MR 源分支（isMr = true 时必填）
 *
 * 参数（多仓）：
 *   manifest       : YAML 格式的仓库列表清单
 *
 * 通用参数：
 *   credentialsId  : Jenkins 凭证 ID（默认: default-git-cred）
 *   shallow        : 是否使用浅克隆（默认: true）
 *   cleanBefore    : 是否在拉取前清理目录（默认: true）
 */
def call(Map config = [:]) {
    if (config.repoUrl) {
        echo "[CodeSync] ▶ 模式：单仓库"
        _checkoutSingleRepo(config)
    } else if (config.manifest) {
        echo "[CodeSync] ▶ 模式：多仓库 (manifest)"
        _checkoutMultiRepos(config)
    } else {
        error "[CodeSync] ❌ 请提供 repoUrl（单仓）或 manifest（多仓）参数"
    }
}

// ========== 单仓封装函数 ==========
def _checkoutSingleRepo(Map config) {
    def repoUrl = config.repoUrl
    def isMr = config.get('isMr', false)
    def branch = isMr ? config.mrSourceBranch : (config.branch ?: 'main')
    def credentialsId = config.credentialsId ?: 'default-git-cred'
    def shallow = config.get('shallow', true)
    def cleanBefore = config.get('cleanBefore', true)

    if (isMr && !config.mrSourceBranch) {
        error "[CodeSync] ❌ isMr 为 true 时必须指定 mrSourceBranch"
    }

    stage("Checkout: ${branch}") {
        echo "[CodeSync] 拉取仓库：${repoUrl} @ ${branch}"
        if (cleanBefore) {
            deleteDir()
        }

        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/${branch}"]],
            userRemoteConfigs: [[
                url: repoUrl,
                credentialsId: credentialsId
            ]],
            doGenerateSubmoduleConfigurations: false,
            extensions: shallow ? [[
                $class: 'CloneOption',
                depth: 1,
                noTags: false,
                reference: '',
                shallow: true,
                timeout: 10
            ]] : []
        ])
    }
}

// ========== 多仓封装函数（并发） ==========
def _checkoutMultiRepos(Map config) {
    def manifestFile = config.manifest
    def credentialsId = config.credentialsId ?: 'default-git-cred'
    def shallow = config.get('shallow', true)
    def cleanBefore = config.get('cleanBefore', true)

    def manifest = readYaml file: manifestFile
    def checkoutTasks = [:]

    manifest.repos.each { repo ->
        def name = repo.name
        def url = repo.url
        def branch = repo.branch ?: 'main'

        checkoutTasks["checkout-${name}"] = {
            stage("Checkout: ${name}") {
                dir(name) {
                    if (cleanBefore) {
                        echo "[CodeSync] 清理目录 ${name}"
                        deleteDir()
                    }

                    echo "[CodeSync] 拉取 ${url} (${branch})"

                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${branch}"]],
                        userRemoteConfigs: [[
                            url: url,
                            credentialsId: credentialsId
                        ]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: shallow ? [[
                            $class: 'CloneOption',
                            depth: 1,
                            noTags: false,
                            reference: '',
                            shallow: true,
                            timeout: 10
                        ]] : []
                    ])
                }
            }
        }
    }

    parallel checkoutTasks
}
