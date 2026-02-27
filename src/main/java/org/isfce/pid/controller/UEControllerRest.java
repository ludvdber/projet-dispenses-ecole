package org.isfce.pid.controller;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.isfce.pid.controller.error.DuplicateException;
import org.isfce.pid.dto.UEFullDto;
import org.isfce.pid.dto.SectionDto;
import org.isfce.pid.dto.UEDto;
import org.isfce.pid.service.UEService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/api/ue/", produces = "application/json")
@Slf4j
@CrossOrigin("*")
public class UEControllerRest {

	private UEService ueService;

	private MessageSource bundle;

	public UEControllerRest(UEService ueService, MessageSource bundle) {
		this.ueService = ueService;
		this.bundle = bundle;
	}
	
	@GetMapping("sections")
	ResponseEntity<List<SectionDto>> getListeSection() {
		return ResponseEntity.ok(ueService.getListeSections());
	}

	@GetMapping("liste")
	ResponseEntity<List<UEDto>> getListe(@RequestParam(name = "section",required = false,defaultValue = "all") String section) {
		if ("all".equals(section))
		return ResponseEntity.ok(ueService.getListeUE());
		return ResponseEntity.ok(ueService.getListeUEBySection(section));
	}

	@GetMapping("/detail/{code}")
	ResponseEntity<UEFullDto> getUE(@PathVariable(name = "code") String code, Locale locale) {

		if (!ueService.existUE(code))
			throw new NoSuchElementException(bundle.getMessage("err.notFound", new String[] { code }, locale));

		return ResponseEntity.ok(ueService.getUE(code).get());
	}

	@PostMapping(path = "add", consumes = "application/json")
	ResponseEntity<UEFullDto> addUEPost(@Valid @RequestBody UEFullDto ue, Locale locale) {
		if (ueService.existUE(ue.getCode()))
			throw new DuplicateException(bundle.getMessage("err.doublon", new String[] { "UE" }, locale), ue.getCode());

		ue = ueService.addUE(ue);
		log.debug("Ajout d'une UE: " + ue);
		return new ResponseEntity<>(ue, HttpStatus.CREATED);
	}
	
	@DeleteMapping(path = "{code}/delete")
	ResponseEntity<String> deleteUE(@PathVariable(value = "code") String code) {
		if (!ueService.existUE(code))
			return new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
		ueService.deleteUE(code);
		return ResponseEntity.ok(code);
	}
	
	
}
