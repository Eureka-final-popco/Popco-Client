package com.popcoclient.example.service;

import com.popcoclient.example.dto.response.ExampleResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExampleService {
    ExampleResponseDto getExample();
    ExampleResponseDto getException();
    List<ExampleResponseDto> getExampleList(Pageable pageable);
}
