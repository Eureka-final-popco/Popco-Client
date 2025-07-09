package com.popcoclient.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "content")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;

    @Column(name = "rating_average", precision = 3, scale = 2)
    private BigDecimal ratingAverage;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "trailer_path")
    private String trailerPath;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Content(String title, String overview, BigDecimal ratingAverage, LocalDate releaseDate,
                    String backdropPath, String type, String posterPath, String trailerPath, Integer runtime) {
        this.title = title;
        this.overview = overview;
        this.ratingAverage = ratingAverage;
        this.releaseDate = releaseDate;
        this.backdropPath = backdropPath;
        this.type = type;
        this.posterPath = posterPath;
        this.trailerPath = trailerPath;
        this.runtime = runtime;
        this.ratingCount = 0;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
