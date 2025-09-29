package org.example.tasktracker.api.converter;

import org.example.tasktracker.api.dto.TaskDto;
import org.example.tasktracker.store.entity.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoConverter {

    public TaskDto makeTaskDto(TaskEntity entity) {

        return TaskDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .created(entity.getCreated())
                .description(entity.getDescription())
                .build();
    }

}
