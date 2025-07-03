package vars
import org.junit.Before
import org.junit.Test

class CustomPipelineTest extends MyBasePipelineTest {
    def customPipeline

    @Override
    @Before
    void setUp() {
        super.setUp()
        helper.registerAllowedMethod('node', [String, Closure], { label, body ->
            println "MOCK node: ${label}"
            body()
        })
        customPipeline = loadScript("vars/customPipeline.groovy")
    }

    @Test
    void testCustomPipelineTemplate() {
        customPipeline.run([nodeLabel: 'build-node']) {
            stage('Build') {
                sh 'make'
            }
            stage('Test') {
                sh 'make test'
            }
        }
        printCallStack()
    }
}
