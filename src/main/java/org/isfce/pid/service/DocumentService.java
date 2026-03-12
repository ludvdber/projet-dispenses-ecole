package org.isfce.pid.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;

import org.isfce.pid.controller.error.DossierException;
import org.isfce.pid.dao.ICoursEtudiantDao;
import org.isfce.pid.dao.IDocumentDao;
import org.isfce.pid.dao.IDossierDao;
import org.isfce.pid.dto.DocumentDto;
import org.isfce.pid.model.CoursEtudiant;
import org.isfce.pid.model.Document;
import org.isfce.pid.model.Dossier;
import org.isfce.pid.model.EtatDossier;
import org.isfce.pid.model.TypeDoc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service métier pour l'upload et la gestion des documents.
 *
 * @author Ludovic
 */
@SuppressWarnings("null")
@Service
@Transactional
public class DocumentService {

	private final IDossierDao daoDossier;
	private final ICoursEtudiantDao daoCoursEtudiant;
	private final IDocumentDao daoDocument;
	private final Path uploadDir;
	private final long maxSize;

	public DocumentService(IDossierDao daoDossier, ICoursEtudiantDao daoCoursEtudiant,
			IDocumentDao daoDocument,
			@Value("${app.upload.dir:uploads}") String uploadDir,
			@Value("${app.upload.max-size:5242880}") long maxSize) {
		this.daoDossier = daoDossier;
		this.daoCoursEtudiant = daoCoursEtudiant;
		this.daoDocument = daoDocument;
		this.uploadDir = Paths.get(uploadDir);
		this.maxSize = maxSize;
	}

	/**
	 * Upload un document lié à un dossier ou à un cours étudiant (XOR).
	 */
	public DocumentDto uploadDocument(Long dossierId, Long coursEtudiantId,
			TypeDoc typeDoc, MultipartFile file, String username) throws DossierException {

		// Validation type MIME
		String contentType = file.getContentType();
		if (contentType == null || !contentType.equals("application/pdf")) {
			throw new DossierException("err.document.typeMime");
		}

		// Validation signature PDF (magic bytes %PDF = 0x25504446)
		try (InputStream check = file.getInputStream()) {
			byte[] header = new byte[4];
			if (check.read(header) < 4
					|| header[0] != 0x25 || header[1] != 0x50
					|| header[2] != 0x44 || header[3] != 0x46) {
				throw new DossierException("err.document.typeInvalide");
			}
		} catch (IOException e) {
			throw new DossierException("err.document.uploadFailed");
		}

		// Validation taille
		if (file.getSize() > maxSize) {
			throw new DossierException("err.document.tailleTrop");
		}

		// Validation cible (XOR dossierId / coursEtudiantId)
		if (dossierId == null && coursEtudiantId == null) {
			throw new DossierException("err.document.cibleManquante");
		}

		// Résoudre le dossier parent (pour ownership)
		Dossier dossier;
		CoursEtudiant coursEtudiant = null;

		if (coursEtudiantId != null) {
			coursEtudiant = daoCoursEtudiant.findById(coursEtudiantId)
					.orElseThrow(() -> new DossierException("err.cours.notFound"));
			dossier = coursEtudiant.getDossier();
		} else {
			dossier = daoDossier.findById(dossierId)
					.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		}

		// Vérification ownership + état
		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}
		if (dossier.getEtat() != EtatDossier.DEMANDE_EN_COURS
				&& dossier.getEtat() != EtatDossier.ATTENTE_COMPLEMENT) {
			throw new DossierException("err.cours.dossierClos");
		}

