package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.argument.DefaultArgumentProvider;
import org.apache.commons.cli.Options;

public class KubernetesArgumentProvider extends DefaultArgumentProvider
{
    public KubernetesArgumentProvider(String[] args)
    {
        super(args);
    }

    @Override
    protected Options getOptions()
    {
        Options options = super.getOptions();

        addOption(options, KubeConfigArgument.OAUTH_CERT_FILE_PATH.getFieldName(), true, "File path to the kubernetes cert file.");
        addOption(options, KubeConfigArgument.OAUTH_TOKEN_FILE_PATH.getFieldName(), true, "File path to the kubernetes token file.");

        return options;
    }
}
