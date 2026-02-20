package org.isfce.pid.service;

import java.util.List;
import java.util.Optional;

import org.isfce.pid.dao.SectionDao;
import org.isfce.pid.dao.UeDao;
import org.isfce.pid.dto.UEFullDto;
import org.isfce.pid.dto.SectionDto;
import org.isfce.pid.dto.UEDto;
import org.isfce.pid.mapper.UEMapper;
import org.isfce.pid.model.UE;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// L'analyse null d'Eclipse génère des faux positifs sur les retours de Spring Data JPA
// (save(), findById()…) dont les @NonNull ne sont pas toujours lus depuis le classpath.
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class UEService {

	private final UeDao daoUE;
	
	private final SectionDao daoSection;
	
	private final UEMapper mapper;

	public List<UE> getListe() {
		return daoUE.findAll();
	}

	// @Transactional
	public Optional<UEFullDto> getUE(String id) {
		var oUe = daoUE.findById(id);
		//oUe.ifPresent(ue -> ue.getAcquis());
		if (oUe.isEmpty())return Optional.empty();
		return Optional.ofNullable(mapper.toUEFullDto(daoUE.findById(id).get()));

	}
	
	//sauve une nouvelle UE à oartir d'un FullDTO
	public UEFullDto addUE(@Valid UEFullDto ueAcquisDto) {
		return mapper.toUEFullDto( daoUE.save(mapper.fromUEFullDto(ueAcquisDto)));
	}

	public boolean existUE(String code) {
		return daoUE.existsById(code);
	}

	public void deleteUE(String code) {
		daoUE.deleteById(code);
	}

	/**
	 * Retourne la liste des UE sans les acquis (via un DTO)
	 * @return
	 */
	public List<UEDto> getListeUE() {
		//return mapper.toListUEDto(daoUE.findAll());
		 return daoUE.findAllUE_Dto();
	}
	
	/**
	 * Retourne la liste des UE sans les acquis (via un DTO)
	 * @return
	 */
	public List<UEDto> getListeUEBySection(String section) {
		 return daoUE.findAllUEBySection_Dto(section);
	}
	@Cacheable
	public List<SectionDto> getListeSections(){
		return daoSection.findAllSection_Dto();
	}

}
