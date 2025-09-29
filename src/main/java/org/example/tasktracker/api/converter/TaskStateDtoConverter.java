package org.example.tasktracker.api.converter;

import org.example.tasktracker.api.dto.TaskStateDto;
import org.example.tasktracker.store.entity.TaskStateEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskStateDtoConverter {

    public TaskStateDto makeTaskStateDto(TaskStateEntity entity) {

        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .created(entity.getCreated())
                .ordinal(entity.getOrdinal())
                .build();
    }
}
