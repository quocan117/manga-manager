package com.examplevsc.demo.controller;

import org.springframework.web.bind.annotation.*;
import com.examplevsc.demo.model.Student;
import com.examplevsc.demo.service.StudentService;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService){

        this.studentService = studentService;

    }

    @GetMapping
    public List<Student> getAllStudents(){

        return studentService.getAllStudents();

    }

    @PostMapping
    public Student addStudent(@RequestBody Student student){

        return studentService.addStudent(student);

    }
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id){
        return studentService.getStudentById(id);
    }
    @DeleteMapping
    public boolean deleteStudentById(@RequestParam Long id){
        return studentService.deleteStudentById(id);
    }
    
}
