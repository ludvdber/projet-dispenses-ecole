package org.isfce.pid.controller.error;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Gestionnaire global des exceptions pour les contrôleurs REST.
 * @author Ludovic
 */
@RestControllerAdvice
public class MonAdviceRestController {

	private final MessageSource messageSource;

	public MonAdviceRestController(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, String>> gestionErreurNotFound(NoSuchElementException exc) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exc.getMessage()));
	}

	@ExceptionHandler(DossierException.class)
	public ResponseEntity<Map<String, String>> gestionErreurDossier(DossierException exc) {
		return ResponseEntity.badRequest().body(Map.of("error", exc.getMessage()));
	}
	/**
	 * Permet de capturer les erreurs de validation et de retourner un objet avec les erreurs
	 * @param exc
	 * @return
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> gestionErreurTailleFichier(MaxUploadSizeExceededException exc,
			Locale locale) {
		String msg = messageSource.getMessage("err.document.uploadMaxSize", null, locale);
		return ResponseEntity.badRequest().body(Map.of("error", msg));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> gestionErreurValidationExceptions(MethodArgumentNotValidException exc) {
		Map<String, String> errors = new HashMap<>();

		exc.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		return ResponseEntity.badRequest().body(errors);
	}

}
