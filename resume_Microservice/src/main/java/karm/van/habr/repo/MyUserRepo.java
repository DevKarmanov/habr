package karm.van.habr.repo;

import karm.van.habr.entity.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public interface MyUserRepo extends JpaRepository<MyUser, Long> {
    Optional<MyUser> findByName(String name);

    Boolean existsByName(String name);
    Boolean existsByEmail(String email);

    Optional<MyUser> findByEmail(String email);

    List<MyUser> findAllByIsEnableFalseAndUnlockAtBefore(LocalDateTime unlockAt);


    @Modifying
    @Query("UPDATE MyUser user " +
            "SET user.isEnable = :enable " +
            "WHERE user.id = :userId")
    void updateUserStatus(@Param("userId") Long userId, @Param("enable") boolean enable);
}
