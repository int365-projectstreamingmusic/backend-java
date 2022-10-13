package com.application.apis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.controllers.PlaylistController;
import com.application.controllers.TrackGeneralController;
import com.application.entities.models.PlaylistModel;
import com.application.entities.models.TracksModel;

@RestController
@RequestMapping("api/public/")
public class VIsitorApi {
	private static String mapping = "api/public/";

	@Autowired
	private TrackGeneralController trackController;
	@Autowired
	private PlaylistController playlistController;

	// TRACK : Get latest X tracks.
	@GetMapping("latest")
	public ResponseEntity<List<TracksModel>> listLatestReleaseByNumber(
			@RequestParam(defaultValue = "5") int numberOfTrack) {
		return ResponseEntity.ok().body(trackController.listLatestRelease(numberOfTrack));
	}

	// PLAYLIST : Get play list by name.
	@GetMapping("playlist")
	public ResponseEntity<Page<PlaylistModel>> listPlaylist(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "0") int pageSize, @RequestParam(defaultValue = "") String searchContent) {
		return ResponseEntity.ok().body(playlistController.listAllPlaylist(page, pageSize, searchContent));
	}

	// PLAYLIST : Get latest 5 play list.
	@GetMapping("playlist/latest")
	public ResponseEntity<List<PlaylistModel>> listLatestPlaylist(
			@RequestParam(defaultValue = "5") int numberOfPlaylist) {
		return ResponseEntity.ok().body(playlistController.listLatestPlaylist(numberOfPlaylist));
	}
}