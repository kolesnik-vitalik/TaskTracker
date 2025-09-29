package org.example.tasktracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.controllers.helper.ControllerHelper;
import org.example.tasktracker.api.converter.TaskStateDtoConverter;
import org.example.tasktracker.api.dto.TaskStateDto;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.api.exceptions.NotFoundException;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.example.tasktracker.store.entity.TaskStateEntity;
import org.example.tasktracker.store.repository.TaskStateRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TaskStateController {

    private final TaskStateRepository taskStateRepository;

    private final TaskStateDtoConverter taskStateDtoConverter;

    private final ControllerHelper controllerHelper;

    public static final String GET_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String UPDATE_TASK_STATE = "/api/task-states/{task_state_id}";


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

        if(name.trim().isEmpty()) {
            throw new BadRequestException("Task state name cannot be empty");
        }

        ProjectEntity project = controllerHelper.getProjectEntity(projectId);

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();
        for(TaskStateEntity taskState : project.getTaskStates()) {

            if(taskState.getName().equalsIgnoreCase(name)) {
                throw new BadRequestException("Task state name already exists");
            }

            if(!taskState.getRightTaskState().isPresent()) {
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }


        }

        TaskStateEntity taskStateEntity = taskStateRepository.saveAndFlush(
                TaskStateEntity.builder()
                        .name(name)
                        .project(project)
                        .build()
        );

        optionalAnotherTaskState
                .ifPresent(anotherTaskStateEntity -> {
                    taskStateEntity.setLeftTaskState(anotherTaskStateEntity);
                    anotherTaskStateEntity.setRightTaskState(taskStateEntity);

                    taskStateRepository.saveAndFlush(anotherTaskStateEntity);
                }
                );

        final TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskStateEntity);

        return taskStateDtoConverter.makeTaskStateDto(savedTaskState);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDto updateTaskState(@PathVariable(name="task_state_id") Long taskStateId,
                                        @RequestParam(name="task_state_name") String name){

        if(name.trim().isEmpty()) {
            throw new BadRequestException("Task state name cannot be empty");
        }

        TaskStateEntity taskStateEntity = getTaskStateOrThrowException(taskStateId);

        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskStateEntity.getProject().getId(),
                        name
                )
                .filter(anotherTaskStateEntity -> !anotherTaskStateEntity.getId().equals(taskStateId))
                .ifPresent(anotherTaskStateEntity -> {
                    throw new BadRequestException("Task state name already exists");
                });

        taskStateEntity.setName(name);

        taskStateEntity = taskStateRepository.save(taskStateEntity);

        return taskStateDtoConverter.makeTaskStateDto(taskStateEntity);
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new NotFoundException("Task state not found"));
    }
}
