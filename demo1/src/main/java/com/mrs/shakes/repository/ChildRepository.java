package com.mrs.shakes.repository;

import com.mrs.shakes.entity.Child;
import com.mrs.shakes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {
    
    // 특정 부모(Member)에게 등록된 모든 자녀 목록을 가져오는 메서드
    // 부모 객체 자체를 넘겨서 조회합니다.
    List<Child> findByParent(User parent);

    // 필요하다면 부모의 email을 통해 자녀 목록을 가져올 수도 있습니다.
    // List<Child> findByParentEmail(String email);
}
