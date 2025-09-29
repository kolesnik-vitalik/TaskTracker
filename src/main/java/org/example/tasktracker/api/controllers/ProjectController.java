package org.example.tasktracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.dto.ProjectDto;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.api.exceptions.NotFoundException;
import org.example.tasktracker.api.converter.ProjectDtoConverter;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.example.tasktracker.store.repository.ProjectRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;

    private final ProjectDtoConverter projectDtoConverter;

    public static final String FETCH_PROJECT = "/api/projects";
    public static final String CREATE_PROJECT = "/api/projects";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";

    @GetMapping(FETCH_PROJECT)
    @Transactional
    public List<ProjectDto> getProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::findAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        return projectStream
                .map(projectDtoConverter::makeProjectDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping(DELETE_PROJECT)
    public Boolean deleteProject(@PathVariable("project_id") Long projectId) {

        projectRepository.findById(projectId)
                        .orElseThrow(() -> new NotFoundException("Project not found"));

        projectRepository.deleteById(projectId);

        return true;
    }

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam String name) {

        if(name.isEmpty()) {
            throw new BadRequestException("Project name cannot be empty");
        }

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

        return projectDtoConverter.makeProjectDto(projectEntity);

    }

    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editProject(@PathVariable("project_id") Long projectId,
                                  @RequestParam String name) {

        ProjectEntity project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException("Project with id " + projectId + " not found")
                );

        projectRepository
                .findByName(name)
                .filter(anotherProject -> !Objects.equals(projectId, anotherProject.getId()))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists", name));
                });

        project.setName(name);

        project = projectRepository.saveAndFlush(project);

        return projectDtoConverter.makeProjectDto(project);

    }

}
