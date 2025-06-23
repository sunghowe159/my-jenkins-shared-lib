# Jenkins Shared Library - sqaTools 模板说明

---

## 1. 介绍

`sqaTools` 是基于 Jenkins Scripted Pipeline 的流水线模板，统一封装了以下功能：

- 节点选择（`node`）
- Docker 容器运行（根据 `dockerImage` 配置自动启用）
- 凭证注入（支持多种凭证类型，自动添加默认凭证）
- 异常捕获与统一处理
- 构建产物归档（可配置归档路径）

该模板旨在为 Jenkins 流水线提供统一的执行环境封装，提升复用和可维护性。

---

## 2. 使用方法

### 2.1 引入共享库

在 Jenkinsfile 顶部引入你的共享库：

```groovy
@Library('your-shared-library-name') _
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
## 5. 注意事项
pipeline 参数的闭包体需遵循 Scripted Pipeline 语法（支持 stage、timeout、sh 等标准步骤）。

Docker 运行环境只会在 node 环境内创建，保证执行环境稳定。

构建产物归档会在流水线完成后执行，无论成功还是失败，确保产物不会丢失。

## 6. 未来扩展TODO
增加统一日志输出和阶段封装工具，如 runStage 等。

扩展错误处理，内置多渠道通知模块。

支持多语言、多构建环境的模板分发。

自动管理和注入凭证，结合 Jenkins Folder 级配置。

增强流水线参数化支持，实现更灵活配置。
