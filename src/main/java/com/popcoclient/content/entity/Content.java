package com.popcoclient.content.entity;

import com.popcoclient.content.entity.enums.ContentTypes;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    private String title;
    private String overview;
    private Float rating_average;
    private LocalDateTime releaseDate;
    private Integer ratingCount;
    private String backdropPath;
    private ContentTypes type;
    private String posterPath;
    private String trailerPath;
    private Integer runtime;

}
