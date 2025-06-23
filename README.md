# Jenkins Shared Library - sqaTools 模板说明

---

## 1. 介绍

`sqaTools` 是基于 Jenkins Scripted Pipeline 的流水线模板，统一封装了以下功能：

- 节点选择（`node`）
- Docker 容器运行（根据 `dockerImage` 配置自动启用）
- 凭证注入（支持多种凭证类型，自动添加默认凭证）
- 异常捕获与统一处理（`handleError()`）
- 构建产物归档（可配置归档路径）
- 构建结果通知（`notifyResult()`，支持 SUCCESS、FAILURE、ABORTED、UNSTABLE 等）

该模板旨在为 Jenkins 流水线提供统一的执行环境封装，提升复用和可维护性。

---

## 2. 使用方法

### 2.1 引入共享库

在 Jenkinsfile 顶部引入共享库：

```groovy
@Library('my-jenkins-shared-lib') _
```
### 2.2 调用流水线模板
流水线主体代码需符合 Scripted Pipeline 语法规范。
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
## 3. 配置参数说明
|参数	|类型	|说明	|默认值|
|--|--|--|--|
|nodeLabel|	String	|Jenkins 执行节点标签|	'build-node'
|dockerImage|	String	|Docker 镜像名称，非空时启用容器|	''
|dockerArgs|	String	|Docker 运行参数，如挂载缓存目录等	|''
|credentials|	List	|Jenkins 凭证列表（支持多种类型）|	[]
|archiveArtifacts|	String	|构建产物归档路径，支持通配符，逗号分隔|	''
## 4. 错误处理
模板内置了异常捕获机制，捕获到异常时会打印错误日志，并自动标记构建状态为失败 (FAILURE)。

默认错误处理在 handleError 函数内实现，用户可根据需要扩展该方法，集成通知（飞书、Slack、邮件等）。
### 4.1 handleError(Exception err)
模板内部封装的异常处理函数，在流水线异常时被调用，作用包括：

捕获错误日志；

调用 notifyResult(type: 'FAILURE', error: err) 发送失败通知；

保留扩展点（如记录错误到日志系统、触发飞书通知等）；

保留 handleError() 是为了未来对异常处理进行统一增强。
### 4.2 notifyResult(Map args = [:])
用于发送构建结果通知，支持所有 Jenkins 构建状态：

参数	说明
type	构建结果类型：SUCCESS、FAILURE、ABORTED、UNSTABLE、NOT_BUILT
error	可选，传入异常对象（显示异常信息）
subject	邮件标题（可选）
body	邮件正文（可选）
to	收件人邮箱（默认取 NOTIFY_EMAIL_TO）
调用示例：

```groovy
sqaTools.notifyResult(type: 'UNSTABLE', error: err)
```
### 4.3 模板中行为自动化说明
模板中已自动集成结果判断与通知逻辑：

|场景	|行为|
|--|--|
|正常成功	|自动发送成功通知|
|抛出异常	|调用 handleError()，发送失败通知|
|构建中止	|自动发送 ABORTED 通知|
|设置 UNSTABLE	|自动发送不稳定通知|

无需用户在 Jenkinsfile 中手动处理结果判断。
## 5. 环境变量建议
通过环境变量配置收件人：

```groovy
env.NOTIFY_EMAIL_TO = 'ci-team@example.com'
```
或在 Jenkins 系统/文件夹级别统一配置变量。
## 6. 注意事项
pipeline 参数的闭包体需遵循 Scripted Pipeline 语法（支持 stage、timeout、sh 等标准步骤）。

Docker 运行环境只会在 node 环境内创建，保证执行环境稳定。

构建产物归档会在流水线完成后执行，无论成功还是失败，确保产物不会丢失。

## 7. 未来扩展TODO
增加统一日志输出和阶段封装工具，如 runStage 等。

扩展错误处理，内置多渠道通知模块。

支持多语言、多构建环境的模板分发。

自动管理和注入凭证，结合 Jenkins Folder 级配置。

增强流水线参数化支持，实现更灵活配置。
