package org.isfce.pid.dao;

import org.isfce.pid.model.Acquis;
import org.isfce.pid.model.Acquis.IdAcquis;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO Spring Data JPA pour l'entité Acquis.
 * @author Ludovic
 */
public interface AcquisDao extends JpaRepository<Acquis,IdAcquis>
{
	int countByIdFkUE(String codeUE);

}
