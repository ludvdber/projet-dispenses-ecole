package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dao.ICorrCoursDao;
import org.isfce.pid.dao.IEcoleDao;
import org.isfce.pid.dto.CoursDto;
import org.isfce.pid.dto.EcoleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST pour les écoles (base de connaissances).
 *
 * @author Ludovic
 */
@RestController
@RequestMapping(path = "/api/ecole", produces = "application/json")
public class EcoleControllerRest {

	private final IEcoleDao ecoleDao;
	private final ICorrCoursDao corrCoursDao;

	public EcoleControllerRest(IEcoleDao ecoleDao, ICorrCoursDao corrCoursDao) {
		this.ecoleDao = ecoleDao;
		this.corrCoursDao = corrCoursDao;
	}

	@GetMapping
	ResponseEntity<List<EcoleDto>> getAll() {
		return ResponseEntity.ok(ecoleDao.findAll().stream()
				.map(e -> new EcoleDto(e.getCode(), e.getNom(), e.getUrlSite()))
				.toList());
	}

	@GetMapping("/{code}/cours")
	ResponseEntity<List<CoursDto>> getCoursByEcole(@PathVariable("code") String code) {
		var cours = corrCoursDao.findByCorrespondanceEcoleCode(code).stream()
				.map(cc -> new CoursDto(cc.getCodeCours(), cc.getIntitule(), cc.getEcts()))
				.toList();
		return ResponseEntity.ok(cours);
	}
}
