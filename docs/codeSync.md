# codeSync 使用说明
>PS: 适用Gitlab，待开发Gerrit版本

> 通用 GitLab 代码拉取工具，支持单仓和多仓（并发）拉取，适配 Merge Request 场景与普通分支构建。
## 1. 支持功能概览
| 功能点                | 描述                                     |
| ------------------ | -------------------------------------- |
| 单个仓库拉取           | 使用 `repoUrl` 拉取指定分支                    |
| Merge Request 支持 | 拉取源分支（非合并分支）用于验证                       |
| 多仓库拉取（manifest）  | 支持 YAML 格式仓库清单                         |
| 多仓并发拉取           | 使用 `parallel` 同时 clone 多个项目            |                          |
| 浅克隆支持            | 默认只拉取最近 commit 以加快速度                   |等              |
## 2. 参数说明
| 参数名              | 类型      | 默认值                | 适用模式 | 说明                   |
| ---------------- | ------- | ------------------ | ---- | -------------------- |
| `repoUrl`        | String  | 无                  | 单仓   | Git 仓库地址             |
| `branch`         | String  | `main`             | 单仓   | 指定分支名（非 MR）          |
| `isMr`           | Boolean | `false`            | 单仓   | 是否为 Merge Request 模式 |
| `mrSourceBranch` | String  | 无                  | 单仓   | 若为 MR，指定源分支名         |
| `manifest`       | String  | 无                  | 多仓   | manifest 文件路径        |
| `credentialsId`  | String  | `default-git-cred` | 通用   | Jenkins Git 凭证 ID    |
| `shallow`        | Boolean | `true`             | 通用   | 是否使用浅克隆              |
| `cleanBefore`    | Boolean | `true`             | 通用   | 是否在拉取前清理目录           |
## 3. 使用示例
```groovy
// 拉取单个仓库
codeSync([
  repoUrl: 'https://gitlab.example.com/backend/service.git',
  branch: 'release-v2.0',
  credentialsId: 'gitlab-token'
])
// Merge Request场景
codeSync([
  repoUrl: 'https://gitlab.example.com/backend/service.git',
  isMr: true,
  mrSourceBranch: env.gitlabSourceBranch,
  credentialsId: 'gitlab-token'
])
// 拉取多个仓库（并发）
codeSync([
  manifest: 'resources/manifest/gitlab-manifest.yaml',
  credentialsId: 'gitlab-token'
])
```
## 4. Manifest 文件格式（YAML）
```yaml
repos:
  - name: common-lib
    url: https://gitlab.example.com/team/common-lib.git
    branch: main

  - name: backend
    url: https://gitlab.example.com/services/backend.git
    branch: develop

  - name: frontend
    url: https://gitlab.example.com/ui/frontend.git
    branch: release/v1.0
```