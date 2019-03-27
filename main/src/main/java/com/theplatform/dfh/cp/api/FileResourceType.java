package com.theplatform.dfh.cp.api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FileResourceType
{
    // NOTE: all labels should be lowercase (see constructor)

    MEZZANINE("mezzanine"),
    TEXT_TRACK_SIDECAR("text track sidecar"),
    AUDIO_TRACK("audio track"),
    VIDEO_TRACK("video_track"),
    AUDIO("audio"),
    VIDEO("video"),
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
        this.label = label.toLowerCase();
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

    /**
     * Checks if the label of the enum is equal to the input string (case insensitive)
     * @param typeString The string to check
     * @return True if matching, false otherwise
     */
    public boolean labelEquals(String typeString)
    {
        if(typeString == null) return false;
        return label.equals(typeString.toLowerCase());
    }

    /**
     * Checks if the label of the enum is present in the input string (case insensitive)
     * @param typeString The string to check
     * @return True if contained, false otherwise
     */
    public boolean containedIn(String typeString)
    {
        if(typeString == null) return false;
        return typeString.toLowerCase().contains(label);
    }
}
