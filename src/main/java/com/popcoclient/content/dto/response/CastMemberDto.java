package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.CastMember;
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
public class CastMemberDto {
    private Long actorId;
    private String actorName;
    private String profilePath;
    private String characterName;
    private Integer castOrder;

    public static List<CastMemberDto> from(List<CastMember> casts) {
        return casts.stream()
                .map(cast -> CastMemberDto.builder()
                        .actorId(cast.getActor().getId())
                        .actorName(cast.getActor().getName())
                        .profilePath(cast.getActor().getProfilePath())
                        .characterName(cast.getCharacterName())
                        .castOrder(cast.getCastOrder())
                        .build())
                .sorted((a, b) -> Integer.compare(a.getCastOrder(), b.getCastOrder()))
                .collect(Collectors.toList());
    }
}