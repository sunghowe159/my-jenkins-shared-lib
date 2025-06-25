# `runCmd` - 跨平台命令执行封装

封装 Jenkins 原生 `sh` / `bat`，可直接传入字符串或参数 Map，支持多行命令、返回结果等。

---

## 基本用法

```groovy
runCmd('echo "Hello Jenkins"')
```
### 支持多行语句
```groovy
runCmd('''
echo "Step 1"
echo "Step 2"
pwd
''')
```

### 参数说明
| 参数名          | 类型      | 说明                        |
| ------------ | ------- | ------------------------- |
| script       | String  | 要执行的命令文本，支持多行（必填）         |
| returnStdout | Boolean | 是否返回命令结果，默认 `false`       |
| ignoreError  | Boolean | 是否忽略错误不中断流水线，默认 `false`   |
| isUnix       | Boolean | 强制指定平台（默认自动使用 `isUnix()`） |

##  示例
### 获取输出结果
```groovy
def version = runCmd([
  script: 'python --version',
  returnStdout: true
])
echo "当前 Python 版本：${version}"
```
###  忽略命令失败不中断
默认情况下命令失败将终止流水线。通过 ignoreError: true 来让构建继续执行
```groovy
runCmd([
  script: 'exit 1',
  ignoreError: true
])
```
###  Windows 平台示例
```groovy
runCmd([
  script: '''
    echo "Windows 上执行命令"
    dir
  ''',
  isUnix: false
])
```