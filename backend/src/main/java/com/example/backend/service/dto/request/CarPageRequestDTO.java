package com.example.backend.service.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;

import java.util.Objects;

@Getter
@Setter
public class CarPageRequestDTO {
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    @Enumerated(EnumType.STRING)
    private Sort.Direction sort = Sort.Direction.ASC;

    private String sortByColumn = "id";

    public Pageable getPageable(CarPageRequestDTO dto){
        Integer page = Objects.nonNull(dto.getPageNo()) ? dto.getPageNo() : this.pageNo;
        Integer size = Objects.nonNull(dto.getPageSize()) ? dto.getPageSize() : this.pageSize;
        Sort.Direction sort = Objects.nonNull(dto.getSort()) ? dto.getSort() : this.sort;
        String sortByColumn = Objects.nonNull(dto.getSortByColumn()) ? dto.getSortByColumn() : this.sortByColumn;
        try
        {
            return PageRequest.of(page, size, sort, sortByColumn);
        }
        catch (PropertyReferenceException e)
        {
            throw new RuntimeException("Invalid sort column: " + sortByColumn, e);
        }
    }
}
