package karm.van.habr.repo;

import karm.van.habr.entity.LikedResume;
import karm.van.habr.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepo extends JpaRepository<LikedResume,Long> {
    void deleteByResume(Resume resume);
}
