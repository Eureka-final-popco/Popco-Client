package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.WatchProvider;
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
public class WatchProviderDto {
    private Integer providerId;
    private String name;
    private String link;
    private String logoPath;

    public static List<WatchProviderDto> from(List<WatchProvider> watchProviders) {
        return watchProviders.stream()
                .map(wp -> WatchProviderDto.builder()
                        .providerId(wp.getProvider().getId())
                        .name(wp.getProvider().getName())
                        .link(wp.getProvider().getLink())
                        .logoPath(wp.getProvider().getLogoPath())
                        .build())
                .collect(Collectors.toList());
    }
}
