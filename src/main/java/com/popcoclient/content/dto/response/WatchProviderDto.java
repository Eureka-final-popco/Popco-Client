package com.popcoclient.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchProviderDto {
    private Integer providerId;
    private String name;
    private String link;
    private String logoPath;
}
