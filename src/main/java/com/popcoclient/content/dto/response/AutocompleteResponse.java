package com.popcoclient.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutocompleteResponse {
    private String value;
    private String type; // "content" or "actor"
    private Long contentId;
    private String contentType; // "movie" or "tv"
}
