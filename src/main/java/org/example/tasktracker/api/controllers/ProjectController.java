package org.example.tasktracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.dto.ProjectDto;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.api.factories.ProjectDtoFactory;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.example.tasktracker.store.repository.ProjectRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;

    private final ProjectDtoFactory projectDtoFactory;

    public static final String CREATE_PROJECT = "/api/project";

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam String name) {

        projectRepository
                .findByName(name)
                .ifPresent(project -> {
                   throw new BadRequestException(String.format("Project \"%s\" already exists", name));
                });

        ProjectEntity projectEntity = projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(name)
                        .build()
        );

        return projectDtoFactory.makeProjectDto(projectEntity);

    }

}
