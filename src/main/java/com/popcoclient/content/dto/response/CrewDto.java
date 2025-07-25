package com.popcoclient.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}