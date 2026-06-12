package com.examplevsc.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.examplevsc.demo.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

}
