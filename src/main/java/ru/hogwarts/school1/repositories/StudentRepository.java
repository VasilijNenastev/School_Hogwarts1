package ru.hogwarts.school1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwarts.school1.model.Student;

public interface StudentRepository extends JpaRepository <Student, Long> {
}
