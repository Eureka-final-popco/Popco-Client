package com.popcoclient.collection.dto.response;

import com.popcoclient.collection.dto.ContentPosterDto;
import com.popcoclient.collection.entity.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionResponseDto {

    private Long collectionId;
    private Long userId;
    private String userNickname;
    private String title;
    private String description;
    private Integer saveCount;
    private Integer contentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ContentPosterDto> contentPosters = new ArrayList<>();

    @Builder.Default
    private Boolean isMarked = false;

    public static CollectionResponseDto from(Collection collection) {
        return CollectionResponseDto.builder()
                .collectionId(collection.getCollectionId())
                .userId(collection.getUser().getUserId())
                .userNickname(collection.getUser().getNickname())
                .title(collection.getTitle())
                .description(collection.getDescription())
                .saveCount(collection.getSaveCount())
                .contentCount(collection.getContentCount())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .contentPosters(new ArrayList<>())
                .isMarked(false)
                .build();
    }

    public static CollectionResponseDto from(Collection collection, List<ContentPosterDto> posters) {
        return CollectionResponseDto.builder()
                .collectionId(collection.getCollectionId())
                .userId(collection.getUser().getUserId())
                .userNickname(collection.getUser().getNickname())
                .title(collection.getTitle())
                .description(collection.getDescription())
                .saveCount(collection.getSaveCount())
                .contentCount(collection.getContentCount())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .contentPosters(posters != null ? posters : new ArrayList<>())
                .isMarked(false)
                .build();
    }

    public static CollectionResponseDto from(Collection collection, List<ContentPosterDto> posters, boolean isMarked) {
        return CollectionResponseDto.builder()
                .collectionId(collection.getCollectionId())
                .userId(collection.getUser().getUserId())
                .userNickname(collection.getUser().getNickname())
                .title(collection.getTitle())
                .description(collection.getDescription())
                .saveCount(collection.getSaveCount())
                .contentCount(collection.getContentCount())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .contentPosters(posters != null ? posters : new ArrayList<>())
                .isMarked(isMarked)
                .build();
    }
}
