package ru.hogwarts.school1.controllerTestWebMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.hogwarts.school1.controller.StudentController;
import ru.hogwarts.school1.model.Faculty;
import ru.hogwarts.school1.model.Student;
import ru.hogwarts.school1.repositories.FacultyRepository;
import ru.hogwarts.school1.repositories.StudentRepository;
import ru.hogwarts.school1.service.StudentService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = StudentController.class)
public class StudentControllerTestWebMvc {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private FacultyRepository facultyRepository;
    @MockBean
    private StudentRepository studentRepository;

    @SpyBean
    private StudentService studentService;


    @Test
    void getStudentInfoPositiveTest() throws Exception {
        final long id = 1L;
        when(studentRepository.findById(any())).thenReturn(Optional.of(createStudentForTests()));

        mockMvc.perform(MockMvcRequestBuilders.get("/student/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Student responseStudent = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Student.class);
            assertThat(responseStudent).usingRecursiveComparison().isEqualTo(createStudentForTests());
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });

    }

    @Test
    void getStudentInfoNegativeTest() throws Exception {
        final long id = 1L;
        when(studentRepository.findById(eq(id))).thenReturn(Optional.of(createStudentForTests()));

        mockMvc.perform(MockMvcRequestBuilders.get("/student/{id}", id + 1)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getAllStudents() throws Exception {

        when(studentRepository.findAll()).thenReturn(createListOfStudents());

        mockMvc.perform(MockMvcRequestBuilders
                .get("/student")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            List<Student> responseStudent = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {
            });
            assertThat(responseStudent).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(createListOfStudents());
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }

    @Test
    void findByAgeBetween() throws Exception {
        int min = 20;
        int max = 25;

        List<Student> studentsBetweenAge = createListOfStudents().stream()
                .filter(student -> student.getAge() >= min && student.getAge() <= max)
                .toList();

        when(studentRepository.findByAgeBetween(anyInt(), anyInt())).thenReturn(studentsBetweenAge);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/student?min=" + min + "&max=" + max)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();

            List<Student> responseStudent = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {
            });
            assertThat(responseStudent).usingRecursiveComparison()
                    .ignoringFields()
                    .isEqualTo(studentsBetweenAge);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        });
    }

    @Test
        //Не работает, FacultyNotFoundException. Нужно как-то факультет замокать, прошу объяснить.
    void createStudent() throws Exception {
        long id = createStudentForTests().getId();

        when(studentRepository.save(any(Student.class))).thenReturn(createStudentForTests());
        when(studentRepository.findById(any())).thenReturn(Optional.of(createStudentForTests()));
        when(facultyRepository.findById(any())).thenReturn(Optional.of(createStudentForTests().getFaculty()));

        mockMvc.perform(MockMvcRequestBuilders.post("/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStudentForTests()))
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Student responseStudent = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Student.class);
            assertThat(responseStudent)
                    .usingRecursiveComparison()
                    .isEqualTo(createStudentForTests());
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });

        mockMvc.perform(MockMvcRequestBuilders.get("/student/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Student responseStudent = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Student.class);
            assertThat(responseStudent)
                    .usingRecursiveComparison()
                    .isEqualTo(createStudentForTests());
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }


    @Test
    void editStudentPositiveTest() throws Exception {
        long id = 1L;
        Student oldStudent = createStudentForTests();

        Faculty griffindor = new Faculty();
        griffindor.setId(1L);
        griffindor.setName("Гриффиндор");
        griffindor.setColor("Жёлтый");

        Student newStudent = new Student();
        newStudent.setId(2L);
        newStudent.setName("Гермиона Грейнджер");
        newStudent.setAge(23);
        newStudent.setFaculty(griffindor);


        when(studentRepository.findById(any())).thenReturn(Optional.of(oldStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(newStudent);

        mockMvc.perform(MockMvcRequestBuilders.put("/student/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newStudent))
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Student responseStudent = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Student.class);
            assertThat(responseStudent).usingRecursiveComparison().isEqualTo(newStudent);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });

    }


    @Test
    void editStudentNegativeTest() throws Exception {
        final long id = 1L;

        Faculty griffindor = new Faculty();
        griffindor.setId(1L);
        griffindor.setName("Гриффиндор");
        griffindor.setColor("Жёлтый");

        Student student = new Student();
        student.setId(1L);
        student.setName("Гермиона Грейнджер");
        student.setAge(23);
        student.setFaculty(griffindor);

        when(studentRepository.findById(eq(id))).thenReturn(Optional.of(student));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/{id}", id + 1)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void deleteStudent() throws Exception {
        final long id = 1L;

        when(studentRepository.findById(any())).thenReturn(Optional.of(createStudentForTests()));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/student/{id}", id)
                )
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value()));
    }


    @Test
    void getStudentsByAge() throws Exception {
        int age = 23;
        List<Student> studentsByAge = createListOfStudents().stream()
                .filter(student -> student.getAge() == age)
                .toList();

        when(studentRepository.findByAge(anyInt())).thenReturn(studentsByAge);

        mockMvc.perform(MockMvcRequestBuilders.get("/student")
                .param("age", String.valueOf(age))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            List<Student> responseStudents = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {
            });
            assertThat(responseStudents).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(studentsByAge);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }


    @Test
    void findFaculty() throws Exception {
        long id = 1L;
        Faculty griffindor = new Faculty();
        griffindor.setId(1L);
        griffindor.setName("Гриффиндор");
        griffindor.setColor("Жёлтый");

        Student harryPotter = new Student();
        harryPotter.setId(1L);
        harryPotter.setName("Гарри Поттер");
        harryPotter.setAge(25);
        harryPotter.setFaculty(griffindor);


        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(createStudentForTests()));

        mockMvc.perform(MockMvcRequestBuilders.get("/student/{id}/faculty", id)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Faculty responseStudents = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Faculty.class);
            assertThat(responseStudents).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(harryPotter.getFaculty());
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }

    Student createStudentForTests() {
        Faculty griffindor = new Faculty();
        griffindor.setId(1L);
        griffindor.setName("Гриффиндор");
        griffindor.setColor("Жёлтый");

        Student harryPotter = new Student();
        harryPotter.setId(1L);
        harryPotter.setName("Гарри Поттер");
        harryPotter.setAge(25);
        harryPotter.setFaculty(griffindor);

        return harryPotter;
    }

    List<Student> createListOfStudents() {
        Faculty griffindor = new Faculty();
        griffindor.setId(1L);
        griffindor.setName("Гриффиндор");
        griffindor.setColor("Жёлтый");

        Faculty slizerin = new Faculty();
        slizerin.setId(2L);
        slizerin.setName("Слизерин");
        slizerin.setColor("Зелёный");

        Student harryPotter = new Student();
        harryPotter.setId(1L);
        harryPotter.setName("Гарри Поттер");
        harryPotter.setAge(25);
        harryPotter.setFaculty(griffindor);

        Student hermioneGranger = new Student();
        hermioneGranger.setId(2L);
        hermioneGranger.setName("Гермиона Грейнджер");
        hermioneGranger.setAge(23);
        hermioneGranger.setFaculty(griffindor);

        Student drakoMalfoy = new Student();
        drakoMalfoy.setId(3L);
        drakoMalfoy.setName("Драко Малфой");
        drakoMalfoy.setAge(18);
        drakoMalfoy.setFaculty(slizerin);

        return List.of(harryPotter, hermioneGranger, drakoMalfoy);
    }
}
