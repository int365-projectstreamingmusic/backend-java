package com.application.controllers;

import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.application.entities.models.PlayHistoryModel;
import com.application.entities.models.UserAccountModel;
import com.application.exceptons.ExceptionFoundation;
import com.application.exceptons.ExceptionResponseModel.EXCEPTION_CODES;
import com.application.repositories.PlayHistoryRepository;
import com.application.repositories.UserAccountRepository;
import com.application.utilities.JwtTokenUtills;

@Service
public class PlayHistoryController {

	@Autowired
	private PlayHistoryRepository playHistoryRepository;
	@Autowired
	private UserAccountRepository userAccountRepository;

	private static int maxHistoryPageSize = 250;
	private static int defaultHistoryPageSize = 50;

	// OK!
	// GetMyHistory
	public Page<PlayHistoryModel> getMyHistory(int page, int pageSize, String searchContent,
			HttpServletRequest request) {
		if (page < 0) {
			page = 0;
		}
		if (pageSize < 1 || pageSize > maxHistoryPageSize) {
			pageSize = defaultHistoryPageSize;
		}

		UserAccountModel requestedBy = userAccountRepository
				.findByUsername(JwtTokenUtills.getUserNameFromToken(request));
		if (requestedBy == null) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND,
					"[ USER_ACCOUNT_NOT_FOUND ] This user does not exist in our database.");
		}

		Pageable sendPageRequest = PageRequest.of(page, pageSize);
		Page<PlayHistoryModel> result;

		if (searchContent == "") {
			result = playHistoryRepository.listHistoryByUserId(requestedBy.getAccountId(), sendPageRequest);
		} else {
			result = playHistoryRepository.findHistoryByUserIdAndSearchName(requestedBy.getAccountId(), searchContent,
					sendPageRequest);
		}

		return result;
	}

	// OK!
	// GetRecordsByUserIdAndTrackId
	public PlayHistoryModel getRecordsByUserIdAndTrackId(int trackId, HttpServletRequest request) {
		UserAccountModel requestedBy = userAccountRepository
				.findByUsername(JwtTokenUtills.getUserNameFromToken(request));
		if (requestedBy == null) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND,
					"[ USER_ACCOUNT_NOT_FOUND ] This user does not exist in our database.");
		}

		PlayHistoryModel playHistoryModel = playHistoryRepository
				.findRecordByUserIdAndTrackId(requestedBy.getAccountId(), trackId);
		if (playHistoryModel == null) {
			throw new ExceptionFoundation(EXCEPTION_CODES.BROWSE_NO_RECORD_EXISTS, HttpStatus.NOT_FOUND,
					"[ BROWSE_NO_RECORD_EXISTS ] This record does not exist.");
		}

		return playHistoryModel;

	}

	// OK!
	// AUTOMATION METHOD
	// InsertNewHistoryByUserId
	public void insertNewHistoryByUserId(int userId, int trackId) {

		if (playHistoryRepository.isExistedRecord(userId, trackId) == 1) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_SAVE_REJECTED, HttpStatus.I_AM_A_TEAPOT,
					"[ USER_SAVE_REJECTED ] This record is already exist.");
		}
		playHistoryRepository.insertNewPlayHistory(userId, trackId,
				new Timestamp(System.currentTimeMillis()).toString());
	}

	// OK!
	// ClearHistory
	public void clearHistory(HttpServletRequest request) {
		UserAccountModel requestedBy = userAccountRepository
				.findByUsername(JwtTokenUtills.getUserNameFromToken(request));
		if (requestedBy == null) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND,
					"[ USER_ACCOUNT_NOT_FOUND ] This user does not exist in our database.");
		}

		if (playHistoryRepository.hasAtLeastOneRecord(requestedBy.getAccountId()) == 1) {
			playHistoryRepository.deleteAllByUserAccountId(requestedBy.getAccountId());
		} else {
			throw new ExceptionFoundation(EXCEPTION_CODES.RECORD_ALREADY_GONE, HttpStatus.I_AM_A_TEAPOT,
					"[ DELETE_ALREADY_GONE ] This user has no history, no need to delete anything.");
		}

	}

	// OK!
	// ClearHistoryInThePassHoures
	public void clearHistoryInThePassHoures(HttpServletRequest request, int inTheLastXMinute) {
		UserAccountModel requestedBy = userAccountRepository
				.findByUsername(JwtTokenUtills.getUserNameFromToken(request));
		if (requestedBy == null) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND,
					"[ USER_ACCOUNT_NOT_FOUND ] This user does not exist in our database.");
		}

		Timestamp targetAfterThisTime = new Timestamp(System.currentTimeMillis() - (inTheLastXMinute * 1000));

		if (playHistoryRepository.hasAtLeastOneRecordAfterTimeRange(requestedBy.getAccountId(),
				targetAfterThisTime) == 0) {
			throw new ExceptionFoundation(EXCEPTION_CODES.RECORD_ALREADY_GONE, HttpStatus.I_AM_A_TEAPOT,
					"[ DELETE_ALREADY_GONE ] This user has no history in the past " + inTheLastXMinute
							+ " minutes, no need to delete anything.");
		} else {
			playHistoryRepository.deleteAllByUserAccountIdAndTimeRange(inTheLastXMinute, targetAfterThisTime);
		}

	}

	// OK!
	// DeleteRecordById
	public void deleteRecordById(int historyId, HttpServletRequest request) {
		UserAccountModel requestedBy = userAccountRepository
				.findByUsername(JwtTokenUtills.getUserNameFromToken(request));
		if (requestedBy == null) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND,
					"[ USER_ACCOUNT_NOT_FOUND ] This user does not exist in our database.");
		}

		PlayHistoryModel targetHistory = playHistoryRepository.findById(historyId)
				.orElseThrow(() -> new ExceptionFoundation(EXCEPTION_CODES.RECORD_ALREADY_GONE,
						HttpStatus.I_AM_A_TEAPOT, "[ DELETE_ALREADY_GONE ] No history of this Id."));

		if (targetHistory.getUserAccount().getAccountId() != requestedBy.getAccountId()) {
			throw new ExceptionFoundation(EXCEPTION_CODES.AUTHEN_NOT_THE_OWNER, HttpStatus.I_AM_A_TEAPOT,
					"[ AUTHEN_NOT_THE_OWNER ] This user is not the owner of this record, and is not allowed to commit change to this record.");
		} else {
			playHistoryRepository.deleteById(historyId);
		}

	}

	// OK!
	// DeleteRecordByUserIdAndTrackId
	public void deleteRecordByUserIdAndTrackId(int trackId, HttpServletRequest request) {
		UserAccountModel requestedBy = userAccountRepository
				.findByUsername(JwtTokenUtills.getUserNameFromToken(request));
		if (requestedBy == null) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND,
					"[ USER_ACCOUNT_NOT_FOUND ] This user does not exist in our database.");
		}

		if (playHistoryRepository.isExistedRecord(requestedBy.getAccountId(), trackId) == 0) {
			throw new ExceptionFoundation(EXCEPTION_CODES.RECORD_ALREADY_GONE, HttpStatus.I_AM_A_TEAPOT,
					"[ DELETE_ALREADY_GONE ] This user has no history, no need to delete anything.");
		} else {
			playHistoryRepository.deleteByUserIdAndTrackId(requestedBy.getAccountId(), trackId);
		}

	}

	// OK!
	// AUTIMATION METHOD
	// CheckAndUpdateRepeatedHistoryByUserToken
	public void checkAndUpdateRepeatedHistoryByUserToken(int trackId, HttpServletRequest request) {
		UserAccountModel requestedBy = userAccountRepository
				.findByUsername(JwtTokenUtills.getUserNameFromToken(request));
		if (requestedBy == null) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND,
					"[ USER_ACCOUNT_NOT_FOUND ] This user does not exist in our database.");
		}

		if (playHistoryRepository.isExistedRecord(requestedBy.getAccountId(), trackId) == 0) {
			throw new ExceptionFoundation(EXCEPTION_CODES.BROWSE_NO_RECORD_EXISTS, HttpStatus.NOT_FOUND,
					"[ BROWSE_NO_RECORD_EXISTS ] This record does not exist in the database.");
		} else {
			playHistoryRepository.updateTimeStamp(new Timestamp(System.currentTimeMillis()), requestedBy.getAccountId(),
					trackId);
		}

	}

	// OK!
	// AUTIMATION METHOD
	// CheckAndUpdateRepeatedHistoryByUserId
	public void checkAndUpdateRepeatedHistoryByUserId(int userId, int trackId) {
		if (userAccountRepository.existsByAccountId(userId) != 1) {
			throw new ExceptionFoundation(EXCEPTION_CODES.USER_ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND,
					"[ USER_ACCOUNT_NOT_FOUND ] This user does not exist in our database.");
		}

		if (playHistoryRepository.isExistedRecord(userId, trackId) == 0) {
			throw new ExceptionFoundation(EXCEPTION_CODES.BROWSE_NO_RECORD_EXISTS, HttpStatus.NOT_FOUND,
					"[ BROWSE_NO_RECORD_EXISTS ] This record does not exist in the database.");
		} else {
			playHistoryRepository.updateTimeStamp(new Timestamp(System.currentTimeMillis()), userId, trackId);
		}

	}

}