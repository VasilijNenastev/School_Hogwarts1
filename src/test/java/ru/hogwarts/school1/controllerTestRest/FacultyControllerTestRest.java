package ru.hogwarts.school1.controllerTestRest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import ru.hogwarts.school1.controller.FacultyController;
import ru.hogwarts.school1.model.Faculty;
import ru.hogwarts.school1.model.Student;
import ru.hogwarts.school1.repositories.FacultyRepository;
import ru.hogwarts.school1.repositories.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FacultyControllerTestRest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private FacultyController facultyController;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;
    private Faculty griffindor;
    private Faculty slizerin;
    private List<Faculty> faculties;


    @Test
    void contextLoads() {
        assertThat(facultyController).isNotNull();
    }

    @AfterEach
    public void afterEach() {
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        griffindor = new Faculty();
        slizerin = new Faculty();
        griffindor.setName("Гриффиндор");
        griffindor.setColor("Жёлтый");
        slizerin.setName("Слизерин");
        slizerin.setColor("Зелёный");
        faculties = facultyRepository.saveAll(List.of(griffindor, slizerin));
    }

    @Test
    void getFaculty() {
        long id = griffindor.getId();
        final ResponseEntity<Faculty> response = restTemplate.getForEntity("http://localhost:" + port + "/faculty/" + id, Faculty.class);
        Faculty facultyGetBody = response.getBody();
        Optional<Faculty> fromDb = facultyRepository.findById(id);
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get()).isEqualTo(facultyGetBody);
    }

    @Test
    void getFacultyInfoNegativeTest() {
        Optional<Faculty> faculty = facultyRepository.findById(slizerin.getId());
        long id = faculty.get().getId();
        final ResponseEntity<Faculty> response = restTemplate.getForEntity("http://localhost:" + port + "/faculty/" + id + 1, Faculty.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllFaculties() {
        final ResponseEntity<List<Faculty>> response = restTemplate.exchange(
                "http://localhost:" + port + "/faculty",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        List<Faculty> findFaculties = response.getBody();
        assertThat(findFaculties).isEqualTo(faculties);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findFaculties).isNotNull();
    }

    @Test
    void createFaculty() {
        Faculty ravenclaw = new Faculty();
        ravenclaw.setName("Когтевран");
        ravenclaw.setColor("Пурпурный");
        ResponseEntity<Faculty> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/faculty",
                ravenclaw,
                Faculty.class);
        Faculty created = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(ravenclaw);
        Optional<Faculty> fromDb = facultyRepository.findById(created.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get())
                .isEqualTo(created);
    }

    @Test
    void updatePositiveTest() {
        long id = slizerin.getId();
        Faculty change = new Faculty();
        change.setName("Когтевран");
        change.setColor("Красный");
        final ResponseEntity<Faculty> response = restTemplate.exchange(
                "http://localhost:" + port + "/faculty/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(change),
                Faculty.class);
        Faculty actual = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(change)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(actual);

        Optional<Faculty> fromDb = facultyRepository.findById(actual.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get())
                .isEqualTo(actual);
    }

    @Test
    void updateNegativeTest() {
        long id = griffindor.getId();
        Faculty change = new Faculty();
        change.setName("Когтевран");
        change.setColor("Красный");
        final ResponseEntity<Faculty> response = restTemplate.exchange(
                String.format("http://localhost:" + port + "/faculty/" + id + 1),
                HttpMethod.PUT,
                new HttpEntity<>(change),
                Faculty.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteFaculty() {
        long id = griffindor.getId();
        restTemplate.delete("http://localhost:" + port + "/faculty/{id}", id, String.class);
        final ResponseEntity<Faculty> response = restTemplate.getForEntity("http://localhost:" + port + "/faculty/" + id, Faculty.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getFacultyByColor() {
        String color = "Жёлтый";
        List<Faculty> expected = faculties.stream()
                .filter(faculty -> faculty.getColor().equals(color))
                .toList();
        final ResponseEntity<List<Faculty>> response = restTemplate.exchange(
                "http://localhost:" + port + "/faculty?find-by-color=" + color,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                Map.of("color", color)
        );
        List<Faculty> actual = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual).isEqualTo(expected);
        assertThat(actual).isNotNull();
    }

    @Test
    void findStudentsOfFaculty() {
        long id = griffindor.getId();
        Student harryPotter = new Student();
        harryPotter.setName("Гарри Поттер");
        harryPotter.setAge(25);
        harryPotter.setFaculty(griffindor);

        Student hermioneGranger = new Student();
        hermioneGranger.setName("Гермиона Грейнджер");
        hermioneGranger.setAge(23);
        hermioneGranger.setFaculty(griffindor);

        Student drakoMalfoy = new Student();
        drakoMalfoy.setName("Драко Малфой");
        drakoMalfoy.setAge(18);
        drakoMalfoy.setFaculty(slizerin);

        List<Student> students = studentRepository.saveAll(List.of(harryPotter, hermioneGranger, drakoMalfoy));

        List<Student> expected = students.stream()
                .filter(student -> student.getFaculty().getId() == id)
                .toList();

        final ResponseEntity<List<Student>> response = restTemplate.exchange(
                "http://localhost:" + port + "/faculty/" + id + "/students",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        List<Student> actual = response.getBody();
        assertThat(actual).isEqualTo(expected);
        assertThat(actual).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
