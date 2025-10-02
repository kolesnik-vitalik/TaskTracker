package org.example.tasktracker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.controllers.helper.ControllerHelper;
import org.example.tasktracker.api.converter.TaskStateDtoConverter;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.api.exceptions.NotFoundException;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.example.tasktracker.store.entity.TaskStateEntity;
import org.example.tasktracker.store.repository.TaskStateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskStateService {

    private final TaskStateRepository taskStateRepository;

    private final TaskStateDtoConverter taskStateDtoConverter;

    private final ControllerHelper controllerHelper;

    public List<TaskStateEntity> getAllTaskStates(Long projectId) {

        ProjectEntity project = controllerHelper.getProjectEntity(projectId);

        return project.getTaskStates();
    }

    @Transactional
    public TaskStateEntity createTaskState(Long projectId, String name) {

        ProjectEntity project = controllerHelper.getProjectEntity(projectId);

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();
        for(TaskStateEntity taskState : project.getTaskStates()) {

            if(taskState.getName().equalsIgnoreCase(name)) {
                throw new BadRequestException("Task state name already exists");
            }

            if(taskState.getRightTaskState().isEmpty()) {
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

        return taskStateRepository.saveAndFlush(taskStateEntity);
    }

    @Transactional
    public TaskStateEntity updateTaskState(Long taskStateId, String name) {
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

        return taskStateRepository.save(taskStateEntity);
    }

    @Transactional
    public TaskStateEntity changeTaskState(Long taskStateId, Optional<Long> optionalLeftTaskStateId) {
        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        ProjectEntity project = changeTaskState.getProject();

        Optional<Long> oldLeftTaskStateId =  changeTaskState
                .getLeftTaskState()
                .map(TaskStateEntity::getId);

        if(oldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return changeTaskState;
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
        if(optionalNewLeftTaskState.isEmpty()) {
            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> anotherTaskState.getLeftTaskState().isEmpty())
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

        return changeTaskState;
    }

    @Transactional
    public TaskStateEntity deleteTaskState(Long taskStateId) {
        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        replaceOldTaskStates(changeTaskState);

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);
        taskStateRepository.delete(changeTaskState);

        return changeTaskState;
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new NotFoundException("Task state not found"));
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
}
