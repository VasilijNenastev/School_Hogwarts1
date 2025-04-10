package ru.hogwarts.school1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwarts.school1.model.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty,Long> {
}
