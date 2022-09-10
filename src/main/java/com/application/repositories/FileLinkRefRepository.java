package com.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.application.entities.models.FileLinkRefModel;
import com.application.entities.models.FileTypeModel;

@Repository
public interface FileLinkRefRepository extends JpaRepository<FileLinkRefModel, String> {

	// OK!
	// findByTargetRefAndTypeId
	@Query(value = "SELECT f FROM FileLinkRefModel f WHERE f.targetRef = :targetRef AND f.fileType = :fileType ")
	FileLinkRefModel findByTargetRefAndTypeId(int targetRef, FileTypeModel fileType);
	
}
