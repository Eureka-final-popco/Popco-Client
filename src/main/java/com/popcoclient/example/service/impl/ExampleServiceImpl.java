package com.popcoclient.example.service.impl;

import com.popcoclient.example.dto.response.ExampleResponseDto;
import com.popcoclient.example.entity.Example;
import com.popcoclient.example.repository.ExampleRepository;
import com.popcoclient.example.service.ExampleService;
import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExampleServiceImpl implements ExampleService {
    private final ExampleRepository exampleRepository;

//    @Transactional 필요하다면 작성
    @Override
    public ExampleResponseDto getExample() { // 파라미터로 id 받아와서
//        Example example = exampleRepository.findById(id); // 실제 로직
        Example example = Example.builder().id(1L).build(); // 테스트를 위한 하드 코딩, builder 에서 createdAt, updatedAt 은 자동

        return ExampleResponseDto.from(example, "성공 응답 예시");
    }

    @Override
    public ExampleResponseDto getException() {
        Example example = exampleRepository.findById(404L)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 테스트용 예외 메세지

        return ExampleResponseDto.from(example, "실패 응답 예시");
    }

    @Override
    public List<ExampleResponseDto> getExampleList(Pageable pageable) {
        Page<Example> page = exampleRepository.findAll(pageable);

        return page.getContent().stream()
                .map(example -> {
                    Long id = example.getId();
                    String description = "리스트 응답 예시";
                    return ExampleResponseDto.from(example, description);
                })
                .collect(Collectors.toList());
    }
}
