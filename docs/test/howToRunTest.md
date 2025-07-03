# Jenkins Shared Library 单元测试指南
## 1. 简介
为保障质量与快速迭代，使用 JenkinsPipelineUnit 对库进行本地单元测试，模拟 Jenkins 流水线环境。

## 2. 依赖环境
- JDK 8+
- Groovy (由 gradle 自动管理)
- Gradle (推荐使用自带 wrapper ./gradlew)

## 3. 如何运行单元测试
在项目根目录下执行：
```bash
./gradlew tests
```
会自动扫描 tests/ 下所有 Test 文件并执行。

输出将展示所有模拟的 node、stage、sh、withCredentials 等步骤。

## 3 测试写法规范
### 3.1 所有测试继承 MyBasePipelineTest
```groovy
class CustomPipelineTest extends MyBasePipelineTest {
    ...
}
```
自动加载 commonMocks，无需每次重复注册 node, stage, sh 等。

### 3.2 示例：测试 vars/ 中的模板方法
示例 tests/vars/CustomPipelineTest.groovy：

```groovy
package vars

class CustomPipelineTest extends MyBasePipelineTest {
    def customPipeline

    @Override
    void setUp() {
        super.setUp()
        customPipeline = loadScript("vars/customPipeline.groovy")
    }

    void testCustomPipelineTemplate() {
        customPipeline.run([nodeLabel: 'build-node']) {
            stage('Build') {
                sh 'make'
            }
            stage('Test') {
                sh 'make test'
            }
        }
    }
}
```
### 3.2 示例：测试 src/ 下的工具类
例如 Utils：

```groovy
package com.internal.sqa

class UtilsTest extends MyBasePipelineTest {
    def utils = new Utils()

    void testRunCmd() {
        utils.runCmd("echo Hello", [strict: true])
    }
}
```

### 3.4 如何扩展新的 pipeline step mock
在 test/commonMocks.groovy 中集中管理，示例：

```groovy
helper.registerAllowedMethod('docker.image', [String], { img ->
    return [inside: { body -> 
        println "[MOCK docker.image(${img}).inside]"
        body()
    }]
})
```
这样所有测试都会自动继承。