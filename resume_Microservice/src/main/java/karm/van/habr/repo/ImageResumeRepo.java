package karm.van.habr.repo;

import karm.van.habr.entity.Complaint;
import karm.van.habr.entity.ImageResume;
import karm.van.habr.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ImageResumeRepo extends JpaRepository<karm.van.habr.entity.ImageResume,Long> {
    List<ImageResume> findByResume(Resume resume);

    List<ImageResume> findByComplaint(Complaint complaint);

    void deleteAllByComplaint(Complaint complaint);

    void deleteAllByResume(Resume resume);
}
