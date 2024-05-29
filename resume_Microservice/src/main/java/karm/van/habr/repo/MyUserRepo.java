package karm.van.habr.repo;

import karm.van.habr.entity.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MyUserRepo extends JpaRepository<MyUser, Long> {
    Optional<MyUser> findByName(String name);

    Boolean existsByName(String name);

}
