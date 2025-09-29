package org.example.tasktracker.api.converter;

import org.example.tasktracker.api.dto.ProjectDto;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectDtoConverter {

    public ProjectDto makeProjectDto(ProjectEntity entity) {

        return ProjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
