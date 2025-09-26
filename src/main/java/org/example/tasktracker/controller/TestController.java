package org.example.tasktracker.controller;

import org.example.tasktracker.entity.Task;
import org.example.tasktracker.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TestController {
    @Autowired
    private TaskRepository taskRepository;
    
    @GetMapping("/{id}")
    public String index(@PathVariable Long id) {
        return taskRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Task not found"))
                                .getName();
    }

    @PostMapping("/tasks")
    public String createTask(@RequestParam(name = "name") String name) {
        Task task = new Task();
        task.setName(name); 
        taskRepository.save(task);
        return "Task created successfully";
    }
}
