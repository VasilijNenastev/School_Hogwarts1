package ru.hogwarts.school1.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school1.model.Avatar;
import ru.hogwarts.school1.model.Student;
import ru.hogwarts.school1.repositories.AvatarRepository;
import ru.hogwarts.school1.repositories.StudentRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@Transactional
    public class AvatarService {
        @Value("path")
        private String avatarDir;

        private final StudentRepository studentRepository;
        private final AvatarRepository avatarRepository;

        public AvatarService(StudentRepository studentRepository, AvatarRepository avatarRepository) {
            this.studentRepository = studentRepository;
            this.avatarRepository = avatarRepository;
        }

        public void uploadAvatar(Long studentId, MultipartFile avatarFile) throws IOException {
            Student student = studentRepository.getReferenceById(studentId);

            Path filePath = Path.of(avatarDir, student + "." + getExtension(avatarFile.getOriginalFilename()));
            Files.createDirectories(filePath.getParent());
            Files.deleteIfExists(filePath);

            try (
                    InputStream is = avatarFile.getInputStream();
                    OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                    BufferedInputStream bis = new BufferedInputStream(is, 1024);
                    BufferedOutputStream bos = new BufferedOutputStream(os, 1024);
            ) {
                bis.transferTo(bos);
            }

            Avatar avatar = findAvatar(studentId);
            avatar.setStudent(student);
            avatar.setFilePath(filePath.toString());
            avatar.setFileSize(avatarFile.getSize());
            avatar.setMediaType(avatarFile.getContentType());
            avatar.setData(avatarFile.getBytes());

            avatarRepository.save(avatar);
        }

        private String getExtension(String originalFilename) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }

        public Avatar findAvatar(Long studentId) {
            return avatarRepository.findByStudentId(studentId).orElse(new Avatar());
        }
}
