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
import java.util.Objects;
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
    public static final String CHANGE_TASK_POSITION = "/api/task-states/{task_state_id}/position/change";
    public static final String DELETE_TASK = "/api/task-states/{task_state_id}";



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

    @PatchMapping(CHANGE_TASK_POSITION)
    public TaskStateDto changeTaskPosition(@PathVariable(name="task_state_id") Long taskStateId,
                                        @RequestParam(name= "left_task_state_id") Optional<Long> optionalLeftTaskStateId){

        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        ProjectEntity project = changeTaskState.getProject();

        Optional<Long> oldLeftTaskStateId =  changeTaskState
                .getLeftTaskState()
                .map(TaskStateEntity::getId);

        if(oldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return taskStateDtoConverter.makeTaskStateDto(changeTaskState);
        }

        Optional<TaskStateEntity> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {

                    if(taskStateId.equals(leftTaskStateId)) {
                        throw new BadRequestException("Left task state id can't be the same");
                    }

                    TaskStateEntity leftTaskStateEntity = getTaskStateOrThrowException(leftTaskStateId);

                    if(!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                        throw new BadRequestException("Left task state id doesn't match project id");
                    }

                    return leftTaskStateEntity;

                });

        Optional<TaskStateEntity> optionalNewRightTaskState;
        if(!optionalNewLeftTaskState.isPresent()) {
            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> !anotherTaskState.getLeftTaskState().isPresent())
                    .findAny();
        }else{
            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        replaceOldTaskStates(changeTaskState);

        if(optionalNewLeftTaskState.isPresent()) {

            TaskStateEntity newLeftTaskState = optionalNewLeftTaskState.get();

            newLeftTaskState.setRightTaskState(changeTaskState);

            changeTaskState.setLeftTaskState(newLeftTaskState);
        }else{
            changeTaskState.setLeftTaskState(null);
        }

        if(optionalNewRightTaskState.isPresent()) {

            TaskStateEntity newRightTaskState = optionalNewRightTaskState.get();

            newRightTaskState.setLeftTaskState(changeTaskState);

            changeTaskState.setRightTaskState(newRightTaskState);
        }else{
            changeTaskState.setRightTaskState(null);
        }

        changeTaskState = taskStateRepository.save(changeTaskState);

        optionalNewLeftTaskState.ifPresent(taskStateRepository::saveAndFlush);
        optionalNewRightTaskState.ifPresent(taskStateRepository::saveAndFlush);

        return taskStateDtoConverter.makeTaskStateDto(changeTaskState);
    }

    @DeleteMapping(DELETE_TASK)
    public TaskStateDto deleteTaskPosition(@PathVariable(name="task_state_id") Long taskStateId) {

        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        replaceOldTaskStates(changeTaskState);

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);
        taskStateRepository.delete(changeTaskState);

        return taskStateDtoConverter.makeTaskStateDto(changeTaskState);
    }

    private void replaceOldTaskStates(TaskStateEntity changeTaskState) {
        Optional<TaskStateEntity> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskStateEntity> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState.ifPresent(taskState -> {

            taskState.setRightTaskState(optionalOldRightTaskState.orElse(null));

            taskStateRepository.saveAndFlush(taskState);

        });

        optionalOldRightTaskState.ifPresent(taskState -> {

            taskState.setLeftTaskState(optionalOldLeftTaskState.orElse(null));

            taskStateRepository.saveAndFlush(taskState);

        });
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new NotFoundException("Task state not found"));
    }
}
