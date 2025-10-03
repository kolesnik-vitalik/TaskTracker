package org.example.tasktracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.tasktracker.api.converter.TaskStateDtoConverter;
import org.example.tasktracker.api.dto.TaskStateDto;
import org.example.tasktracker.api.exceptions.BadRequestException;
import org.example.tasktracker.service.TaskStateService;
import org.example.tasktracker.store.entity.TaskStateEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/task-states")
public class TaskStateController {

    private final TaskStateService taskStateService;

    private final TaskStateDtoConverter taskStateDtoConverter;

    @GetMapping("/project/{projectId}")
    public List<TaskStateDto> getTaskStates(@PathVariable(name="projectId") long projectId) {

        List<TaskStateEntity> taskStateList = taskStateService.getAllTaskStates(projectId);

        return taskStateList.stream()
                .map(taskStateDtoConverter::makeTaskStateDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/project/{projectId}")
    public TaskStateDto createTaskState(@PathVariable(name="projectId") long projectId,
                                        @RequestParam(name="taskStateName") String name) {

        if(name.trim().isEmpty()) {
            throw new BadRequestException("Task state name cannot be empty");
        }

        final TaskStateEntity savedTaskState = taskStateService.create(projectId, name);

        return taskStateDtoConverter.makeTaskStateDto(savedTaskState);
    }

    @PatchMapping("/{taskStateId}")
    public TaskStateDto updateTaskState(@PathVariable(name="taskStateId") long taskStateId,
                                        @RequestParam(name="taskStateName") String name){

        if(name.trim().isEmpty()) {
            throw new BadRequestException("Task state name cannot be empty");
        }

        TaskStateEntity savedTaskState = taskStateService.update(taskStateId, name);

        return taskStateDtoConverter.makeTaskStateDto(savedTaskState);
    }

    @PatchMapping("/{taskStateId}/position/change")
    public TaskStateDto changeTaskPosition(@PathVariable(name="taskStateId") long taskStateId,
                                        @RequestParam(name= "leftTaskStateId") Optional<Long> optionalLeftTaskStateId){

        TaskStateEntity changeTaskState = taskStateService.change(taskStateId, optionalLeftTaskStateId);

        return taskStateDtoConverter.makeTaskStateDto(changeTaskState);
    }

    @DeleteMapping("/{taskStateId}")
    public TaskStateDto deleteTaskPosition(@PathVariable(name="taskStateId") long taskStateId) {
        TaskStateEntity changeTaskState = taskStateService.delete(taskStateId);
        return taskStateDtoConverter.makeTaskStateDto(changeTaskState);
    }

}
