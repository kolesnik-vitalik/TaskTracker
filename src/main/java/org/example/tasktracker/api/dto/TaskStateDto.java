package org.example.tasktracker.api.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.tasktracker.store.entity.TaskStateEntity;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskStateDto {

    @NonNull
    Long id;

    @NonNull
    String name;

    @NonNull
    Instant created;

    private Long leftTaskStateId;

    private Long rightTaskStateId;

    @NonNull
    List<TaskDto> tasks;

}
