package org.example.tasktracker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.api.exceptions.NotFoundException;
import org.example.tasktracker.store.entity.ProjectEntity;
import org.example.tasktracker.store.entity.TaskStateEntity;
import org.example.tasktracker.store.repository.ProjectRepository;
import org.example.tasktracker.store.repository.TaskStateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskStateService {
    private final ProjectRepository projectRepository;
    private final TaskStateRepository taskStateRepository;

    private final String TASK_STATE_NAME_EXISTS = "Task state name already exists";
    private final String PROJECT_NOT_FOUND = "Project not found";
    private final String TASK_STATE_NOT_FOUND = "Task state not found";
    private final String THE_SAME_TASK_STATE = "Left task state id can't be the same";
    private final String NOT_MATCH = "Left task state id doesn't match project id";

    public List<TaskStateEntity> getAllTaskStates(long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(PROJECT_NOT_FOUND));

        return project.getTaskStates();
    }

    @Transactional
    public TaskStateEntity create(long projectId, String name) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(PROJECT_NOT_FOUND));

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();
        for(TaskStateEntity taskState : project.getTaskStates()) {

            if(taskState.getName().equalsIgnoreCase(name)) {
                throw new BadRequestException(TASK_STATE_NAME_EXISTS);
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
    public TaskStateEntity update(long taskStateId, String name) {
        TaskStateEntity taskStateEntity = getTaskState(taskStateId);

        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskStateEntity.getProject().getId(),
                        name
                )
                .filter(anotherTaskStateEntity -> !anotherTaskStateEntity.getId().equals(taskStateId))
                .ifPresent(anotherTaskStateEntity -> {
                    throw new BadRequestException(TASK_STATE_NAME_EXISTS);
                });

        taskStateEntity.setName(name);

        return taskStateRepository.save(taskStateEntity);
    }

    @Transactional
    public TaskStateEntity change(Long taskStateId, Optional<Long> optionalLeftTaskStateId) {
        TaskStateEntity changeTaskState = getTaskState(taskStateId);

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
                        throw new BadRequestException(THE_SAME_TASK_STATE);
                    }

                    TaskStateEntity leftTaskStateEntity = getTaskState(leftTaskStateId);

                    if(!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                        throw new BadRequestException(NOT_MATCH);
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

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);

        optionalNewLeftTaskState.ifPresent(taskStateRepository::saveAndFlush);
        optionalNewRightTaskState.ifPresent(taskStateRepository::saveAndFlush);

        return changeTaskState;
    }

    @Transactional
    public TaskStateEntity delete(long taskStateId) {
        TaskStateEntity changeTaskState = getTaskState(taskStateId);

        replaceOldTaskStates(changeTaskState);

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);
        taskStateRepository.delete(changeTaskState);

        return changeTaskState;
    }

    private TaskStateEntity getTaskState(long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new NotFoundException(TASK_STATE_NOT_FOUND));
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
