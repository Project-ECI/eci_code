package com.eci.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eci.dao.CandidateDao;
import com.eci.dao.DistrictDao;
import com.eci.dao.ElectionDao;
import com.eci.dao.PartyDao;
import com.eci.dao.VoterDao;
import com.eci.dto.ElectionDateDto;
import com.eci.dto.ElectionResultDto;
import com.eci.entity.Candidate;
import com.eci.entity.District;
import com.eci.entity.Election;
import com.eci.entity.Party;
import com.eci.entity.Voter;

@Service
@Transactional
public class ElectionServiceImpl implements ElectionService {
	@Autowired
	private ElectionDao electionDao;

	@Autowired
	private DistrictDao districtDao;

	@Autowired
	private CandidateDao candidateDao;

	@Autowired
	private VoterDao voterDao;

	@Autowired
	private PartyDao partyDao;

	@Autowired
	private ModelMapper mapper;

	@Override
	public String addElectionDate(ElectionDateDto dto) {
		Long districtId = Long.parseLong(dto.getDistrictId());
		Optional<District> district = districtDao.findById(districtId);

		// if districtId is valid
		if (district.isPresent()) {
			Optional<Election> election = electionDao.findByDistrictId(district.get());

			// update election date
			if (election.isPresent()) {
				System.out.println(dto.getElectionDate()+"*************");
				election.get().setElectionDate(LocalDate.parse(dto.getElectionDate()));
				election.get().setDistrictId(district.get());
				electionDao.save(election.get());
				return "update election date" + election.toString();
			}
			// add new election date
			else {
				Election election1 = new Election();
				election1.setDistrictId(district.get());
				System.out.println(dto.getElectionDate()+"//////////");
				election1.setElectionDate(LocalDate.parse(dto.getElectionDate()));
				election1.setResultDeclared(false);
				electionDao.save(election1);
				return "add new election date" + election1.toString();
			}
		}
		return "Invalid District";

	}

	@Override
	public List<ElectionResultDto> getResult() {
		List<ElectionResultDto> list = new ArrayList<>();
		List<District> districtList = districtDao.findAll();

		for (District district : districtList) {
			Optional<Election> electionOpt = electionDao.findByDistrictId(district);
			if (electionOpt.isPresent()&&electionOpt.get().isResultDeclared()) {
				// Use constituency since it's the correct mapping to District in Candidate
				Optional<Candidate> topCandidateOpt = candidateDao.findTopByConstituencyOrderByVotesDesc(district);

				if (topCandidateOpt.isPresent() && topCandidateOpt.get().getParty() != null) {
					if (topCandidateOpt.get().isAccepted() || topCandidateOpt.get().isIndependent()) {
						
						Candidate topCandidate = topCandidateOpt.get();
						ElectionResultDto resultDto = new ElectionResultDto();
						resultDto.setDistrictName(district.getDistrictName());
						resultDto.setCandiateName(topCandidate.getVoterId().getFullName()); // Assuming Voter has a
																							// voterName
						resultDto.setVotes(topCandidate.getVotes());
						resultDto.setPartyName(topCandidate.getParty().getPartyName()); // Assuming Party has a partyName
						list.add(resultDto);
					}
				}
			}
			
		}

		return list;
	}

	@Override
	public List<ElectionResultDto> getResultConstituency(String voterid) {
		Long voterId = Long.parseLong(voterid);
		Optional<Voter> voter = voterDao.findById(voterId);
		Optional<Election> electionOpt = electionDao.findByDistrictId(voter.get().getDistrictId());
		List<ElectionResultDto> list = new ArrayList<ElectionResultDto>();
		if (electionOpt.isPresent() && electionOpt.get().isResultDeclared() == true) {
			List<Candidate> listOfCandidate = candidateDao.findByConstituency(voter.get().getDistrictId());

			for (Candidate candidate : listOfCandidate) {
				if (candidate.isAccepted() == true && candidate.isRejected() == false) {
					ElectionResultDto dto = new ElectionResultDto();
					if (candidate.getParty() != null) {
						Optional<Party> partyOpt = partyDao.findById(candidate.getParty().getPartyId());
						dto.setPartyName(partyOpt.get().getPartyName());
						dto.setIndependent(false);
					} else {
						dto.setIndependent(true);
						dto.setPartyName(null);
					}
					Optional<Voter> voterOpt = voterDao.findById(candidate.getVoterId().getVoterId());
					dto.setCandiateName(voterOpt.get().getFullName());
					dto.setVotes(candidate.getVotes());
					dto.setDistrictName(candidate.getConstituency().getDistrictName());
					list.add(dto);
				}
			}
			return list;
		}
		return list;
	}

	@Override
	public List<ElectionDateDto> getElectionDate() {
		List<Election> allElection = electionDao.findAll();
		List<ElectionDateDto> list = new ArrayList<ElectionDateDto>();
		for (Election election : allElection) {
			ElectionDateDto dto = new ElectionDateDto();
			dto.setDistrictId(election.getDistrictId().getDistrictName());
			dto.setElectionDate(election.getElectionDate().toString());
			list.add(dto);
		}
		return list;
	}

	@Override
	public ElectionDateDto getConstituencyElection(String voterid) {
		Long voterId = Long.parseLong(voterid);
		Optional<Voter> voter = voterDao.findById(voterId);
		if (voter.isPresent()) {
			Optional<Election> election = electionDao.findByDistrictId(voter.get().getDistrictId());
			ElectionDateDto dto = new ElectionDateDto();
			dto.setDistrictId(election.get().getDistrictId().getDistrictName());
			dto.setElectionDate(election.get().getElectionDate().toString());
			return dto;
		}
		return null;
	}

	@Override
	public String declaredResult(String districtid) {
		Long districtId = Long.parseLong(districtid);
		Optional<District> districtOpt = districtDao.findById(districtId);
		if (districtOpt.isPresent()) {
			List<Election> electionList = electionDao.findAllByDistrictId(districtOpt.get());
			if (!electionList.isEmpty()) {
				for (Election election : electionList) {
					election.setResultDeclared(true);
					electionDao.save(election);
				}
				return "Election Declared";
			}
			return "No election set for specified district";
		}
		return "District not found";
	}

	@Override
	public boolean isResultDeclared(Long districtId) {
		Optional<District> districtOpt = districtDao.findById(districtId);
		Optional<Election> electionDetailsOpt = electionDao.findByDistrictId(districtOpt.get());

		return electionDetailsOpt.get().isResultDeclared();
	}

	@Override
	public boolean isElectionDate(Long districtId) {
		Optional<District> districtOpt = districtDao.findById(districtId);
		Optional<Election> electionDetailsOpt = electionDao.findByDistrictId(districtOpt.get());

		LocalDate electionDate = electionDetailsOpt.get().getElectionDate();
		return electionDate.equals(LocalDate.now());
	}
}
