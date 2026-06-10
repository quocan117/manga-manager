package com.examplevsc.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.examplevsc.demo.model.Student;

@Service

public class StudentService {
    private List<Student> students = new ArrayList<>();

    public StudentService() {
        students.add(new Student(1L, "Kel", 20));
        students.add(new Student(2L, "Skie", 21));
    }

    public List<Student> getAllStudents(){
         return students;

    }

    public Student addStudent(Student student){
        students.add(student);
        return student;
    }

    public Student getStudentById(Long id){
        for(Student student: students){
            if(student.getId().equals(id)){
                return student;
            }           
        }
        return null;
    }
    public boolean deleteStudentById(Long id){
        return students.removeIf(student -> student.getId().equals(id));
    }

}
