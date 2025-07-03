package com.internal.sqa
import groovy.json.JsonSlurperClassic

class CustomPipelineCfg implements Serializable {

    def steps  // Pipeline binding context

    CustomPipelineCfg(steps) {
        this.steps = steps
    }

    /**
     * 读取默认凭证
     */
    List getDefaultCredentialsFromResource() {
        def raw = steps.libraryResource('config/default-credentials.json')
        def list = new groovy.json.JsonSlurperClassic().parseText(raw)

        return list.collect { cred ->
            switch (cred.type) {
                case 'usernamePassword':
                    return steps.usernamePassword(
                        credentialsId: cred.credentialsId,
                        usernameVariable: cred.usernameVariable,
                        passwordVariable: cred.passwordVariable
                    )
                case 'string':
                    return steps.string(
                        credentialsId: cred.credentialsId,
                        variable: cred.variable
                    )
                case 'file':
                    return steps.file(
                        credentialsId: cred.credentialsId,
                        variable: cred.variable
                    )
                case 'sshUserPrivateKey':
                    return steps.sshUserPrivateKey(
                        credentialsId: cred.credentialsId,
                        keyFileVariable: cred.keyFileVariable,
                        passphraseVariable: cred.passphraseVariable,
                        usernameVariable: cred.usernameVariable
                    )
                default:
                    error "[cicd] ❌ 不支持的凭证类型: ${cred.type}"
            }
        }
    }

    /**
     * 设置流水线属性
     * 目前支持：
     * - parameters: 参数列表
     * - triggers: 触发器列表（cron、pollSCM、upstream
     */
    static void applyJobProperties(Map config = [:]) {
        def props = []

        if (config.triggers instanceof List && config.triggers) {
            def triggerList = config.triggers.collect { trig ->
                switch (trig.type) {
                    case 'cron': return cron(trig.spec)
                    case 'pollSCM': return pollSCM(trig.spec)
                    case 'upstream': return upstream(trig.job)
                    default:
                        error "[cicd] 不支持的 trigger 类型: ${trig.type}"
                }
            }
            props << pipelineTriggers(triggerList)
        }

        if (config.parameters instanceof List && config.parameters) {
            props << parameters(config.parameters)
        }

        if (props) {
            properties(props)
            echo "[cicd] 注入 Job 属性：parameters=${config.parameters?.size() ?: 0} triggers=${config.triggers?.size() ?: 0}"
        }
    }

}