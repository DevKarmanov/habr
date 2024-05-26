package karm.van.habr.repo;

import karm.van.habr.entity.LikedResume;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepo extends JpaRepository<LikedResume,Long> {
    void deleteByResume(Resume resume);

   Optional<List<LikedResume>> findByUser(MyUser user);
}
