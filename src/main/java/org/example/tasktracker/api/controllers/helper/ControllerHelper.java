package org.example.tasktracker.api.controllers.helper;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.exceptions.NotFoundException;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.example.tasktracker.store.repository.ProjectRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerHelper {

    private final ProjectRepository projectRepository;

    public ProjectEntity getProjectEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }


}
