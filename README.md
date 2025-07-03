# Jenkins Shared Libraries - 用户文档
---
## 1. 介绍
随着越来越多的项目采用流水线，一些通用的模式可能会逐渐出现。为了减少代码冗余，可以在不同项目之间共享流水线的部分功能，这部分共享功能即 Shared Libraries。
### 1.1 引入共享库
在 Jenkinsfile 顶部引入共享库，后续流水线中即可调用共享库函数。

```groovy
@Library('my-jenkins-shared-lib') _
```
### 1.2  共享库说明
#### 1.2.1 src目录 —— Groovy 工具类、复用模块
存放开发的工具类、逻辑组件、数据结构，被 vars/ 或其他 src/ 类引用。
#### 1.2.2 vars目录 —— 共享库的函数入口
定义暴露给 Jenkinsfile 使用的函数，每个 .groovy 文件对应一个全局变量；

文件名就是函数名，例如 vars/myStep.groovy → myStep(...)；

通常包含一个 call() 方法，使其可直接像函数一样被调用。
#### 1.2.4 resources目录 —— 存放静态文件模板、脚本
存放脚本、模板、配置文件，可以通过 libraryResource() 加载为字符串

使用相对路径读取：libraryResource('org/example/script.sh')

常用于注入 shell 脚本、Groovy 模板等
#### 1.2.4 tests目录
针对本库封装的工具或函数编写的Jenkinsfile示例
#### 1.2.5 docs目录
各工具、模板说明文档

## 2. 共享库vars功能说明
### 2.1 customPipeline 使用说明
> `customPipeline` 是基于 Jenkins Scripted Pipeline 的流水线工具，统一封装若干功能。详情见文档：[customPipeline](docs/customPipeline.md)
### 2.2 codeSync 使用说明
> 通用 GitLab 代码拉取工具，支持单仓和多仓（并发）拉取，适配 Merge Request 场景与普通分支构建。详情见文档：[codeSync](docs/codeSync.md)
### 2.3 runCmd 使用说明
> 封装 Jenkins 原生 sh / bat，可直接传入字符串或参数 Map，支持多行命令、返回结果等。详情见文档：[runCmd](docs/runCmd.md)
## 3.共享库resources/script说明
### 3.1 build_pipeline.sh 使用说明
> 该脚本用于触发 Jenkins Job，并实时监控其状态，支持带参触发。详情见文档：[build_pipeline.sh](docs/script/buildPipelineUsage.md)

## 4. 测试工具

基于 JenkinsPipelineUnit （Groovy + Spock/JUnit）来进行单元测试，测试 Shared Library 的脚本语法、流程结构、DSL 传参是否正确。在本地模拟流水线运行，快速验证：

- pipeline 的 DSL 是否能正常解析（不会 MissingMethodException / No such property）。

- 各步骤的执行顺序、传参、循环展开是否符合预期。

- 分支流程（try-catch, catchError, parallel）能正常流转。

- 自定义 Shared Library 的函数、工具、配置类能正确调用。

用于保证语法正确、参数正确、流程正确，流水线本身实现的自动化功能仍需在CI上验证。

详情见：[howToRunTest](docs/test/howToRunTest.md)

## 5. 参考文档

[Jenkins用户手册](https://www.jenkins.io/doc/book/getting-started/)

[Groovy文档](https://groovy-lang.org/documentation.html)

[Using Docker With Pipeline](https://www.jenkins.io/doc/book/pipeline/docker/)

[Pipeline Steps Reference](https://www.jenkins.io/doc/pipeline/steps/)

[Scripted Pipeline简介](https://www.jenkins.io/doc/book/pipeline/syntax/#scripted-pipeline)

[JenkinsPipelineUnit](https://github.com/jenkinsci/JenkinsPipelineUnit)