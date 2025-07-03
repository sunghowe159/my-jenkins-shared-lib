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
