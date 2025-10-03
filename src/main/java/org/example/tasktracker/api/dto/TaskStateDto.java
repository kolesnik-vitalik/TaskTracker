package org.example.tasktracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskStateDto {

    @JsonProperty("id")
    @NonNull
    Long id;

    @JsonProperty("name")
    @NonNull
    String name;

    @JsonProperty("created")
    @NonNull
    Instant created;

    @JsonProperty("leftTaskStateId")
    Long leftTaskStateId;

    @JsonProperty("rightTaskStateId")
    Long rightTaskStateId;

    @JsonProperty("tasks")
    @NonNull
    List<TaskDto> tasks;

}
