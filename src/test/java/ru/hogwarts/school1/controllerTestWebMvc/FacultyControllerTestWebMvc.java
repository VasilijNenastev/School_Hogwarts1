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
import ru.hogwarts.school1.controller.FacultyController;
import ru.hogwarts.school1.model.Faculty;
import ru.hogwarts.school1.model.Student;
import ru.hogwarts.school1.repositories.FacultyRepository;
import ru.hogwarts.school1.repositories.StudentRepository;
import ru.hogwarts.school1.service.FacultyService;
import ru.hogwarts.school1.service.StudentService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = FacultyController.class)
public class FacultyControllerTestWebMvc {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private FacultyRepository facultyRepository;
    @MockBean
    private StudentRepository studentRepository;
    @SpyBean
    private FacultyService facultyService;
    @SpyBean
    private StudentService studentService;


    @Test
    void getFacultyPositiveTest() throws Exception {

        final long id = 1L;
        final String name = "Когтевран";
        final String color = "Пурпурный";

        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColor(color);

        when(facultyRepository.findById(any())).thenReturn(Optional.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Faculty responseFaculty = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Faculty.class);
            assertThat(responseFaculty).usingRecursiveComparison().isEqualTo(faculty);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }

    @Test
    void getFacultyNegativeTest() throws Exception {

        final long id = 1L;
        final String name = "Когтевран";
        final String color = "Пурпурный";

        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColor(color);

        when(facultyRepository.findById(eq(id))).thenReturn(Optional.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/{id}", id + 1)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        });
    }

    @Test
    void getAllFaculties() throws Exception {

        final long idOfRavenclaw = 4L;
        final String nameOfRavenclaw = "Когтевран";
        final String colorOfRavenclaw = "Пурпурный";

        final long idOfSlizerin = 2L;
        final String nameOfSlizerin = "Слизерин";
        final String colorOfSlizerin = "Зелёный";

        Faculty ravenclawFaculty = new Faculty();
        ravenclawFaculty.setId(idOfRavenclaw);
        ravenclawFaculty.setName(nameOfRavenclaw);
        ravenclawFaculty.setColor(colorOfRavenclaw);

        Faculty slizerinFaculty = new Faculty();
        slizerinFaculty.setId(idOfSlizerin);
        slizerinFaculty.setName(nameOfSlizerin);
        slizerinFaculty.setColor(colorOfSlizerin);

        List<Faculty> faculties = List.of(ravenclawFaculty, slizerinFaculty);

        when(facultyRepository.findAll()).thenReturn(faculties);

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            List<Faculty> responseFaculties = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {
            });
            assertThat(responseFaculties).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(faculties);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }

    @Test
    void createFaculty() throws Exception {
        final long id = 1L;
        final String name = "Гриффиндор";
        final String color = "Жёлтый";

        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColor(color);

        when(facultyRepository.save(any(Faculty.class))).thenReturn(faculty);
        when(facultyRepository.findById(any())).thenReturn(Optional.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.post("/faculty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(faculty))
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Faculty responseFaculty = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Faculty.class);
            assertThat(responseFaculty).usingRecursiveComparison().isEqualTo(faculty);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Faculty responseFaculty = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Faculty.class);
            assertThat(responseFaculty).usingRecursiveComparison().isEqualTo(faculty);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }

