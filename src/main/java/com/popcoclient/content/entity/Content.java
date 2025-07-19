package com.popcoclient.content.entity;

import com.popcoclient.content.entity.key.ContentId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "contents")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"casts", "crews", "videos", "watchProviders"})
public class Content {

    @EmbeddedId
    private ContentId contentId;

    private String title;
    private String overview;
    private BigDecimal ratingAverage;
    private LocalDate releaseDate;
    private Integer ratingCount;
    private String backdropPath;
    private String posterPath;
    private String trailerPath;
    private Integer runtime;

    @ElementCollection
    @CollectionTable(
            name = "content_genres",
            joinColumns = {
                    @JoinColumn(name = "content_id",   referencedColumnName = "id"),
                    @JoinColumn(name = "type", referencedColumnName = "type")
            }
    )
    @Column(name = "genre_id")
    private Set<Integer> genreIds = new HashSet<>();

    // 관계 매핑
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CastMember> casts = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Crew> crews = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentVideo> videos = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WatchProvider> watchProviders = new ArrayList<>();
}
