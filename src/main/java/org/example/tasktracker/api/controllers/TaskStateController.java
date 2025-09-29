package org.example.tasktracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.controllers.helper.ControllerHelper;
import org.example.tasktracker.api.converter.TaskStateDtoConverter;
import org.example.tasktracker.api.dto.TaskStateDto;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.example.tasktracker.store.entity.TaskStateEntity;
import org.example.tasktracker.store.repository.TaskStateRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TaskStateController {

    private final TaskStateRepository taskStateRepository;

    private final TaskStateDtoConverter taskStateDtoConverter;

    private final ControllerHelper controllerHelper;

    public static final String GET_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String DELETE_TASK_STATE = "/api/projects/{project_id}";


    @GetMapping(GET_TASK_STATE)
    public List<TaskStateDto> getTaskStates(@PathVariable(name="project_id") Long projectId) {
        ProjectEntity project = controllerHelper.getProjectEntity(projectId);

        return project.getTaskStates()
                .stream()
                .map(taskStateDtoConverter::makeTaskStateDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(@PathVariable(name="project_id") Long projectId,
                                        @RequestParam(name="task_state_name") String name) {

        if(name.isEmpty()) {
            throw new BadRequestException("Task state name cannot be empty");
        }

        ProjectEntity project = controllerHelper.getProjectEntity(projectId);

        project.getTaskStates()
                .stream()
                .map(TaskStateEntity::getName)
                .filter(taskStateName -> taskStateName.equalsIgnoreCase(name))
                .findAny()
                .ifPresent(taskName ->{
                    throw new BadRequestException("Task state name already exists");
                });

        TaskStateEntity taskStateEntity = taskStateRepository.saveAndFlush(
          TaskStateEntity.builder()
                  .name(name)
                  .build()
        );

        return taskStateDtoConverter.makeTaskStateDto(taskStateEntity);
    }
}