    @Test
    void updatePositiveTest() throws Exception {
        long id = 1;
        final String newName = "Гриффиндор";
        final String newColor = "Красный";

        Faculty oldFaculty = new Faculty();
        oldFaculty.setId(id);
        oldFaculty.setName("Слизерин");
        oldFaculty.setColor("Зелёный");
        Faculty newFaculty = new Faculty();

        newFaculty.setId(id);
        newFaculty.setName(newName);
        newFaculty.setColor(newColor);

        when(facultyRepository.findById(any())).thenReturn(Optional.of(oldFaculty));
        when(facultyRepository.save(any(Faculty.class))).thenReturn(newFaculty);

        mockMvc.perform(MockMvcRequestBuilders.put("/faculty/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newFaculty))
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            Faculty responseFaculty = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), Faculty.class);
            assertThat(responseFaculty).usingRecursiveComparison().isEqualTo(newFaculty);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });

    }

    @Test
    void updateNegativeTest() throws Exception {
        final long id = 1L;
        final String name = "Когтевран";
        final String color = "Пурпурный";

        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColor(color);

        when(facultyRepository.findById(eq(id))).thenReturn(Optional.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/{id}", id + 1)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        });
    }

    @Test
    void delete() throws Exception {
        final long id = 1L;
        final String name = "Когтевран";
        final String color = "Пурпурный";

        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColor(color);

        when(facultyRepository.findById(any())).thenReturn(Optional.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/faculty/{id}", id)
                )
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value()));
    }

    @Test
    void getFacultyByColor() throws Exception {
        final long idOfRavenclaw = 4L;
        final String nameOfRavenclaw = "Когтевран";
        final String colorOfRavenclaw = "Пурпурный";

        final long idOfSlizerin = 2L;
        final String nameOfSlizerin = "Слизерин";
        final String colorOfSlizerin = "Зелёный";

        Faculty ravenclawFaculty = new Faculty();
        ravenclawFaculty.setId(idOfRavenclaw);
        ravenclawFaculty.setName(nameOfRavenclaw);
        ravenclawFaculty.setColor(colorOfRavenclaw);

        Faculty slizerinFaculty = new Faculty();
        slizerinFaculty.setId(idOfSlizerin);
        slizerinFaculty.setName(nameOfSlizerin);
        slizerinFaculty.setColor(colorOfSlizerin);

        List<Faculty> faculties = List.of(ravenclawFaculty, slizerinFaculty);

        List<Faculty> facultiesOfGreenColor = faculties.stream()
                .filter(faculty -> faculty.getColor().equals(colorOfSlizerin))
                .toList();

        when(facultyRepository.findByColorIgnoringCase(eq("Зелёный"))).thenReturn(facultiesOfGreenColor);

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty")
                .param("color", colorOfSlizerin)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            List<Faculty> responseFaculties = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {
            });
            assertThat(responseFaculties).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(facultiesOfGreenColor);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }

    @Test
    void findByNameOrColor() throws Exception {
        String color = "Зелёный";
        String name = "Слизерин";

        final long idOfRavenclaw = 4L;
        final String nameOfRavenclaw = "Когтевран";
        final String colorOfRavenclaw = "Пурпурный";

        final long idOfSlizerin = 2L;
        final String nameOfSlizerin = "Слизерин";
        final String colorOfSlizerin = "Зелёный";

        Faculty ravenclawFaculty = new Faculty();
        ravenclawFaculty.setId(idOfRavenclaw);
        ravenclawFaculty.setName(nameOfRavenclaw);
        ravenclawFaculty.setColor(colorOfRavenclaw);

        Faculty slizerinFaculty = new Faculty();
        slizerinFaculty.setId(idOfSlizerin);
        slizerinFaculty.setName(nameOfSlizerin);
        slizerinFaculty.setColor(colorOfSlizerin);

        List<Faculty> faculties = List.of(ravenclawFaculty, slizerinFaculty);

        List<Faculty> facultiesOfColor = faculties.stream()
                .filter(faculty -> faculty.getColor().equals(colorOfSlizerin))
                .toList();
        List<Faculty> facultiesOfName = faculties.stream()
                .filter(faculty -> faculty.getColor().equals(colorOfSlizerin))
                .toList();
        when(facultyRepository.findByNameAndColorContainingIgnoreCase(any(), any())).thenReturn(facultiesOfColor, facultiesOfName);

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty")
                .param("nameOrColor", colorOfSlizerin)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            List<Faculty> responseFaculties = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {
            });
            assertThat(responseFaculties).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(facultiesOfColor);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        });
    }

    @Test
    void findStudentsOfFaculty() throws Exception {
        long id = 1L;
        final long idOfRavenclaw = 2L;
        final String nameOfRavenclaw = "Когтевран";
        final String colorOfRavenclaw = "Пурпурный";

        final long idOfSlizerin = 1L;
        final String nameOfSlizerin = "Слизерин";
        final String colorOfSlizerin = "Зелёный";

        Faculty ravenclawFaculty = new Faculty();
        ravenclawFaculty.setId(idOfRavenclaw);
        ravenclawFaculty.setName(nameOfRavenclaw);
        ravenclawFaculty.setColor(colorOfRavenclaw);

        Faculty slizerinFaculty = new Faculty();
        slizerinFaculty.setId(idOfSlizerin);
        slizerinFaculty.setName(nameOfSlizerin);
        slizerinFaculty.setColor(colorOfSlizerin);

        List<Faculty> faculties = List.of(ravenclawFaculty, slizerinFaculty);

        Student harryPotter = new Student();
        harryPotter.setId(1L);
        harryPotter.setName("Гарри Поттер");
        harryPotter.setAge(25);
        harryPotter.setFaculty(ravenclawFaculty);

        Student hermioneGranger = new Student();
        hermioneGranger.setId(2L);
        hermioneGranger.setName("Гермиона Грейнджер");
        hermioneGranger.setAge(23);
        hermioneGranger.setFaculty(ravenclawFaculty);

        Student drakoMalfoy = new Student();
        drakoMalfoy.setId(3L);
        drakoMalfoy.setName("Драко Малфой");
        drakoMalfoy.setAge(18);
        drakoMalfoy.setFaculty(slizerinFaculty);

        List<Student> students = List.of(harryPotter, hermioneGranger, drakoMalfoy);

        List<Student> expected = students.stream()
                .filter(student -> student.getFaculty().getId() == id)
                .toList();
        when(studentRepository.findByFaculty_Id(eq(id))).thenReturn(expected);
        when(facultyRepository.findById(any())).thenReturn(Optional.of(drakoMalfoy.getFaculty()));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/{id}/students", id)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> {
            MockHttpServletResponse response = result.getResponse();
            List<Student> responseStudent = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {
            });
            assertThat(responseStudent).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(expected);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        });
    }
}
