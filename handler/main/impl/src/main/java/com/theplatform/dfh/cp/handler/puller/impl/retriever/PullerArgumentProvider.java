package com.theplatform.dfh.cp.handler.puller.impl.retriever;

import com.theplatform.dfh.cp.handler.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.field.retriever.argument.DefaultArgumentProvider;
import org.apache.commons.cli.Options;

/**
 * program arguments specific to the Puller
 */
public class PullerArgumentProvider extends DefaultArgumentProvider
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
