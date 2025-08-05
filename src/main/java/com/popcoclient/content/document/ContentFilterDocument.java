package com.popcoclient.content.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(indexName = "contents_filter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentFilterDocument {

    @Id
    @JsonIgnore
    private String id;

    @Field(type = FieldType.Long)
    private Long contentId;

    @Field(type = FieldType.Keyword)
    private String contentType;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Keyword)
    private List<String> genres;

    @Field(type = FieldType.Keyword)
    private List<String> platforms;

    @Field(type = FieldType.Double)
    private BigDecimal ratingAverage;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate releaseDate;

    @Field(type = FieldType.Keyword)
    private String posterPath;

    @Field(type = FieldType.Float)
    private Float popularityScore;
}