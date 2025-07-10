package com.popcoclient.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "content_genre")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContentGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentGenreId;

    @ManyToOne
    @JoinColumn(name = "content_id")
    private Content content;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;
}
