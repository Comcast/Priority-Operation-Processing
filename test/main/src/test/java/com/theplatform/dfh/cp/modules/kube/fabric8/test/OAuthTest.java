package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.OAuthCredentialCapture;
import com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.DefaultConfigFactory;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User: kimberly.todd
 * Date: 8/24/18
 */
public class OAuthTest extends KubeClientTestBase
{

    private static Logger logger = LoggerFactory.getLogger(OAuthTest.class);

    @Test
    public void testName() throws Exception
    {
        KubeConfig kubeConfig = DefaultConfigFactory.getDefaultKubeConfig();
        OAuthCredentialCapture oauthCredentialCapture = new OAuthCredentialCapture().init();
        if(oauthCredentialCapture.isOAuthAvailable())
        {
            kubeConfig.setCaCertData(oauthCredentialCapture.getOauthCert());
            kubeConfig.setOauthToken(oauthCredentialCapture.getOauthToken());
        }

        Config config = Fabric8Helper.getFabric8Config(kubeConfig);

        if (oauthCredentialCapture.isOAuthAvailable())
        {
            DefaultKubernetesClient innerClient = new DefaultKubernetesClient(config);

            PodList list = innerClient.inNamespace(kubeConfig.getNameSpace()).pods().list();
            logger.debug("Pod list {}", list);
        }
        else
        {
            Assert.fail("This test requires " + OAuthCredentialCapture.OAUTH_CERT + " and " + OAuthCredentialCapture.OAUTH_TOKEN + " as environment variables." );
        }
    }
}
