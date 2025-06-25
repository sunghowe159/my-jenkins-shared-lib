// vars/customPipeline.groovy

/**
 * 主流水线模板函数
 * 统一封装 node、docker、凭证、异常捕获和产物归档
 * @param config Map 配置项
 *        nodeLabel: Jenkins节点标签，默认 'build-node'
 *        dockerImage: Docker 镜像名，非空则启用 docker 容器执行
 *        dockerArgs: docker 参数，如 -v 缓存挂载，默认空
 *        credentials: withCredentials 需要的凭证列表，默认空（会自动添加默认凭证）
 *        archiveArtifacts: 构建产物归档路径，空则不归档
 * @param pipeline Closure 具体流水线逻辑，遵守脚本式Pipeline规范
 */
def run(Map config = [:], Closure pipeline = {}) {
    def defaultConfig = [
        nodeLabel: 'build-node',
        dockerImage: '',
        dockerArgs: '',
        credentials: [],
        archiveArtifacts: ''
    ]

    def cicd_config = defaultConfig + config

    // 默认凭证添加
    cicd_config.credentials += [
        usernamePassword(
            credentialsId: 'default-git-cred',
            usernameVariable: 'GIT_USER',
            passwordVariable: 'GIT_PASS'
        )
    ]

    this.binding.setVariable('cicd_config', cicd_config)
    cicd_code = {
        withCredentials(cicd_config.credentials) {
            def capturedException = null
            try_pipeline = {
                try {
                    pipeline() 
                    if (!currentBuild.result) {
                        currentBuild.result = 'SUCCESS'
                    }
                } catch (e) {
                    currentBuild.result = 'FAILURE'
                    capturedException = e
                    handleError(e)
                    throw e
                } finally {
                    if (cicd_config.archiveArtifacts?.trim()) {
                        archiveArtifacts artifacts: cicd_config.archiveArtifacts, allowEmptyArchive: true
                    }

                    // 成功 / 非失败状态，也发送一次结果通知
                    if (currentBuild.result != 'FAILURE') {
                        notifyResult(type: currentBuild.result, error: capturedException)
                    }
                }
            }
            if(cicd_config.dockerImage) {
                runWithDocker(cicd_config) {
                    try_pipeline()
                }
            } else {
                try_pipeline()
            }
            
        }
    }

    node(cicd_config.nodeLabel) {
        cicd_code()
    }
}


/**
 * 使用 docker 容器运行流水线
 */
def runWithDocker(Map config, Closure pipeline) {
    docker.withRegistry('https://registry.example.com', 'credentials-id') {
        docker.image(config.dockerImage).pull() // 确保拉取最新镜像
        docker.image(config.dockerImage).inside(config.dockerArgs ?: '') {
            pipeline()
        }
    }
}

/**
 * 默认错误处理函数
 * 可扩展为发送通知等
 */
def handleError(Exception err) {
    echo "❌ 构建失败：${err?.getMessage() ?: 'Unknown Error'}"

    // 可扩展：写入日志系统、统计系统、清理逻辑等
    // sendToLogSystem(err)
    // markBuildStatusInSystem('FAILURE')

    notifyResult(
        type: 'FAILURE',
        error: err
    )
}


/**
 * 通用构建结果通知工具（当前版本仅支持邮件）
 * 参数:
 *   type     : 'success' / 'failure'，默认根据 currentBuild.result 自动判断
 *   subject  : 邮件标题（可选）
 *   body     : 邮件内容正文（可选）
 *   to       : 收件人，默认取 env.NOTIFY_EMAIL_TO
 */
def notifyResult(Map args = [:]) {
    def result = (args.type ?: currentBuild.result ?: 'SUCCESS').toUpperCase()
    def errorMsg = args.error?.message ?: ''
    def resultLabel = [
        'SUCCESS'  : '成功',
        'FAILURE'  : '失败',
        'ABORTED'  : '中止',
        'UNSTABLE' : '不稳定',
        'NOT_BUILT': '未执行'
    ][result] ?: result

    def subject = args.subject ?: "[Jenkins 构建${resultLabel}] ${env.JOB_NAME} #${env.BUILD_NUMBER}"

    def body = args.body ?: """
        构建结果: ${resultLabel}
        项目: ${env.JOB_NAME}
        分支: ${env.BRANCH_NAME ?: 'N/A'}
        编号: #${env.BUILD_NUMBER}
        构建链接: ${env.BUILD_URL}
        ${errorMsg ? "\n异常信息: ${errorMsg}" : ""}
    """

    def to = args.to ?: env.NOTIFY_EMAIL_TO ?: 'ci-team@example.com'

    emailext(
        subject: subject,
        body: body,
        to: to,
        mimeType: 'text/plain'
    )

    echo "📨 构建结果通知 [${resultLabel}] 已发送至 ${to}"
}

