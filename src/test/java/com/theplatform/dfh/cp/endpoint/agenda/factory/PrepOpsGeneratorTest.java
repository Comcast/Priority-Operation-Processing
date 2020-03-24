package com.theplatform.dfh.cp.endpoint.agenda.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.endpoint.agenda.factory.PrepOpsGenerator;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

public class PrepOpsGeneratorTest
{
    private PrepOpsGenerator prepOpsGenerator = new PrepOpsGenerator();

    @Test
    public void testGenerateAgenda()
    {
        TransformRequest transformRequest = new TransformRequest();
        InputFileResource inputFileResource = new InputFileResource();
        inputFileResource.setUrl("/the/file/path/video.mp4");
        transformRequest.setInputs(Collections.singletonList(inputFileResource));
        Agenda agenda = prepOpsGenerator.generateAgenda(transformRequest);
        Assert.assertNotNull(agenda);
        System.out.println(new JsonHelper().getPrettyJSONString(agenda));
        // TODO: this test is way too light on validation (just proving construction 'looks' right)
    }
}
