package com.popcoclient.content.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class FilterRequestDto {
    private String contentType;
    private List<String> genres;
    private BigDecimal minRating;
    private BigDecimal maxRating;
    private List<String> platforms;
    private Integer minReleaseYear;
    private Integer maxReleaseYear;
    private Map<String, Object> ageGroupFilter;
    private Map<String, Object> personaFilter;
    private Map<String, Object> popcorithmFilter;

}
