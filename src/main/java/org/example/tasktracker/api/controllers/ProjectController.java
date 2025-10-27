package org.example.tasktracker.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.dto.ProjectDto;
import org.example.tasktracker.api.converter.ProjectDtoConverter;
import org.example.tasktracker.service.ProjectService;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.springframework.http.ResponseEntity;
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

    @Tag(name = "Получение всех проектов", description = "Получение списка всех проектов с опциональным параметром по префиксу")
    @GetMapping
    @Transactional
    public List<ProjectDto> getProjects(
            @RequestParam(value = "prefixName", required = false) Optional<String> optionalPrefixName) {

        Stream<ProjectEntity> projectStream = projectService.getProjects(optionalPrefixName);

        return projectStream
                .map(projectDtoConverter::makeProjectDto)
                .collect(Collectors.toList());
    }

    @Tag(name = "Удаление проекта", description = "Удаление проекта по id")
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteById(@PathVariable("id") long projectId) {
        projectService.deleteById(projectId);
        return ResponseEntity.notFound().build();
    }

    @Tag(name = "Создание проекта", description = "Создание проекта по параметру имени")
    @PostMapping
    public ProjectDto createProject(@RequestParam String name) {

        ProjectEntity projectEntity = projectService.create(name);

        return projectDtoConverter.makeProjectDto(projectEntity);

    }

    @Tag(name = "Редактирование проекта", description = "Изменение названия проекта по его id")
    @PatchMapping("{id}")
    public ProjectDto edit(@PathVariable("id") long projectId,
                                  @RequestParam String name) {

        ProjectEntity project = projectService.edit(projectId, name);

        return projectDtoConverter.makeProjectDto(project);

    }

}
