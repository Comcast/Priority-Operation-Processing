package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.source.TextResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileResourceByTypeMap
{
    private HashMap<String, List<FileResource>> fileResourceByType = new HashMap<>();

    public List<FileResource> getTextResources()
    {
        return fileResourceByType.get(FileResourceType.text.name());
    }

    public List<FileResource> getVideoResources()
    {
        return fileResourceByType.get(FileResourceType.video.name());
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
            FileResourceType enumType = FileResourceType.parse(source.getType());
            List<FileResource> fileReferencesForType = fileResourceByType.get(source.getType());
            if(fileReferencesForType == null)
            {
                fileReferencesForType = new ArrayList<>();
                fileResourceByType.put(source.getType(), fileReferencesForType);
            }
            if(enumType == FileResourceType.text)
            {
                fileReferencesForType.add(new TextResource(source));
            }
            else
            {
                fileReferencesForType.add(source);
            }
        }
    }
}
