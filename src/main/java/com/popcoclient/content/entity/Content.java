package com.popcoclient.content.entity;

import com.popcoclient.content.entity.enums.ContentTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "content")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @Column(name = "content_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    private String title;
    private String overview;
    private BigDecimal ratingAverage;
    private LocalDateTime releaseDate;
    private Integer ratingCount;
    private String backdropPath;

    @Enumerated(EnumType.STRING)
    private ContentTypes type;
    private String posterPath;
    private String trailerPath;
    private Integer runtime;

}
