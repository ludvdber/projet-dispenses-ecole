package org.isfce.pid.service;

import java.util.List;
import java.util.Optional;

import org.isfce.pid.dao.SectionDao;
import org.isfce.pid.dao.UeDao;
import org.isfce.pid.dto.UEFullDto;
import org.isfce.pid.dto.SectionDto;
import org.isfce.pid.dto.UEDto;
import org.isfce.pid.mapper.UEMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Service de consultation des UE et sections.
 * @author Ludovic
 */
// L'analyse null d'Eclipse génère des faux positifs sur les retours de Spring Data JPA
// (save(), findById()…) dont les @NonNull ne sont pas toujours lus depuis le classpath.
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UEService {

	private final UeDao daoUE;
	
	private final SectionDao daoSection;
	
	private final UEMapper mapper;

	public Optional<UEFullDto> getUE(String id) {
		return daoUE.findById(id).map(mapper::toUEFullDto);
	}
	
	public boolean existUE(String code) {
		return daoUE.existsById(code);
	}

	/**
	 * Retourne la liste des UE sans les acquis (via un DTO)
	 * @return
	 */
	public List<UEDto> getListeUE() {
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
