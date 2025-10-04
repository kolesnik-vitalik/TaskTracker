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
public class ProjectDto {

    @JsonProperty("id")
    @NonNull
    Long id;

    @JsonProperty("name")
    @NonNull
    String name;

    @JsonProperty("createdAt")
    @NonNull
    Instant createdAt;
}
