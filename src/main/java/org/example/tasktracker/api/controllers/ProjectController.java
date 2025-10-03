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
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectDtoConverter projectDtoConverter;

    @GetMapping
    @Transactional
    public List<ProjectDto> getProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        Stream<ProjectEntity> projectStream = projectService.getProjects(optionalPrefixName);

        return projectStream
                .map(projectDtoConverter::makeProjectDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("{project_id}")
    public Boolean deleteProject(@PathVariable("project_id") Long projectId) {

        return projectService.delete(projectId);
    }

    @PostMapping
    public ProjectDto createProject(@RequestParam String name) {

        ProjectEntity projectEntity = projectService.create(name);

        return projectDtoConverter.makeProjectDto(projectEntity);

    }

    @PatchMapping("{project_id}")
    public ProjectDto editProject(@PathVariable("project_id") Long projectId,
                                  @RequestParam String name) {

        ProjectEntity project = projectService.edit(projectId, name);

        return projectDtoConverter.makeProjectDto(project);

    }

}
