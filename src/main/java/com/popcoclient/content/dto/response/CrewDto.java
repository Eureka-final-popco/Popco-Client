package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.Crew;
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
public class CrewDto {
    private Long crewMemberId;
    private String name;
    private String profilePath;
    private String job;
    private String knownForDepartment;

    public static List<CrewDto> from(List<Crew> crews) {
        return crews.stream()
                .map(crew -> CrewDto.builder()
                        .crewMemberId(crew.getCrewMember().getId())
                        .name(crew.getCrewMember().getName())
                        .profilePath(crew.getCrewMember().getProfilePath())
                        .job(crew.getJob())
                        .knownForDepartment(crew.getCrewMember().getKnownForDepartment())
                        .build())
                .collect(Collectors.toList());
    }
}