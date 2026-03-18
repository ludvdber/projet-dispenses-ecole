package org.isfce.pid.ue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.isfce.pid.dto.AcquisDto;
import org.isfce.pid.dto.AcquisFullDto;
import org.isfce.pid.dto.UEFullDto;
import org.isfce.pid.dto.UEDto;
import org.isfce.pid.mapper.UEMapper;
import org.isfce.pid.model.Acquis;
import org.isfce.pid.model.Acquis.IdAcquis;
import org.isfce.pid.model.UE;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests du mapping UE ↔ DTO via MapStruct.
 *
 * @author Ludovic
 */
@SpringBootTest
@ActiveProfiles("testU")
public class TestUeDto {
	@Autowired
	UEMapper mapper;

	@Test
	void testAcquis() {
		AcquisDto aq1D = new AcquisDto("IPAP", 1, "TXT", 20);
		Acquis aq2 = mapper.toAcquis(aq1D);
		Acquis aq1 = new Acquis(new IdAcquis("IPAP", 1), null, "TXT", 20);
		assertEquals(aq1, aq2);
	}

	@Test
	void testListAcquis() {
		// liste Dto
		AcquisDto aq1D = new AcquisDto("IPAP", 1, "TXT1", 10);
		AcquisDto aq2D = new AcquisDto("IPAP", 2, "TXT2", 20);
		Acquis aq1 = new Acquis(new IdAcquis("IPAP", 1), null, "TXT1", 10);
		Acquis aq2 = new Acquis(new IdAcquis("IPAP", 2), null, "TXT2", 20);

		List<AcquisDto> listeDto = new ArrayList<>();
		listeDto.add(aq1D);
		listeDto.add(aq2D);
		// mapper sur liste Acquis
		List<Acquis> liste = mapper.toListAcquis(listeDto);
		// Vérification
		assertEquals(2, liste.size());
		assertEquals(aq1, liste.get(0));
		assertEquals(aq2, liste.get(1));
	}

	@Test
	void testUEDto() {
		UE pid = UE.builder().code("IPID").ects(9).nbPeriodes(100).nom("PROJET D’INTEGRATION DE DEVELOPPEMENT")
				.prgm("prgm").ref("ABCD").build();
		UEDto pidLazyDto = mapper.toUELazyDto(pid);
		assertEquals("IPID", pidLazyDto.code());
		assertEquals(9, pidLazyDto.ects());
		assertEquals(100, pidLazyDto.nbPeriodes());
		assertEquals("PROJET D’INTEGRATION DE DEVELOPPEMENT", pidLazyDto.nom());
		assertEquals("prgm", pidLazyDto.prgm());
		assertEquals("ABCD", pidLazyDto.ref());

		// TestDTO to UE

		UE pidLazy = mapper.fromUEDto(pidLazyDto);
		assertEquals(pid, pidLazy);
		assertNotNull(pidLazy.getAcquis());
		assertEquals(0, pidLazy.getAcquis().size());

		// Test avec Acquis
		Acquis aq1 = new Acquis(new IdAcquis("IPID", 1), null, "TXT1", 10);
		Acquis aq2 = new Acquis(new IdAcquis("IPID", 2), null, "TXT2", 20);
		pid.getAcquis().add(aq1);
		pid.getAcquis().add(aq2);
		
		//vers DtoFull
		UEFullDto pidDto=mapper.toUEFullDto(pid);
		//
		assertEquals("IPID", pidDto.code());
		assertEquals(9, pidDto.ects());
		assertEquals(100, pidDto.nbPeriodes());
		assertEquals("PROJET D’INTEGRATION DE DEVELOPPEMENT", pidDto.nom());
		assertEquals("prgm", pidDto.prgm());
		assertEquals("ABCD", pidDto.ref());
		assertEquals(2,pidDto.acquis().size());
		//vérifie si les acquis sont bien des AcquisDto
		assertEquals(AcquisFullDto.class,pidDto.acquis().get(0).getClass());

		//Test DtoFull ==> UE
		UE pid2=mapper.fromUEFullDto(pidDto);
		
		assertEquals(pid, pid2);
		assertEquals(pid.getAcquis(),pid2.getAcquis());
	}

}
