// vars/sqaTools.groovy

/**
 * 主流水线模板函数
 * 统一封装 node、docker、凭证、异常捕获和产物归档
 * @param config Map 配置项
 *        nodeLabel: Jenkins节点标签，默认 'build-node'
 *        dockerImage: Docker 镜像名，非空则启用 docker 容器执行
 *        dockerArgs: docker 参数，如 -v 缓存挂载，默认空
 *        credentials: withCredentials 需要的凭证列表，默认空（会自动添加默认凭证）
 *        archiveArtifactsPatterns: 构建产物归档路径，空则不归档
 * @param pipeline Closure 具体流水线逻辑，遵守脚本式Pipeline规范
 */
def cicd(Map config = [:], Closure pipeline = {}) {
    def defaultConfig = [
        nodeLabel              : 'build-node',
        dockerImage            : '',
        dockerArgs             : '',
        credentials            : [],
        archiveArtifacts       : ''
    ]

    def cicd_config = defaultConfig + config

    // 自动添加默认凭证（如 Git 拉取凭证）
    cicd_config.credentials += [
        usernamePassword(
            credentialsId: 'default-git-cred',
            usernameVariable: 'GIT_USER',
            passwordVariable: 'GIT_PASS'
        )
    ]

    this.binding.setVariable('cicd_config', cicd_config)

    node(cicd_config.nodeLabel) {
        withCredentials(cicd_config.credentials) {
            try {
                runWithDockerIfNeeded(cicd_config) {
                    pipeline.call()
                }
            } catch (e) {
                echo "[cicd] 捕获异常：${e.getMessage()}"
                currentBuild.result = 'FAILURE'
                handleError(e)
                throw e
            } finally {
                if (cicd_config.archiveArtifactsPatterns?.trim()) {
                    echo "[cicd] 归档构建产物：${cicd_config.archiveArtifactsPatterns}"
                    archiveArtifacts artifacts: cicd_config.archiveArtifactsPatterns, allowEmptyArchive: true
                }
            }
        }
    }
}

/**
 * 根据配置决定是否使用 docker 容器运行闭包
 */
def runWithDockerIfNeeded(Map config, Closure body) {
    if (config.dockerImage?.trim()) {
        docker.image(config.dockerImage).inside(config.dockerArgs ?: '') {
            body()
        }
    } else {
        body()
    }
}

/**
 * 默认错误处理函数
 * 可扩展为发送通知等
 */
def handleError(Exception err) {
    echo "❌ 默认错误处理：${err.getMessage()}"
    // TODO: 可调用通知函数，如 feishu/slack/email
}
