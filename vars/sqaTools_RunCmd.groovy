/**
 * sqaTools_RunCmd.groovy
 *
 * 跨平台命令封装，等价于 sh/bat，支持：
 * - 多行脚本
 * - 返回命令输出
 * - 忽略错误（不中断流水线）
 * - 自动判断平台（或强制指定）
 */

def call(Object arg) {
    Map config = [:]

    if (arg instanceof String) {
        config.script = arg
    } else if (arg instanceof Map) {
        config = arg
    } else {
        error "[RunCmd] ❌ 参数必须是 String 或 Map"
    }

    if (!config.script) {
        error "[RunCmd] ❌ 缺少必填参数 script"
    }

    def returnStdout = config.get('returnStdout', false)
    def ignoreError  = config.get('ignoreError', false)
    def isUnix       = config.containsKey('isUnix') ? config.isUnix : isUnix()

    try {
        if (isUnix) {
            return returnStdout ?
                sh(script: config.script, returnStdout: true).trim() :
                sh(script: config.script)
        } else {
            return returnStdout ?
                bat(script: config.script, returnStdout: true).trim() :
                bat(script: config.script)
        }
    } catch (err) {
        if (ignoreError) {
            echo "[RunCmd] 命令执行失败，忽略错误：${err.getMessage()}"
            return returnStdout ? '' : null
        } else {
            throw err
        }
    }
}
