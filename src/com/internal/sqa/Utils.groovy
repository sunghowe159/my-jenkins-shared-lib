package com.internal.sqa

class Utils implements Serializable {

    def steps  // Pipeline binding context

    Utils(steps) {
        this.steps = steps
    }

    /**
     * 跨平台命令执行函数（封装 sh / bat）
     */
    def runCmd(Map config) {
        if (!config.script) {
            steps.error "[Utils.runCmd] 缺少必填参数 script"
        }

        def returnStdout = config.get('returnStdout', false)
        def ignoreError  = config.get('ignoreError', false)
        def isUnix       = config.containsKey('isUnix') ? config.isUnix : steps.isUnix()

        try {
            if (isUnix) {
                return returnStdout ?
                    steps.sh(script: config.script, returnStdout: true).trim() :
                    steps.sh(script: config.script)
            } else {
                return returnStdout ?
                    steps.bat(script: config.script, returnStdout: true).trim() :
                    steps.bat(script: config.script)
            }
        } catch (err) {
            if (ignoreError) {
                steps.echo "[Utils.runCmd] 命令执行失败，但忽略错误：${err.getMessage()}"
                return returnStdout ? '' : null
            } else {
                throw err
            }
        }
    }
}
