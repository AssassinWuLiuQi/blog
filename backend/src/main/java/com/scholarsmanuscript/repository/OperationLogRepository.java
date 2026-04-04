package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Page<OperationLog> findByUsername(String username, Pageable pageable);

    Page<OperationLog> findBySuccess(Boolean success, Pageable pageable);

    Page<OperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<OperationLog> findByUsernameAndSuccess(String username, Boolean success, Pageable pageable);
}
