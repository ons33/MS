package tn.esprit.candidatservice.candidat.repository;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import tn.esprit.candidatservice.candidat.dto.JobDTO;

import java.util.List;

@FeignClient(name = "job-service", url = "http://localhost:8082")
public interface JobClient {
    @RequestMapping("/api/jobs")
    public List<JobDTO> findAll();
    @RequestMapping("/api/jobs/{id}")
    public JobDTO findById(@PathVariable int id);
}
