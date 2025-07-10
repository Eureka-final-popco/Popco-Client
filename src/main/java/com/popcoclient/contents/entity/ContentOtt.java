package com.popcoclient.contents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContentOtt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentOttId;

    @ManyToOne
    @JoinColumn(name = "content_id")
    private Content content;

    @ManyToOne
    @JoinColumn(name = "ott_id")
    private Ott ott;
}
