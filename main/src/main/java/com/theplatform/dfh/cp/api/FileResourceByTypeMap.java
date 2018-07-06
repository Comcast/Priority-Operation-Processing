package com.theplatform.dfh.cp.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileResourceByTypeMap
{
    private HashMap<String, List<FileResource>> fileResourceByType = new HashMap<>();

    public List<FileResource> getTextResources()
    {
        return fileResourceByType.get(FileResourceType.TEXT.name().toLowerCase());
    }

    public List<FileResource> getVideoResources()
    {
        return fileResourceByType.get(FileResourceType.VIDEO.name().toLowerCase());
    }

    public FileResourceByTypeMap(List<? extends FileResource> resources)
    {
        mapStrictSourceReferences(resources);
    }

    private void mapStrictSourceReferences(List<? extends FileResource> resources)
    {
        if (resources == null) return;

        for (int index = 0; index < resources.size(); index++)
        {
            FileResource source = resources.get(index);
            source.setIndex(index);
            List<FileResource> fileReferencesForType = fileResourceByType.get(source.getType());
            if(fileReferencesForType == null)
            {
                fileReferencesForType = new ArrayList<>();
                fileResourceByType.put(source.getType(), fileReferencesForType);
            }
            fileReferencesForType.add(source);
        }
    }
}
