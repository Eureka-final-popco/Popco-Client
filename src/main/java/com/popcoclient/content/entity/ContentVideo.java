package com.popcoclient.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentVideo {
    @Id
    private String id; // YouTube video ID

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "content_id",  referencedColumnName = "id",   nullable = false),
            @JoinColumn(name = "content_type", referencedColumnName = "type", nullable = false)
    })
    private Content content;

    private String name;

    @Column(name = "video_key")
    private String key;

    private String type; // Trailer, Teaser, Clip, etc.

    private Boolean official;

}
