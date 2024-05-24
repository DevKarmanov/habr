package karm.van.habr.repo;

import karm.van.habr.entity.ImageResume;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResumeRepo extends JpaRepository<Resume,Long> {


    @Query("SELECT r from Resume " +
            "r WHERE LOWER(r.title) like LOWER(CONCAT('%', :filter, '%'))")
    Page<Resume> findByTitleContaining(String filter, Pageable pageable);
}
