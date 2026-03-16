package com.example.BE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // ─── GET All Complaints (Admin) ──────────────────────────────────────────
    @GetMapping("/complaints")
    public List<Complaint> getComplaints() {
        return complaintRepository.findAll();
    }

    // ─── GET Complaints by username (Student / Teacher) ──────────────────────
    @GetMapping("/complaints/user/{username}")
    public List<Complaint> getComplaintsByUser(@PathVariable String username) {
        return complaintRepository.findByRaisedBy(username);
    }

    // ─── GET Complaints assigned to a Worker ─────────────────────────────────
    @GetMapping("/complaints/worker/{workerId}")
    public List<Complaint> getWorkerComplaints(@PathVariable Long workerId) {
        return complaintRepository.findByAssignedWorker(workerId);
    }

    // ─── GET Complaints by status ─────────────────────────────────────────────
    @GetMapping("/complaints/status/{status}")
    public List<Complaint> getComplaintsByStatus(@PathVariable String status) {
        return complaintRepository.findByStatus(status);
    }

    // ─── POST Student Complaint ───────────────────────────────────────────────
    @PostMapping("/api/student/complaint")
    public Complaint raiseStudentComplaint(@RequestBody Complaint complaint) {
        complaint.setStatus("PENDING");
        Complaint saved = complaintRepository.save(complaint);
        notifyAdmins("New student complaint from " + complaint.getRaisedByName()
                + " — " + complaint.getIssueType()
                + " at " + complaint.getLocation(), saved.getId());
        return saved;
    }

    // ─── POST Teacher Complaint ───────────────────────────────────────────────
    @PostMapping("/api/teacher/complaint")
    public Complaint raiseTeacherComplaint(@RequestBody Complaint complaint) {
        complaint.setStatus("PENDING");
        Complaint saved = complaintRepository.save(complaint);
        notifyAdmins("New teacher complaint from " + complaint.getRaisedByName()
                + " — " + complaint.getIssueType()
                + " at " + complaint.getLocation(), saved.getId());
        return saved;
    }

    // ─── POST Complaint (backward compatible) ─────────────────────────────────
    @PostMapping("/complaints")
    public Complaint raiseComplaint(@RequestBody Complaint complaint) {
        complaint.setStatus("PENDING");
        Complaint saved = complaintRepository.save(complaint);
        notifyAdmins("New complaint from " + complaint.getRaisedByName()
                + " — " + complaint.getIssueType(), saved.getId());
        return saved;
    }

    // ─── PUT Assign Worker (Admin) ─────────────────────────────────────────────
    @PutMapping("/assign-worker/{id}")
    public Complaint assignWorker(@PathVariable Long id,
                                   @RequestParam Long workerId) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        complaint.setAssignedWorker(workerId);
        complaint.setStatus("IN_PROGRESS");

        Optional<Users> workerOpt = userRepository.findById(workerId);
        workerOpt.ifPresent(w -> complaint.setAssignedWorkerName(w.getUsername()));

        Complaint saved = complaintRepository.save(complaint);

        workerOpt.ifPresent(w -> {
            Notification n = new Notification();
            n.setRecipientUsername(w.getUsername());
            n.setMessage("You have been assigned complaint #" + saved.getId()
                    + ": " + complaint.getIssueType()
                    + " at " + complaint.getLocation());
            n.setComplaintId(saved.getId());
            notificationRepository.save(n);
        });

        return saved;
    }

    // ─── PUT Worker Update Status → REVIEW ────────────────────────────────────
    // FIX: reviewImage is now in the JSON request body (not a query param)
    // A base64 image can be several MB — far too large for a URL parameter
    @PutMapping("/api/worker/update-status/{id}")
    public Complaint workerUpdateStatus(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found: " + id));

        complaint.setStatus("REVIEW");

        if (body != null && body.containsKey("reviewImage")) {
            String img = body.get("reviewImage");
            if (img != null && !img.isBlank()) {
                complaint.setReviewImage(img);
            }
        }

        Complaint saved = complaintRepository.save(complaint);

        notifyAdmins("Worker completed complaint #" + id
                + " — " + complaint.getIssueType()
                + ". Awaiting your verification.", id);

        return saved;
    }

    // ─── POST Worker Upload Proof Image (multipart alternative) ───────────────
    @PostMapping("/upload/{id}")
    public Complaint uploadProofImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(),
                uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        complaint.setReviewImageUrl(fileName);
        complaint.setStatus("REVIEW");

        Complaint saved = complaintRepository.save(complaint);
        notifyAdmins("Proof image uploaded for complaint #" + id
                + ". Ready for verification.", id);
        return saved;
    }

    // ─── PUT Admin Verify / Solve ──────────────────────────────────────────────
    @PutMapping("/api/admin/verify-complaint/{id}")
    public Complaint verifyComplaint(@PathVariable Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        complaint.setStatus("SOLVED");
        return complaintRepository.save(complaint);
    }

    @PutMapping("/solve/{id}")
    public Complaint solveComplaint(@PathVariable Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        complaint.setStatus("SOLVED");
        return complaintRepository.save(complaint);
    }

    @PutMapping("/start/{id}")
    public Complaint startWork(@PathVariable Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        complaint.setStatus("IN_PROGRESS");
        return complaintRepository.save(complaint);
    }

    // ─── Helper: notify all admin users ───────────────────────────────────────
    private void notifyAdmins(String message, Long complaintId) {
        userRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .forEach(admin -> {
                    Notification n = new Notification();
                    n.setRecipientUsername(admin.getUsername());
                    n.setMessage(message);
                    n.setComplaintId(complaintId);
                    notificationRepository.save(n);
                });
    }
}
