/**
 * runCmd.groovy
 *
 * 跨平台命令封装，等价于 sh/bat，支持：
 * - 多行脚本
 * - 返回命令输出
 * - 忽略错误（不中断流水线）
 * - 自动判断平台（或强制指定）
 */

@groovy.transform.Field
def utils = new com.internal.sqa.Utils(this)

def call(Object arg) {
    Map config = [:]

    if (arg instanceof String) {
        return utils.runCmd([script: arg])
    } else if (arg instanceof Map) {
        return utils.runCmd(arg)
    } else {
        error "[sqaTools.RunCmd] 参数必须是 String 或 Map"
    }
}
