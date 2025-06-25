# 流水线模板介绍

`sqaTools` 是基于 Jenkins Scripted Pipeline 的流水线工具，统一封装了以下功能：

- [提供一套 Pipeline 代码模板](#21-流水线模板)
- [流水线错误处理](#22-错误处理)
- [构建结果通知](#23-构建结果通知)
---

## 2. 工具使用说明
### 2.1 流水线模板

- 该模板旨在为 Jenkins 流水线提供统一的执行环境封装，提升复用和可维护性。
- 函数名：
`def cicd(Map config = [:], Closure pipeline = {})`
- 调用流水线模板，流水线主体代码需符合 Scripted Pipeline 语法规范。
    ```groovy
    sqaTools.cicd([
        nodeLabel: 'ci-node-label',
        dockerImage: 'python:3.10',
        dockerArgs: '-v /cache:/root/.cache',
        credentials: [
            string(credentialsId: 'your-cred-id', variable: 'TOKEN')
        ],
        archiveArtifacts: 'build/output/**/*.zip'
    ]) {
        timeout(time: 30, unit: 'MINUTES') {
            stage('Build') {
                sh 'make all'
            }
            stage('Test') {
                sh 'make test'
            }
        }
    }
    ```
- 模板配置参数说明

    |参数	|类型	|说明	|默认值|
    |--|--|--|--|
    |nodeLabel|	String	|Jenkins 执行节点标签|	'build-node'
    |dockerImage|	String	|Docker 镜像名称，非空时启用容器|	''
    |dockerArgs|	String	|Docker 运行参数，如挂载缓存目录等	|''
    |credentials|	List	|Jenkins 凭证列表（支持多种类型）|	[]
    |archiveArtifacts|	String	|构建产物归档路径，支持通配符，逗号分隔|	''
### 2.2 错误处理
- 函数名：`def handleError(Exception err)`
- 异常处理函数，在流水线异常时被调用，作用包括：
    - 捕获错误日志；
    - 调用 notifyResult(type: 'FAILURE', error: err) 发送失败通知；
    - 保留扩展点（如记录错误到日志系统、触发飞书通知等）；
    - ***对异常处理待进行增强处理。***
- 调用示例：

    ```groovy
    try {
        ......
    } catch(err) {
        sqaTools.handleError(err)
    }
    ```
### 2.3 构建结果通知
- 函数名：`def notifyResult(Map args = [:])`
- 用于发送构建结果通知，支持所有 Jenkins 构建状态；
    |参数|	说明|
    |--|--|
    |type|	构建结果类型：SUCCESS、FAILURE、ABORTED、UNSTABLE、NOT_BUILT
    |error|	可选，传入异常对象（显示异常信息）|
    |subject|	邮件标题（可选）|
    |body|	邮件正文（可选）|
    |to|	收件人邮箱（默认取 NOTIFY_EMAIL_TO）|
- 调用示例：

    ```groovy
    sqaTools.notifyResult(type: 'UNSTABLE', error: err)
    ```
- 模板中已自动集成结果判断与通知逻辑：
    > ***（待优化）无需用户在 Jenkinsfile 中手动处理结果判断。***

    |场景	|行为|
    |--|--|
    |正常成功	|自动发送成功通知|
    |抛出异常	|调用 handleError()，发送失败通知|
    |构建中止	|自动发送 ABORTED 通知|
    |设置 UNSTABLE	|自动发送不稳定通知|