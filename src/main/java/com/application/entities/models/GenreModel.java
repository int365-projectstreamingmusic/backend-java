package com.application.entities.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "genre", schema = "sitgarden")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GenreModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int genre_id;
	
	@Column(name = "genre_name")
	private String genreName;
	@Column(name = "genre_desc")
	private String genreDesc;
}
