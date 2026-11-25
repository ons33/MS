package tn.esprit.candidatservice.candidat.service;

import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.candidatservice.candidat.dto.CandidateRequest;
import tn.esprit.candidatservice.candidat.dto.CandidateResponse;
import tn.esprit.candidatservice.candidat.dto.JobDTO;
import tn.esprit.candidatservice.candidat.entity.Address;
import tn.esprit.candidatservice.candidat.entity.Candidate;
import tn.esprit.candidatservice.candidat.mapper.CandidatMapper;
import tn.esprit.candidatservice.candidat.repository.CandidateRepository;

import tn.esprit.candidatservice.candidat.repository.JobClient;
import tn.esprit.candidatservice.common.exception.ConflictException;
import tn.esprit.candidatservice.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final AddressService addressService;


    public CandidateService(CandidateRepository candidateRepository, AddressService addressService) {
        this.candidateRepository = candidateRepository;
        this.addressService = addressService;
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> findAll() {
        return candidateRepository.findAll().stream().map(CandidatMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CandidateResponse findById(Long id) {
        return CandidatMapper.toResponse(getCandidate(id));
    }

    public CandidateResponse create(CandidateRequest request) {
        if (candidateRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A candidate with email " + request.email() + " already exists");
        }
        Candidate saved = candidateRepository.save(CandidatMapper.toEntity(request));
        return CandidatMapper.toResponse(saved);
    }

    public CandidateResponse update(Long id, CandidateRequest request) {
        Candidate candidate = getCandidate(id);
        if (candidateRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new ConflictException("A candidate with email " + request.email() + " already exists");
        }
        candidate.setFirstname(request.firstname());
        candidate.setLastname(request.lastname());
        candidate.setEmail(request.email());

        if (request.address() != null) {
            if (candidate.getAddress() != null) {
                CandidatMapper.updateEntity(candidate.getAddress(), request.address());
            } else {
                candidate.setAddress(CandidatMapper.toEntity(request.address()));
            }
        }
        return CandidatMapper.toResponse(candidateRepository.saveAndFlush(candidate));
    }

    public void delete(Long id) {
        Candidate candidate = getCandidate(id);
        candidateRepository.delete(candidate);
    }

    /** Links an existing address to a candidate (replacing any current link). */
    public CandidateResponse assignAddress(Long candidateId, Long addressId) {
        Candidate candidate = getCandidate(candidateId);
        Address address = addressService.getAddress(addressId);

        if (address.getCandidate() != null && !address.getCandidate().getId().equals(candidateId)) {
            throw new ConflictException("Address " + addressId + " is already assigned to candidate "
                    + address.getCandidate().getId());
        }
        candidate.setAddress(address);
        return CandidatMapper.toResponse(candidateRepository.saveAndFlush(candidate));
    }

    /** Unlinks the candidate's address. The address itself is kept. */
    public CandidateResponse removeAddress(Long candidateId) {
        Candidate candidate = getCandidate(candidateId);
        candidate.setAddress(null);
        return CandidatMapper.toResponse(candidateRepository.saveAndFlush(candidate));
    }

    /** Also used by the candidature module (in-process call inside the monolith). */
    public Candidate getCandidate(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", id));
    }

    @Autowired
    private JobClient jobServiceClient;
    public List<JobDTO> getJobs() {
        return jobServiceClient.findAll();
    }
    public JobDTO getJobById(int id) {
        return jobServiceClient.findById(id);
    }
    public List<JobDTO> getFavoriteJobs(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId).get();

        return candidate.getFavoriteJobs().stream()
                .map(jobServiceClient::findById)
                .collect(Collectors.toList());
    }

    public void saveFavoriteJob(Long candidateId, int jobId) {
        Candidate candidate = candidateRepository.findById(candidateId).get();

        candidate.getFavoriteJobs().add(jobId);
        candidateRepository.save(candidate);
    }

}
