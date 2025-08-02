package com.popcoclient.content.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PopularContentListApiResponseDto {
    private String message;
    private List<PopularContentResponseDto> recommendations;
    @JsonProperty("group_info")
    private String groupInfo;
    @JsonProperty("total_count")
    private int totalCount;
}
