package com.examplevsc.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.examplevsc.demo.model.Student;
import com.examplevsc.demo.repository.StudentRepository;

@Service

public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents(){
        return studentRepository.findAll();
    }

    public Student addStudent(Student student){
        return studentRepository.save(student);
    }

    public Student getStudentById(Long id){
        return studentRepository.findById(id).orElse(null);
    }

    public Student updateStudent(Long id, Student updatedStudent){
        Optional<Student> optional = studentRepository.findById(id);
        if(optional.isPresent()){
            Student student = optional.get();
            student.setName(updatedStudent.getName());
            student.setAge(updatedStudent.getAge());
            student.setEmail(updatedStudent.getEmail());
            student.setPhone(updatedStudent.getPhone());
            return studentRepository.save(student);
        }
        return null;
    }

    public boolean deleteStudentById(Long id){
        if(studentRepository.existsById(id)){
            studentRepository.deleteById(id);
        return true;            
        }
        return false;
    }

}
