package com.popcoclient.content.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Table(name = "providers")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"watchProviders"})
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String link;
    private String logoPath;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WatchProvider> watchProviders = new ArrayList<>();
}
