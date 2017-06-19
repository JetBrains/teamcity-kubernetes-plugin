package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import jetbrains.buildServer.BaseTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 17.06.17.
 */
public class CustomTemplatePodTemplateProviderTest extends BaseTestCase {
    private PodTemplateProvider myPodTemplateProvider;

    @BeforeMethod
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myPodTemplateProvider = new CustomTemplatePodTemplateProvider();
    }

    @Test(enabled = false)
    public void testGetPodTemplate() throws Exception {
        assertNotNull(myPodTemplateProvider.getPodTemplate(null, null, null));
    }
}