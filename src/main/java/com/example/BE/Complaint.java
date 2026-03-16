package com.example.BE;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String raisedBy;       // stores username (String)
    private String raisedByName;   // display name
    private String location;
    private String issueType;
    private String description;
    private String status;
    private Long assignedWorker;
    private String assignedWorkerName;
    private String imageUrl;
    private String reviewImageUrl;
    private LocalDateTime createdAt;

    @Column(columnDefinition = "LONGTEXT")
    private String image;

    @Column(columnDefinition = "LONGTEXT")
    private String reviewImage;

    public Complaint() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getRaisedBy() { return raisedBy; }
    public void setRaisedBy(String raisedBy) { this.raisedBy = raisedBy; }

    public String getRaisedByName() { return raisedByName; }
    public void setRaisedByName(String raisedByName) { this.raisedByName = raisedByName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAssignedWorker() { return assignedWorker; }
    public void setAssignedWorker(Long assignedWorker) { this.assignedWorker = assignedWorker; }

    public String getAssignedWorkerName() { return assignedWorkerName; }
    public void setAssignedWorkerName(String assignedWorkerName) { this.assignedWorkerName = assignedWorkerName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getReviewImageUrl() { return reviewImageUrl; }
    public void setReviewImageUrl(String reviewImageUrl) { this.reviewImageUrl = reviewImageUrl; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getReviewImage() { return reviewImage; }
    public void setReviewImage(String reviewImage) { this.reviewImage = reviewImage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
