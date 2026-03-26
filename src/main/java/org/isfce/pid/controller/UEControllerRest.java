package org.isfce.pid.controller;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.isfce.pid.dto.UEFullDto;
import org.isfce.pid.dto.SectionDto;
import org.isfce.pid.dto.UEDto;
import org.isfce.pid.service.UEService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Contrôleur REST pour la consultation des UE et sections.
 * @author Ludovic
 */
@RestController
@RequestMapping(path = "/api/ue/", produces = "application/json")
@PreAuthorize("hasAnyRole('ETUDIANT','ADMIN')")
@RequiredArgsConstructor
public class UEControllerRest {

	private final UEService ueService;

	private final MessageSource bundle;

	@GetMapping("sections")
	public ResponseEntity<List<SectionDto>> getListeSection() {
		return ResponseEntity.ok(ueService.getListeSections());
	}

	@GetMapping("liste")
	public ResponseEntity<List<UEDto>> getListe(@RequestParam(name = "section",required = false,defaultValue = "all") String section) {
		if ("all".equals(section))
		return ResponseEntity.ok(ueService.getListeUE());
		return ResponseEntity.ok(ueService.getListeUEBySection(section));
	}

	@GetMapping("/detail/{code}")
	public ResponseEntity<UEFullDto> getUE(@PathVariable(name = "code") String code, Locale locale) {

		if (!ueService.existUE(code))
			throw new NoSuchElementException(bundle.getMessage("err.notFound", new String[] { code }, locale));

		return ResponseEntity.ok(ueService.getUE(code)
				.orElseThrow(() -> new NoSuchElementException(
						bundle.getMessage("err.notFound", new String[] { code }, locale))));
	}
}
