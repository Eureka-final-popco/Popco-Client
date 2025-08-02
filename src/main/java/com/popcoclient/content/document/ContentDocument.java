package com.popcoclient.content.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(indexName = "contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String overview;

    @Field(type = FieldType.Keyword)
    private String contentType;

    @Field(type = FieldType.Long)
    private Long contentId;

    @Field(type = FieldType.Double)
    private BigDecimal ratingAverage;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate releaseDate;

    @Field(type = FieldType.Keyword)
    private String posterPath;

    @Field(type = FieldType.Nested)
    private List<CastInfo> cast;

    @Field(type = FieldType.Nested)
    private List<CrewInfo> crew;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CastInfo {
        @Field(type = FieldType.Text)
        private String actorName;

        @Field(type = FieldType.Text)
        private String characterName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CrewInfo {
        @Field(type = FieldType.Text)
        private String name;

        @Field(type = FieldType.Keyword)
        private String job;
    }
}