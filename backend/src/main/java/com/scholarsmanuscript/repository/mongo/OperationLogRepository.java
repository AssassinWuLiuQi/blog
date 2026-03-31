package com.scholarsmanuscript.repository.mongo;

import com.scholarsmanuscript.entity.mongo.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends MongoRepository<OperationLog, String> {

    Page<OperationLog> findByUsername(String username, Pageable pageable);

    Page<OperationLog> findBySuccess(Boolean success, Pageable pageable);

    Page<OperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
