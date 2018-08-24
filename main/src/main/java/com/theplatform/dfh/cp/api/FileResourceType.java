package com.theplatform.dfh.cp.api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FileResourceType
{
    MEZZANINE("mezzanine"),
    TEXT_TRACK_SIDECAR("text track sidecar"),
    AUDIO_TRACK("audio track"),
    VIDEO_TRACK("video_track"),
    IMAGE("image"),
    MP4("mp4"),
    HLS("hls"),
    DASH("dash"),
    SMOOTH("smooth"),
    FILMSTRIP("filmstrip"),
    UNKNOWN("unknown");

    private String label;
    FileResourceType(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
    public static FileResourceType parse(String label)
    {
        if(label == null)
            return FileResourceType.UNKNOWN;
        List<FileResourceType> fileResourceTypeList =
            Stream.of(FileResourceType.values())
            .filter(type -> type.getLabel().equals(label))
            .collect(Collectors.toList());
        return fileResourceTypeList != null ? fileResourceTypeList.get(0) : FileResourceType.UNKNOWN;
    }
}
