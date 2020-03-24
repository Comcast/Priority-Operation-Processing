package com.theplatform.dfh.cp.handler.puller.impl.retriever;

import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubernetesArgumentProvider;
import org.apache.commons.cli.Options;

/**
 * program arguments specific to the Puller
 */
public class PullerArgumentProvider extends KubernetesArgumentProvider
{

    public final static String CONF_PATH = "confPath";

    public PullerArgumentProvider(String[] args)
    {
        super(args);
    }

    // add argument to override the file path of conf.yaml used to configure drop wizard
    @Override
    protected Options getOptions()
    {
        Options options = super.getOptions();        

        addOption(options, CONF_PATH, true, "Indicator of how this handler was launched.");
        return options;
    }


}
