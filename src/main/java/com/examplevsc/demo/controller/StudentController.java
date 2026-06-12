package com.examplevsc.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.examplevsc.demo.model.Student;
import com.examplevsc.demo.service.StudentService;

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
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id){
        Student student  = studentService.getStudentById(id);
        if(student==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }
    @PostMapping
    public ResponseEntity<Student> addStudent(@RequestBody Student student){

        Student saved = studentService.addStudent(student);
        return ResponseEntity.status(201).body(saved);

    }

    @PutMapping("/{id}")
    public Student UpdateStudent(@PathVariable Long id, @RequestBody Student student){
        return studentService.updateStudent(id, student);
    }
    
    @DeleteMapping("/{id}")
    public boolean deleteStudentById(@PathVariable Long id){
        return studentService.deleteStudentById(id);
    }
    
}
