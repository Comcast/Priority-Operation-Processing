package com.theplatform.dfh.cp.api.output;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The Output Streams part describes the needed transformations to create the streams that will
 * be assembled and packaged in the Outputs section. Each Source stream will be either left alone
 * (if it’s going to be muxed into the Outputs) or used to create 1 or more derivative streams
 * (at different bitrates, resolutions, etc.)
 * Unlike source streams, each output stream is likely to correspond to a temporary file on the file system,
 * through demuxing video or audio from sources, or transcoding video or audio to produce a new intermediate file.
 * Each output stream defines the source stream that it is derived from and all of the transcoding
 * settings used to produce the new stream. These transcoding settings will vary, based on what codec
 * is used to in producing this output stream.
 */
public class OutputStreams
{
    List<Video> videos;
    List<Audio> audios;

    @JsonProperty
    public List<Video> getVideo()
    {
        return videos;
    }

    @JsonProperty
    public void setVideo(List<Video> videos)
    {
        this.videos = videos;
    }

    @JsonProperty
    public List<Audio> getAudio()
    {
        return audios;
    }

    @JsonProperty
    public void setAudio(List<Audio> audios)
    {
        this.audios = audios;
    }
}
