package org.example.tasktracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.converter.TaskStateDtoConverter;
import org.example.tasktracker.api.dto.TaskStateDto;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.service.TaskStateService;
import org.example.tasktracker.store.entity.TaskStateEntity;
import org.example.tasktracker.store.repository.TaskStateRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TaskStateController {

    private final TaskStateService taskStateService;

    private final TaskStateRepository taskStateRepository;

    private final TaskStateDtoConverter taskStateDtoConverter;

    public static final String GET_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String UPDATE_TASK_STATE = "/api/task-states/{task_state_id}";
    public static final String CHANGE_TASK_POSITION = "/api/task-states/{task_state_id}/position/change";
    public static final String DELETE_TASK = "/api/task-states/{task_state_id}";



    @GetMapping(GET_TASK_STATE)
    public List<TaskStateDto> getTaskStates(@PathVariable(name="project_id") Long projectId) {

        List<TaskStateEntity> taskStateList = taskStateService.getAllTaskStates(projectId);

        return taskStateList
                .stream()
                .map(taskStateDtoConverter::makeTaskStateDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(@PathVariable(name="project_id") Long projectId,
                                        @RequestParam(name="task_state_name") String name) {

        if(name.trim().isEmpty()) {
            throw new BadRequestException("Task state name cannot be empty");
        }

        final TaskStateEntity savedTaskState = taskStateService.createTaskState(projectId, name);

        return taskStateDtoConverter.makeTaskStateDto(savedTaskState);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDto updateTaskState(@PathVariable(name="task_state_id") Long taskStateId,
                                        @RequestParam(name="task_state_name") String name){

        if(name.trim().isEmpty()) {
            throw new BadRequestException("Task state name cannot be empty");
        }

        TaskStateEntity savedTaskState = taskStateService.updateTaskState(taskStateId, name);

        return taskStateDtoConverter.makeTaskStateDto(savedTaskState);
    }

    @PatchMapping(CHANGE_TASK_POSITION)
    public TaskStateDto changeTaskPosition(@PathVariable(name="task_state_id") Long taskStateId,
                                        @RequestParam(name= "left_task_state_id") Optional<Long> optionalLeftTaskStateId){

        TaskStateEntity changeTaskState = taskStateService.changeTaskState(taskStateId, optionalLeftTaskStateId);

        return taskStateDtoConverter.makeTaskStateDto(changeTaskState);
    }

    @DeleteMapping(DELETE_TASK)
    public TaskStateDto deleteTaskPosition(@PathVariable(name="task_state_id") Long taskStateId) {
        TaskStateEntity changeTaskState = taskStateService.deleteTaskState(taskStateId);
        return taskStateDtoConverter.makeTaskStateDto(changeTaskState);
    }

}
