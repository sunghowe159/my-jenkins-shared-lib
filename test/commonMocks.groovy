// test/commonMocks.groovy
void registerCommonPipelineMocks(helper) {
    helper.registerAllowedMethod('node', [String, Closure], { label, body ->
        println "[MOCK node(${label})]"
        body()
    })
    helper.registerAllowedMethod('stage', [String, Closure], { name, body ->
        println "[MOCK stage(${name})]"
        body()
    })
    helper.registerAllowedMethod('withCredentials', [List, Closure], { creds, body ->
        println "[MOCK withCredentials]"
        body()
    })
    helper.registerAllowedMethod('sh', [Map], { m ->
        println "[MOCK sh: ${m.script}]"
        return m.returnStatus ? 0 : "output"
    })
    helper.registerAllowedMethod('sh', [String], { s ->
        println "[MOCK sh: ${s}]"
        return 0
    })
    helper.registerAllowedMethod('echo', [String], { msg -> println "[MOCK echo: ${msg}]" })
    helper.registerAllowedMethod('archiveArtifacts', [Map], { m -> println "[MOCK archiveArtifacts: ${m.artifacts}]" })
    helper.registerAllowedMethod('error', [String], { msg -> throw new Exception(msg) })
    helper.registerAllowedMethod('catchError', [Map, Closure], { m, body ->
        try { body() } catch (e) {
            println "[MOCK catchError] Caught: ${e.message}"
        }
    })
    helper.registerAllowedMethod('isUnix', [], { true })
    helper.registerAllowedMethod('readJSON', [Map], { m -> 
        println "[MOCK readJSON: ${m.file}]"
        return [:]
    })
}
