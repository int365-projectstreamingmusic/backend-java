package com.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.application.entities.models.ReportModel;

@Repository
public interface ReportsRepository extends JpaRepository<ReportModel, Integer> {

	/*@Query(nativeQuery = true, value = "")
	ReportsModel getReportByGroupId(int groupId);*/

	/*@Modifying
	@Transactional
	@Query(value = "UPDATE ReportsModel r SET r.isSolved = TRUE WHERE r.id = :id")
	void setSolved(int id);*/
}
