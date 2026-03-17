package org.isfce.pid.service;

import java.util.List;

import org.isfce.pid.dao.ICorrCoursDao;
import org.isfce.pid.dao.IEcoleDao;
import org.isfce.pid.dto.CoursDto;
import org.isfce.pid.dto.EcoleDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

/**
 * Service de consultation des écoles et de leurs cours.
 * @author Ludovic
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class EcoleService {

	private final IEcoleDao ecoleDao;
	private final ICorrCoursDao corrCoursDao;

	/**
	 * Retourne la liste de toutes les écoles connues.
	 */
	public List<EcoleDto> getAllEcoles() {
		return ecoleDao.findAll().stream()
				.map(e -> new EcoleDto(e.getCode(), e.getNom(), e.getUrlSite()))
				.toList();
	}

	/**
	 * Retourne les cours d'une école via la table de correspondance.
	 */
	public List<CoursDto> getCoursByEcole(String codeEcole) {
		return corrCoursDao.findByCorrespondanceEcoleCode(codeEcole).stream()
				.map(cc -> new CoursDto(cc.getCodeCours(), cc.getIntitule(), cc.getEcts()))
				.toList();
	}
}
