# Jenkins Job Trigger CLI
该脚本用于触发 Jenkins Job，并实时监控其状态，支持带参触发。
## 快速开始
```shell
bash
./build_pipeline.sh \
  --jenkins-url http://jenkins.example.com \
  --job-path Folder/MyJob \
  --user admin \
  --token your_token_here \
  --param GERRIT_BRANCH dev \
  --param BUILD_ENV staging \
  --sleep-interval 3
```
## 参数说明
|参数|	简写|	描述|
|--|--|--|
|--jenkins-url <url>|	-url	|Jenkins 服务器 URL|
|--job-path <path>	|-path	|Job 绝对路径，例：Folder/MyJob|
|--user <user>	|-u	|Jenkins 用户名
|--token <token>|	-t	|Jenkins API Token 或密码
|--param <k> <v>	|-p	|指定参数，可重复添加
|--sleep-interval <sec>	|-s	|轮询间隔时间，默认 5 秒
|--query	|无	|不触发构建，用于查询指定build number的流水线状态，默认查询最新一次已完成的构建
|--help	|无	|查看帮助文档

## 示例
传递多个参数
```shell
./jenkins_trigger.sh -url http://jenkins:8080 -path Folder/MyJob \
-u admin -t xxxx \
-p GERRIT_BRANCH="main" -p BUILD_TYPE="debug" \
-s 2
```
只打印帮助
```shell
./jenkins_trigger.sh --help
```

## 特性
- 自动获取 CSRF crumb

- 支持多参数

- 支持 Ctrl+C 安全终止远程构建

- 使用退出码 0 / 1 清晰区分成功失败