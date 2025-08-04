package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.Content;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ContentDto {
    private Long id;
    private String type;
    private String title;
    private LocalDate releaseDate;
    private String posterPath;

    private Boolean userLiked;
    private Boolean userDisliked;

    public static ContentDto from(Content content) {
        return ContentDto.builder()
                .id(content.getContentId().getId())
                .type(content.getContentId().getType())
                .title(content.getTitle())
                .releaseDate(content.getReleaseDate())
                .posterPath(content.getPosterPath())
                .userLiked(false)
                .userDisliked(false)
                .build();
    }

   public ContentDto withUserReaction(Boolean userLiked, Boolean userDisliked) {
        return ContentDto.builder()
                .id(this.id)
                .type(this.type)
                .title(this.title)
                .releaseDate(this.releaseDate)
                .posterPath(this.posterPath)
                .userLiked(userLiked)
                .userDisliked(userDisliked)
                .build();
   }
}