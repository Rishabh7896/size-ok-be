package com.sizeok.profile.repository;

import com.sizeok.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    List<Profile> findByUserIdOrderByPrimaryDescCreatedAtAsc(String userId);
}
