package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.ContentVideo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private String id;
    private String name;
    private String key;
    private String type;
    private Boolean official;

    public static List<VideoDto> from(List<ContentVideo> videos) {
        return videos.stream()
                .map(video -> VideoDto.builder()
                        .id(video.getId())
                        .name(video.getName())
                        .key(video.getKey())
                        .type(video.getType())
                        .official(video.getOfficial())
                        .build())
                .collect(Collectors.toList());
    }
}