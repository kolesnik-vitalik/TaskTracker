package org.example.tasktracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDto {

    @JsonProperty("id")
    @NonNull
    Long id;

    @JsonProperty("name")
    @NonNull
    String name;

    @JsonProperty("created")
    @NonNull
    Instant created;

    @JsonProperty("description")
    @NonNull
    String description;

}
