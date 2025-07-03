// test/MyBasePipelineTest.groovy
import com.lesfurets.jenkins.unit.BasePipelineTest

class MyBasePipelineTest extends BasePipelineTest {
    @Override
    void setUp() {
        super.setUp()
        def common = new GroovyShell().parse(new File("test/commonMocks.groovy"))
        common.registerCommonPipelineMocks(helper)
    }
}
