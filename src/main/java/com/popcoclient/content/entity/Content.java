package com.popcoclient.content.entity;

import com.popcoclient.content.entity.enums.ContentTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "content")
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
