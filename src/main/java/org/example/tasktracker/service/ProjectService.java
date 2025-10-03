package org.example.tasktracker.service;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.api.exceptions.NotFoundException;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.example.tasktracker.store.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final String PROJECT_NOT_FOUND = "Project with id %s not found";
    private final String PROJECT_NAME_CANNOT_BE_EMPTY = "Project name cannot be empty";

    public Stream<ProjectEntity> getProjects(Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        return optionalPrefixName
                .map(projectRepository::findAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);
    }

    @Transactional
    public void deleteById(Long projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        projectRepository.deleteById(projectId);

    }

    @Transactional
    public ProjectEntity edit(Long projectId, String name) {
        ProjectEntity project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException(PROJECT_NOT_FOUND.formatted(projectId)));

        projectRepository
                .findByName(name)
                .filter(anotherProject -> !Objects.equals(projectId, anotherProject.getId()))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists", name));
                });

        project.setName(name);

        project = projectRepository.saveAndFlush(project);

        return project;
    }

    @Transactional
    public ProjectEntity create(String name) {
        if(name.isEmpty()) {
            throw new BadRequestException(PROJECT_NAME_CANNOT_BE_EMPTY);
        }

        projectRepository
                .findByName(name)
                .ifPresent(project -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists", name));
                });

        ProjectEntity project = ProjectEntity.builder()
                .name(name)
                .build();

        return projectRepository.saveAndFlush(project);
    }
}
