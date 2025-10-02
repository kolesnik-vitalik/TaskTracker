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

    @Transactional
    public Stream<ProjectEntity> getProjects(Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        return optionalPrefixName
                .map(projectRepository::findAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);
    }

    @Transactional
    public Boolean deleteProjectOrThrowException(Long projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        projectRepository.deleteById(projectId);

        return true;
    }

    @Transactional
    public ProjectEntity editProjectOrThrowException(Long projectId, String name) {
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

        return project;
    }

    @Transactional
    public ProjectEntity createProjectOrThrowException(String name) {
        if(name.isEmpty()) {
            throw new BadRequestException("Project name cannot be empty");
        }

        projectRepository
                .findByName(name)
                .ifPresent(project -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists", name));
                });

        return projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(name)
                        .build()
        );
    }
}
