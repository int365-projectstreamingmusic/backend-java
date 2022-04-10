package naturegecko.jingjok.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.SneakyThrows;
import naturegecko.jingjok.configurations.MinioConfig;
import naturegecko.jingjok.exceptions.ExceptionFoundation;
import naturegecko.jingjok.exceptions.ExceptionResponseModel.EXCEPTION_CODES;
import naturegecko.jingjok.utilities.NameGeneratorUtill;

@Service
public class MinioStorageService {

	private static final String EXTENTION_IMAGE = ".jpg";
	private static final String EXTENTION_TRACKS = ".mp3";

	private final MinioClient minioClient;
	private final MinioConfig minioConfig;

	@Value("${minio.buckek-name}")
	private String bucketname;

	@Value("${minio.maximunfilesize}")
	private long maximumFileSize;

	public MinioStorageService(MinioClient minioClient, MinioConfig minioConfig) {
		this.minioClient = minioClient;
		this.minioConfig = minioConfig;
	}

	// Ping to bucket if it exist.
	public boolean pingBucket(String bucketName) {
		try {
			return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		} catch (Exception ecc) {
			throw new ExceptionFoundation(EXCEPTION_CODES.CORE_METHOD_FAILED,
					"[ ERROR ] Method \'Ping Bucker\' failed. \n Reason : " + ecc.getMessage());
		}
	}

	// Ping if the target object is exist.
	@SneakyThrows
	public StatObjectResponse getStatObject(String bucketName, String objectName) {
		if (!pingBucket(bucketName)) {
			throw new ExceptionFoundation(EXCEPTION_CODES.CORE_METHOD_FAILED,
					"[ ERROR ] The bucket name " + bucketname + " is not reachable.");
		}
		StatObjectResponse stat = minioClient
				.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
		return stat;
	}

	// Track retrieval.
	public InputStream trackRetrivelService(String trackName, String requestRange, HttpServletRequest request,
			HttpServletResponse response) {
		if (!pingBucket(bucketname)) {
			throw new ExceptionFoundation(EXCEPTION_CODES.CORE_MINIO_NOT_FOUND,
					"[ ERROR ] The bucket name " + bucketname + " is not reachable.");
		}
		StatObjectResponse statObject = getStatObject(bucketname, trackName);
		if (statObject != null && statObject.size() > 0) {
			Map<String, String> requestExtraHeader = new HashMap<String, String>();
			requestExtraHeader.put("Range", requestRange);
			try {
				InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketname)
						.extraHeaders(requestExtraHeader).object(trackName).build());
				response.addHeader("Content-Length", "72517965");
				response.addHeader("Content-Range", "bytes 52396032-72517964/72517965");
				response.addHeader("LEL", requestRange);
				return stream;
			} catch (Exception ecc) {
				throw new ExceptionFoundation(EXCEPTION_CODES.CORE_METHOD_FAILED,
						"[ ERROR ] Method \'Ping Bucket\' failed. \n Reason : " + ecc.getMessage());
			}
		} else {
			throw new ExceptionFoundation(EXCEPTION_CODES.CORE_MINIO_NOT_FOUND,
					"[ ERROR ] The track name " + trackName + " is not reachable.");
		}
	}

	// Track retrieval by byte range.
	public InputStream trackRetrivelByByteRangeService(String trackNameAndLocation, long start, long end) {
		if (!pingBucket(bucketname)) {
			throw new ExceptionFoundation(EXCEPTION_CODES.CORE_MINIO_NOT_FOUND,
					"[ ERROR ] The bucket name " + bucketname + " is not reachable.");
		}
		StatObjectResponse statObject = getStatObject(bucketname, trackNameAndLocation);
		if (statObject == null || statObject.size() <= 0) {
			throw new ExceptionFoundation(EXCEPTION_CODES.CORE_MINIO_NOT_FOUND,
					"[ ERROR ] The track name in location " + trackNameAndLocation + " is not reachable.");
		}
		Map<String, String> requestExtraHeader = new HashMap<String, String>();
		requestExtraHeader.put("Range", "bytes=" + start + "-" + end);
		try {
			InputStream stream = minioClient
					.getObject(GetObjectArgs.builder().bucket(bucketname).extraHeaders(requestExtraHeader).build());
			return stream;
		} catch (Exception ecc) {
			throw new ExceptionFoundation(EXCEPTION_CODES.CORE_METHOD_FAILED,
					"[ ERROR ] Method \'Ping Bucket\' failed. \n Reason : " + ecc.getMessage());
		}
	}

	/*
	 * 
	 * 
	 * 
	 */

	/*
	 * ####### # dwadawdawddawww
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	// Image upload
	public String uploadImageToStorage(InputStream image, String destination) {
		try {
			String imageName = NameGeneratorUtill.generateImageName() + EXTENTION_IMAGE;
			minioClient.putObject(PutObjectArgs.builder().bucket(bucketname).object(destination + imageName)
					.stream(image, -1, maximumFileSize).contentType("image/jpg").build());
			return imageName;
		} catch (Exception ex) {
			throw new ExceptionFoundation(EXCEPTION_CODES.SAVE_FILE_FAILED,
					"[ ERROR ] File save failed with known reason : " + ex.getMessage());
		}
	}

	// Track upload
	public String uploadMusicToStorage(InputStream trackFile, String destination) {
		try {
			String trackName = NameGeneratorUtill.generateTrackName() + EXTENTION_TRACKS;
			minioClient.putObject(PutObjectArgs.builder().bucket(bucketname).object(destination + trackName)
					.stream(trackFile, -1, maximumFileSize).contentType("audio/mpeg").build());
			return trackName;
		} catch (Exception ex) {
			throw new ExceptionFoundation(EXCEPTION_CODES.SAVE_FILE_FAILED,
					"[ ERROR ] File save failed with known reason : " + ex.getMessage());
		}
	}

	public List<String> listAllBuckets() {
		try {
			List<Bucket> bucketList = minioClient.listBuckets();
			List<String> bucketNameList = new ArrayList<>();
			for (Bucket bucket : bucketList) {
				bucketNameList.add(bucket.name());
			}
			return bucketNameList;
		} catch (Exception ex) {
			throw new ExceptionFoundation(EXCEPTION_CODES.CORE_INIT_FAILED,
					"[ FAILED ] BUcket might not exist : " + ex.getMessage());
		}
	}

	// Upload to MINIO
	public void uploadToStorage(MultipartFile file, String destination, String fileName) {
		try {
			InputStream inputStream = new ByteArrayInputStream(file.getBytes());
			minioClient.putObject(PutObjectArgs.builder().bucket(this.bucketname).object(destination + fileName)
					.stream(inputStream, -1, maximumFileSize).contentType("image/jpg").build());
		} catch (Exception e) {
			throw new ExceptionFoundation(EXCEPTION_CODES.SAVE_FILE_FAILED, e.getMessage());
		}
	}

	// Call by object name
	// It will generate the link to the resource.
	@SneakyThrows
	public String getObjectAccessUrl(String directory, String fileName) {
		// String destinationUrl = "";
		String destinationUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET)
				.bucket("naturegeckogroup").object(directory + fileName).expiry(60, TimeUnit.SECONDS).build());
		return destinationUrl;
	}

}
