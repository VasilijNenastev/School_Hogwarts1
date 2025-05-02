package ru.hogwarts.school1.controllerTestRest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.hogwarts.school1.controller.StudentController;
import ru.hogwarts.school1.model.Faculty;
import ru.hogwarts.school1.model.Student;
import ru.hogwarts.school1.repositories.FacultyRepository;
import ru.hogwarts.school1.repositories.StudentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerTestRest {
    @LocalServerPort
    private int port;

    @Autowired
    private StudentController studentController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;


    private Faculty griffindor;
    private Faculty slizerin;
    Student harryPotter = new Student();
    Student hermioneGranger = new Student();
    private List<Student> students;

    @AfterEach
    public void afterEach() {
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertThat(studentController).isNotNull();
    }

    @BeforeEach
    public void setUp() {
        griffindor = new Faculty();
        slizerin = new Faculty();
        griffindor.setName("Гриффиндор");
        griffindor.setColor("Жёлтый");
        slizerin.setName("Слизерин");
        slizerin.setColor("Зелёный");
        facultyRepository.saveAll(List.of(griffindor, slizerin));

        harryPotter.setName("Гарри Поттер");
        harryPotter.setAge(25);
        harryPotter.setFaculty(griffindor);


        hermioneGranger.setName("Гермиона Грейнджер");
        hermioneGranger.setAge(23);
        hermioneGranger.setFaculty(griffindor);

        Student drakoMalfoy = new Student();
        drakoMalfoy.setName("Драко Малфой");
        drakoMalfoy.setAge(18);
        drakoMalfoy.setFaculty(slizerin);

        students = studentRepository.saveAll(List.of(harryPotter, hermioneGranger, drakoMalfoy));
    }

    @Test
    void getStudentInfoPositiveTest() {
        long id = harryPotter.getId();
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/student" + id, String.class)).isNotNull();
        final ResponseEntity<Student> response = restTemplate.getForEntity("http://localhost:" + port + "/student/" + id, Student.class);
        Student studentGetBody = response.getBody();
        Optional<Student> fromDb = studentRepository.findById(id);
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get()).isEqualTo(studentGetBody);
    }

    @Test
    void getStudentInfoNegativeTest() {
        long id = harryPotter.getId();
        final ResponseEntity<Student> response = restTemplate.getForEntity("http://localhost:" + port + "/student/" + id + 1, Student.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllStudents() {
        final ResponseEntity<List<Student>> response = restTemplate.exchange(
                "http://localhost:" + port + "/student",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        List<Student> findStudents = response.getBody();
        assertThat(findStudents).isEqualTo(students);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findStudents).isNotNull();
    }

    @Test
    void findByAgeBetween() {
        int min = 20;
        int max = 25;
        List<Student> expected = students.stream()
                .filter(student -> student.getAge() >= min && student.getAge() <= max)
                .toList();

        final ResponseEntity<List<Student>> response = restTemplate.exchange(
                "http://localhost:" + port + "/student?min=" + min + "&max=" + max,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                Map.of("min", min, "max", max)
        );
        List<Student> findStudents = response.getBody();
        assertThat(findStudents)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findStudents).isNotNull();
    }

    @Test
    void createStudent() {
        Student student = new Student();
        Faculty faculty = facultyRepository.findAll(PageRequest.of(0, 1))
                .getContent()
                .get(0);
        student.setName("Рон Уизли");
        student.setAge(24);
        student.setFaculty(faculty);
        ResponseEntity<Student> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/student",
                student,
                Student.class);
        Student created = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(student);
        Optional<Student> fromDb = studentRepository.findById(created.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get())
                .isEqualTo(created);
    }

    @Test
    void editStudentPositiveTest() {
        Faculty faculty = facultyRepository.findAll(PageRequest.of(1, 1))
                .getContent()
                .get(0);
        long id = harryPotter.getId();
        Student change = new Student();
        change.setName("Рон Уизли");
        change.setAge(24);
        change.setFaculty(faculty);
        final ResponseEntity<Student> response = restTemplate.exchange(
                String.format("http://localhost:" + port + "/student/" + id),
                HttpMethod.PUT,
                new HttpEntity<>(change),
                Student.class);

        Student actual = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(change)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(actual);
    }

    @Test
    void editStudentNegativeTest() {
        long id = harryPotter.getId();
        Student editStudent = new Student();
        editStudent.setName("Рон Уизли");
        editStudent.setAge(24);
        editStudent.setFaculty(slizerin);
        final ResponseEntity<Student> response = restTemplate.exchange(
                String.format("http://localhost:" + port + "/student/" + id + 1),
                HttpMethod.PUT,
                new HttpEntity<>(editStudent),
                Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteStudent() {
        long id = hermioneGranger.getId();
        restTemplate.delete("http://localhost:" + port + "/student/" + id, String.class);
        final ResponseEntity<Student> response = restTemplate.getForEntity("http://localhost:" + port + "/student/" + id, Student.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getStudentsByAge() {
        int age = 23;
        List<Student> expected = students.stream()
                .filter(student -> student.getAge() == age)
                .toList();

        final ResponseEntity<List<Student>> response = restTemplate.exchange(
                "http://localhost:" + port + "/student?find-by-age=" + age,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                Map.of("age", age)
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Student> actual = response.getBody();
        assertThat(expected).isEqualTo(actual);
    }

    @Test
    void findFaculty() {
        long id = hermioneGranger.getId();
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/student/" + id + "/faculty", String.class)).isNotNull();
        final ResponseEntity<Faculty> response = restTemplate.getForEntity("http://localhost:" + port + "/student/" + id + "/faculty", Faculty.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Faculty actual = response.getBody();
        assertThat(griffindor).isEqualTo(actual);
    }
}