		// Stocker le fichier sur disque
		try {
			Path dossierDir = uploadDir.resolve(String.valueOf(dossier.getId()));
			Files.createDirectories(dossierDir);

			// Nettoyer le nom de fichier (protection path traversal)
			String safeName = Paths.get(file.getOriginalFilename()).getFileName().toString();
			String filename = System.currentTimeMillis() + "_" + safeName;
			Path target = dossierDir.resolve(filename);

			// Copier + calculer hash SHA-256 en une passe
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			try (InputStream is = file.getInputStream();
					DigestInputStream dis = new DigestInputStream(is, digest)) {
				Files.copy(dis, target, StandardCopyOption.REPLACE_EXISTING);
			}
			String hash = HexFormat.of().formatHex(digest.digest());

			// Chemin relatif stocké en BDD
			String cheminRelatif = dossier.getId() + "/" + filename;

			Document doc = new Document();
			if (coursEtudiant != null) {
				doc.setCoursEtudiant(coursEtudiant);
			} else {
				doc.setDossier(dossier);
			}
			doc.setTypeDoc(typeDoc);
			doc.setOriginalFilename(file.getOriginalFilename());
			doc.setCheminRelatif(cheminRelatif);
			doc.setTypeMime(contentType);
			doc.setTaille(file.getSize());
			doc.setDateDepot(LocalDateTime.now());
			doc.setHashSha256(hash);

			doc = daoDocument.save(doc);
			return toDto(doc);

		} catch (IOException | NoSuchAlgorithmException e) {
			throw new DossierException("err.document.uploadFailed");
		}
	}

	/**
	 * Soft-delete un document (positionne deletedAt).
	 */
	public DocumentDto softDeleteDocument(Long documentId, String username) throws DossierException {
		Document doc = daoDocument.findById(documentId)
				.orElseThrow(() -> new DossierException("err.document.notFound"));

		Dossier dossier = doc.getDossier() != null
				? doc.getDossier()
				: doc.getCoursEtudiant().getDossier();

		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}
		if (dossier.getEtat() != EtatDossier.DEMANDE_EN_COURS
				&& dossier.getEtat() != EtatDossier.ATTENTE_COMPLEMENT) {
			throw new DossierException("err.cours.dossierClos");
		}

		doc.setDeletedAt(LocalDateTime.now());
		deletePhysicalFile(doc);
		doc = daoDocument.save(doc);
		return toDto(doc);
	}

	/**
	 * Retourne les documents actifs d'un dossier.
	 */
	public List<DocumentDto> getDocumentsByDossier(Long dossierId, String username) throws DossierException {
		Dossier dossier = daoDossier.findById(dossierId)
				.orElseThrow(() -> new DossierException("err.dossier.notFound"));
		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}
		// Documents liés directement au dossier + ceux liés aux cours du dossier
		List<Document> docs = new ArrayList<>(
				daoDocument.findByDossierIdAndDeletedAtIsNull(dossierId));
		docs.addAll(daoDocument.findByCoursEtudiantDossierIdAndDeletedAtIsNull(dossierId));
		return docs.stream().map(this::toDto).toList();
	}

	/**
	 * Résultat du téléchargement : resource + métadonnées.
	 */
	public record DownloadResult(Resource resource, String typeMime, String originalFilename) {}

	/**
	 * Retourne une Resource + métadonnées pour télécharger un document (une seule requête BDD).
	 */
	public DownloadResult downloadDocument(Long documentId, String username) throws DossierException {
		Document doc = daoDocument.findById(documentId)
				.orElseThrow(() -> new DossierException("err.document.notFound"));

		if (doc.getDeletedAt() != null) {
			throw new DossierException("err.document.notFound");
		}

		Dossier dossier = doc.getDossier() != null
				? doc.getDossier()
				: doc.getCoursEtudiant().getDossier();

		if (!dossier.getUser().getUsername().equals(username)) {
			throw new DossierException("err.dossier.forbidden");
		}

		try {
			Path filePath = uploadDir.resolve(doc.getCheminRelatif());
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists()) {
				throw new DossierException("err.document.notFound");
			}
			return new DownloadResult(resource, doc.getTypeMime(), doc.getOriginalFilename());
		} catch (IOException e) {
			throw new DossierException("err.document.notFound");
		}
	}

	/**
	 * Supprime les fichiers physiques + enregistrements BDD de tous les documents liés à un cours.
	 */
	public void deleteAllByCoursEtudiantId(Long coursEtudiantId) {
		List<Document> docs = daoDocument.findByCoursEtudiantIdAndDeletedAtIsNull(coursEtudiantId);
		for (Document doc : docs) {
			deletePhysicalFile(doc);
		}
		daoDocument.deleteByCoursEtudiantId(coursEtudiantId);
	}

	private void deletePhysicalFile(Document doc) {
		try {
			Path filePath = uploadDir.resolve(doc.getCheminRelatif());
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			// Log silencieux — ne pas bloquer l'opération métier
		}
	}

	private DocumentDto toDto(Document d) {
		return new DocumentDto(
				d.getId(),
				d.getDossier() != null ? d.getDossier().getId() : null,
				d.getCoursEtudiant() != null ? d.getCoursEtudiant().getId() : null,
				d.getTypeDoc(),
				d.getOriginalFilename(),
				d.getTypeMime(),
				d.getTaille(),
				d.getDateDepot(),
				d.getHashSha256());
	}
}
