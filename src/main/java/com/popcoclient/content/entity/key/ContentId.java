package com.popcoclient.content.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentId implements Serializable {

    @Column(name = "id")
    private Long id;

    @Column(name = "type")
    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // 성능 최적화
        if (!(o instanceof ContentId contentId)) return false;
        return Objects.equals(id, contentId.id)
                && Objects.equals(normalize(type), normalize(contentId.type));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, normalize(type));
    }

    private String normalize(String s) {
        return s == null ? null : s.toLowerCase();
    }
}

