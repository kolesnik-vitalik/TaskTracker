package org.example.tasktracker.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.dto.ProjectDto;
import org.example.tasktracker.api.converter.ProjectDtoConverter;
import org.example.tasktracker.service.ProjectService;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectDtoConverter projectDtoConverter;

    public static final String FETCH_PROJECT = "/api/projects";
    public static final String CREATE_PROJECT = "/api/projects";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";

    @GetMapping(FETCH_PROJECT)
    @Transactional
    public List<ProjectDto> getProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        Stream<ProjectEntity> projectStream = projectService.getProjects(optionalPrefixName);

        return projectStream
                .map(projectDtoConverter::makeProjectDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping(DELETE_PROJECT)
    public Boolean deleteProject(@PathVariable("project_id") Long projectId) {

        return projectService.deleteProjectOrThrowException(projectId);
    }

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam String name) {

        ProjectEntity projectEntity = projectService.createProjectOrThrowException(name);

        return projectDtoConverter.makeProjectDto(projectEntity);

    }

    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editProject(@PathVariable("project_id") Long projectId,
                                  @RequestParam String name) {

        ProjectEntity project = projectService.editProjectOrThrowException(projectId, name);

        return projectDtoConverter.makeProjectDto(project);

    }

}
