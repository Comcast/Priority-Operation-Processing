package com.theplatform.dfh.processor.api.source;

import java.util.List;

/**
 * Source Streams Part
 * The Source Streams part decomposes the sources into the individual streams of content that will be transcoded and packaged in the subsequent parts.
 * In many cases, we know what streams our sources will contain, and then we explicitly define source streams for them.
 * In other cases, our customers may have variability in how provide they media content, and we’ll rely on media analysis to determine what streams to create.
 * For example, a customer may provide videos with one or more audio tracks (each with a different audio language).
 * Source Streams are conceptual, meaning that describe what will be used as a transcoding/packaging source,
 * but there will not typically be a file on the file system that embodies this stream.
 * The Source Streams are grouped into the same categories as the Sources (Video, Audio, Text, Metadata).
 */
public class SourceStreams
{
    private List<SourceStream> video;
    private List<SourceStream> audio;

    public List<SourceStream> getVideo()
    {
        return video;
    }

    public void setVideo(List<SourceStream> video)
    {
        this.video = video;
    }

    public List<SourceStream> getAudio()
    {
        return audio;
    }

    public void setAudio(List<SourceStream> audio)
    {
        this.audio = audio;
    }
}
