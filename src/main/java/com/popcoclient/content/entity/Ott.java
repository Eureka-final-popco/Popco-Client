package com.popcoclient.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "ott")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Ott {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ottId;

    private String name;
    private String link;
    private String profileImage;
}
