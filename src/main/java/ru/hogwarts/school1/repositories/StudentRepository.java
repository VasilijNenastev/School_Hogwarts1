package ru.hogwarts.school1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwarts.school1.model.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository <Student, Long> {
 List <Student> findByAgeBetween(Integer min, Integer max);
 Student findByIdOrNameIgnoreCase(Long id, String name);
 List<Student> findByFaculty_Id(long id);
 List<Student> findByAge(int age);

}
