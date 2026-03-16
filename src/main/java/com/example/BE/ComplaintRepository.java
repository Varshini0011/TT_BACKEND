package com.example.BE;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // raisedBy is now a String (username)
    List<Complaint> findByRaisedBy(String raisedBy);

    // Complaints assigned to a specific worker
    List<Complaint> findByAssignedWorker(Long assignedWorker);

    // Filter by status
    List<Complaint> findByStatus(String status);
}
