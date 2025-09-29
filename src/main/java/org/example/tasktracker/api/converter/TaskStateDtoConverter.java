package org.example.tasktracker.api.converter;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.dto.TaskStateDto;
import org.example.tasktracker.store.entity.TaskStateEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskStateDtoConverter {

    private final TaskDtoConverter taskDtoConverter;

    public TaskStateDto makeTaskStateDto(TaskStateEntity entity) {

        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .created(entity.getCreated())
                .leftTaskStateId(entity.getLeftTaskState().map(TaskStateEntity::getId).orElse(null))
                .rightTaskStateId(entity.getRightTaskState().map(TaskStateEntity::getId).orElse(null))
                .tasks(entity.getTasks().stream()
                        .map(taskDtoConverter::makeTaskDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
