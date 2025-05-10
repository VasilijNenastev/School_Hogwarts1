package ru.hogwarts.school1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwarts.school1.model.Faculty;

import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty,Long> {

   List <Faculty> findByNameIgnoringCase(String name);
   List <Faculty> findByColorIgnoringCase(String color);
   List<Faculty> findByNameAndColorContainingIgnoreCase(String name, String color);
   Faculty findByIdOrNameOrColorContainingIgnoreCase(Long id, String name, String color);

}
