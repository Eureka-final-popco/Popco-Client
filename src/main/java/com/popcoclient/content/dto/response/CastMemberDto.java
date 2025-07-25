package com.popcoclient.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CastMemberDto {
    private Long actorId;
    private String actorName;
    private String profilePath;
    private String characterName;
    private Integer castOrder;
}