package com.popcoclient.persona.entity;

import com.popcoclient.persona.entity.enums.PersonaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Table(name = "personas")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Persona {
    @Id
    @Column(name = "persona_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personaId;
    private String name;
    private String description;
    private String tag;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL)
    private List<PersonaGenre> personaGenre;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL)
    private List<PersonaDetail> personaDetail;

    public String getMainDescription() {
        if (description == null) return "";
        int delimiterIndex = description.indexOf("|");
        return delimiterIndex != -1
                ? description.substring(0, delimiterIndex).trim()
                : description.trim();
    }

    public String getBabyImgPath() {
        return personaDetail.stream()
                .filter(detail -> detail.getPersonaType() == PersonaType.BABY)
                .map(PersonaDetail::getImgPath)
                .filter(Objects::nonNull) // null이 아닌 이미지 경로만 필터링
                .findFirst()
                .orElse(""); // 최종적으로 값이 없으면 빈 문자열 반환
    }

    public String getAdultImgPath() {
        return personaDetail.stream()
                .filter(detail -> detail.getPersonaType() == PersonaType.ADULT)
                .map(PersonaDetail::getImgPath)
                .filter(Objects::nonNull) // null이 아닌 이미지 경로만 필터링
                .findFirst()
                .orElse(""); // 최종적으로 값이 없으면 빈 문자열 반환
    }

}
