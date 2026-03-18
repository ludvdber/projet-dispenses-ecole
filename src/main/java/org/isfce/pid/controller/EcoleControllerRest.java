package org.isfce.pid.controller;

import java.util.List;

import org.isfce.pid.dto.CoursDto;
import org.isfce.pid.dto.EcoleDto;
import org.isfce.pid.service.EcoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Contrôleur REST pour les écoles (base de connaissances).
 *
 * @author Ludovic
 */
@RestController
@RequestMapping(path = "/api/ecole", produces = "application/json")
@PreAuthorize("hasAnyRole('ETUDIANT','ADMIN')")
@RequiredArgsConstructor
public class EcoleControllerRest {

	private final EcoleService ecoleService;

	@GetMapping
	ResponseEntity<List<EcoleDto>> getAll() {
		return ResponseEntity.ok(ecoleService.getAllEcoles());
	}

	@GetMapping("/{code}/cours")
	ResponseEntity<List<CoursDto>> getCoursByEcole(@PathVariable("code") String code) {
		return ResponseEntity.ok(ecoleService.getCoursByEcole(code));
	}
}
