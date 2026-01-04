package com.faceproctoring.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.faceproctoring.model.Student;
import com.faceproctoring.util.CameraHelper;
import com.faceproctoring.util.PythonBridge;
import com.faceproctoring.util.StudentDatabase;
import com.faceproctoring.util.StudentDataLoader;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class FaceRecognitionController {
	@FXML
	public ImageView imgCamera;
	@FXML
	public Label lblResult;

	// New fields for student info and sample image
	@FXML
	private Label lblStudentId;
	@FXML
	private Label lblStudentName;
	@FXML
	private Label lblClass;
	@FXML
	private Label lblRoom;
	@FXML
	private ImageView imgSample;

	private CameraHelper cameraHelper;
	private volatile boolean running = false;
	private volatile boolean firstFrameReceived = false;
	private volatile boolean isCameraReady = false;
	private volatile boolean cameraError = false;

	// Store logged-in student for verification
	private Student loggedInStudent;

	@FXML
	private Button btnStart;
	@FXML
	private Button btnRetry;
	@FXML
	private Button btnCapture;
	@FXML
	private ProgressIndicator spinner;

	public void initialize() {
		cameraHelper = new CameraHelper();
		running = false;
		isCameraReady = false;
		cameraError = false;

		// Set initial UI state
		Platform.runLater(() -> {
			try {
				// Show loading spinner, hide all buttons initially
				spinner.setVisible(true);
				btnStart.setVisible(false);
				btnCapture.setVisible(false);
				btnRetry.setVisible(false);
				lblResult.setText("Đang khởi tạo camera...");

				Image placeholder = new Image(getClass().getResourceAsStream("/images/student.png"));
				imgCamera.setImage(placeholder);

				lblStudentId.setText(StudentLoginController.USERNAME);
				String fullName = StudentDataLoader.getFullName(StudentLoginController.USERNAME);
				String clazz = StudentDataLoader.getClazz(StudentLoginController.USERNAME);
				String room = StudentDataLoader.getRoom(StudentLoginController.USERNAME);
				lblStudentName.setText(fullName);
				lblClass.setText(clazz);
				lblRoom.setText(room);

				// Create logged-in student object
				loggedInStudent = new Student(StudentLoginController.USERNAME, fullName, clazz, room);
				String faceId = StudentDataLoader.getFaceId(StudentLoginController.USERNAME);
				if (faceId != null) {
					loggedInStudent.setFaceId(faceId);
				}
			} catch (Exception e) {
				System.err.println("Lỗi hiển thị thông tin SV: " + e.getMessage());
			}
		});

		// PRE-WARM THE CAMERA in a background thread
		new Thread(this::startCameraStream).start();
	}

	/**
	 * Set the logged-in student for verification
	 */
	public void setLoggedInStudent(Student student) {
		this.loggedInStudent = student;
		System.out.println("Logged-in student set: " + student.getId() + " (FaceID: " + student.getFaceId() + ")");
	}

	private void startCameraStream() {
		System.out.println("startCameraStream called - Pre-warming camera...");
		if (cameraHelper == null) {
			cameraHelper = new CameraHelper();
		}
		running = true;
		cameraError = false;
		firstFrameReceived = false;

		cameraHelper.startStream(frameBase64 -> {
			if (!running)
				return;

			if (!firstFrameReceived) {
				firstFrameReceived = true;
				isCameraReady = true;

				Platform.runLater(() -> {
					lblResult.setText("Sẵn sàng chụp");
					spinner.setVisible(false);
					btnCapture.setVisible(true);
					btnStart.setVisible(false);
					btnRetry.setVisible(false);
				});
			}
		}, imgCamera, ex -> {
			// Camera startup/stream failed
			running = false;
			isCameraReady = false;
			cameraError = true;
			System.err.println("Camera error: " + ex.getMessage());
			ex.printStackTrace();
			Platform.runLater(() -> {
				lblResult.setText("⚠️ Không thể truy cập camera — kiểm tra kết nối/driver");
				spinner.setVisible(false);
				btnStart.setVisible(false);
				btnCapture.setVisible(false);
				btnRetry.setVisible(true); // Show retry button
			});
		});
	}

	public void setStudentInfo(String id, String name, String className, String room) {
		System.out.println("=== setStudentInfo called ===");
		System.out.println("ID: " + id);
		System.out.println("Name: " + name);
		System.out.println("Class: " + className);
		System.out.println("Room: " + room);

		if (lblStudentId != null) {
			lblStudentId.setText(id);
			System.out.println("lblStudentId set to: " + id);
		} else {
			System.err.println("ERROR: lblStudentId is NULL!");
		}

		if (lblStudentName != null) {
			lblStudentName.setText(name);
			System.out.println("lblStudentName set to: " + name);
		} else {
			System.err.println("ERROR: lblStudentName is NULL!");
		}

		if (lblClass != null) {
			lblClass.setText(className);
			System.out.println("lblClass set to: " + className);
		} else {
			System.err.println("ERROR: lblClass is NULL!");
		}

		if (lblRoom != null) {
			lblRoom.setText(room);
			System.out.println("lblRoom set to: " + room);
		} else {
			System.err.println("ERROR: lblRoom is NULL!");
		}
	}

	public void setSampleImage(javafx.scene.image.Image image) {
		imgSample.setImage(image);
	}

	@FXML
	public void onStop(ActionEvent e) {
		cleanup();
		Platform.runLater(() -> {
			lblResult.setText("Đã dừng");
		});
	}

	@FXML
	public void onStartRecognition(ActionEvent e) {
		// This button is now only for "Chụp lại" (Retake)
		System.out.println("Retake button clicked. Restarting camera...");
		Platform.runLater(() -> {
			lblResult.setText("Đang khởi tạo camera...");
			spinner.setVisible(true);
			btnStart.setVisible(false);
			btnCapture.setVisible(false);
		});
		// Restart the camera stream in a new thread
		new Thread(this::startCameraStream).start();
	}

	@FXML
	public void onRetry(ActionEvent e) {
		// This handles retrying after a camera *initialization failure*
		System.out.println("Retrying camera initialization...");
		Platform.runLater(() -> {
			lblResult.setText("Đang thử lại...");
			spinner.setVisible(true);
			btnRetry.setVisible(false);
			btnStart.setVisible(false);
			btnCapture.setVisible(false);
		});
		// Start the camera initialization process again
		new Thread(this::startCameraStream).start();
	}

	@FXML
	public void onCapturePhoto(ActionEvent e) {
		try {
			BufferedImage captured = cameraHelper.captureFrame();
			if (captured == null) {
				Platform.runLater(() -> lblResult.setText("⚠️ Không thể chụp ảnh - chưa có khung hình"));
				return;
			}

			// === USER REQUEST: Stop the camera after capture ===
			cleanup(); // Stop camera and release resources

			final Image fxImage = SwingFXUtils.toFXImage(captured, null);
			Platform.runLater(() -> {
				imgCamera.setImage(fxImage); // Show the frozen frame
				lblResult.setText("Đang nhận diện...");
				btnCapture.setVisible(false); // Hide capture button

				// Show "Chụp lại" (Retake) button, which is the btnStart
				btnStart.setVisible(true);
				btnStart.setText("Chụp lại");
				btnStart.setDisable(false);
			});

			// Convert to base64 and send to recognition API in a background thread
			new Thread(() -> {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(captured, "jpg", baos);
					String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

					// Call Flask API via HTTP
					PythonBridge.RecognitionResult result = callRecognitionApi(base64);

					// Verify if recognized face matches logged-in student
					boolean isVerified = false;
					String verificationMessage = "";

					if (loggedInStudent != null && result.match) {
						String recognizedFaceId = extractFaceId(result.person);
						if (recognizedFaceId != null) {
							isVerified = StudentDatabase.getInstance().verifyStudent(
									loggedInStudent.getId(),
									recognizedFaceId);
							verificationMessage = isVerified
									? "✓ Xác thực thành công - Cho phép vào thi"
									: "⚠️ CẢNH BÁO: Khuôn mặt không khớp với sinh viên đã đăng nhập!";
							if (!isVerified) {
								System.err.println("FRAUD DETECTED: Logged-in: " + loggedInStudent.getId() +
										" (FaceID: " + loggedInStudent.getFaceId() + ") vs Recognized: "
										+ recognizedFaceId);
							}
						}
					}

					// Create final variables for use in the lambda
					final PythonBridge.RecognitionResult finalResult = result;
					final boolean finalIsVerified = isVerified;
					final String finalVerificationMessage = verificationMessage;

					// Navigate to result screen on JavaFX thread
					Platform.runLater(() -> {
						try {
							navigateToResultScreen(finalResult, fxImage, finalIsVerified, finalVerificationMessage);
						} catch (Exception err) {
							lblResult.setText("⚠️ Lỗi khi xử lý kết quả: " + err.getMessage());
							err.printStackTrace();
						}
					});
				} catch (Exception ex) {
					ex.printStackTrace();
					Platform.runLater(() -> lblResult.setText("⚠️ Lỗi khi nhận diện: " + ex.getMessage()));
				}
			}).start();

		} catch (Exception ex) {
			ex.printStackTrace();
			Platform.runLater(() -> lblResult.setText("⚠️ Lỗi khi chụp ảnh: " + ex.getMessage()));
		}
	}

	private PythonBridge.RecognitionResult callRecognitionApi(String base64) {
		try {
			HttpClient client = HttpClient.newHttpClient();
			String jsonBody = "{\"frame\": \"" + base64 + "\"}";
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://127.0.0.1:5000/recognize-stream"))
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + StudentLoginController.TOKEN)
					.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				return new com.google.gson.Gson().fromJson(response.body(), PythonBridge.RecognitionResult.class);
			} else {
				System.err.println("⚠️ Lỗi Flask API: " + response.statusCode());
				return new PythonBridge.RecognitionResult(false, "Xác thực thất bại",
						"LỖI API " + response.statusCode(), 0, "red");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Platform.runLater(() -> lblResult.setText("⚠️ Lỗi khi gọi Flask API: " + ex.getMessage()));
			return new PythonBridge.RecognitionResult(false, ex.getMessage(), "LỖI KẾT NỐI", 0, "red");
		}
	}

	/**
	 * Extract face ID from recognition result person string
	 * Handles formats like "ID: 126" or direct name lookup
	 */
	private String extractFaceId(String person) {
		if (person == null)
			return null;

		// Check if format is "ID: XXX"
		if (person.startsWith("ID: ")) {
			return person.substring(4).trim();
		}

		// Otherwise, try to find student by name (reverse lookup)
		try {
			var allStudents = StudentDatabase.getInstance().getAllStudents();
			for (Student s : allStudents.values()) {
				if (person.equals(s.getName())) {
					return s.getFaceId();
				}
			}
		} catch (Exception e) {
			System.err.println("Error in extractFaceId: " + e.getMessage());
		}

		return null;
	}

	private void navigateToResultScreen(PythonBridge.RecognitionResult result, Image capturedImage,
			boolean isVerified, String verificationMessage) {
		try {
			// Camera is already stopped by cleanup() in onCapturePhoto
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/result.fxml"));
			Parent root = loader.load();

			ResultController controller = loader.getController();

			String studentId = loggedInStudent != null ? loggedInStudent.getId() : "N/A";
			String studentName = loggedInStudent != null ? loggedInStudent.getName() : "N/A";
			String className = loggedInStudent != null ? loggedInStudent.getClazz() : "N/A";
			String room = loggedInStudent != null ? loggedInStudent.getRoom() : "N/A";

			LocalDateTime now = LocalDateTime.now();
			String date = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

			controller.setResultData(
					studentId, studentName, date, time,
					result.percentage / 100.0,
					result.status, result.person, result.color,
					className, room, result.warning);

			controller.setVerificationStatus(isVerified, verificationMessage);
			controller.setCapturedImage(capturedImage);

			Stage stage = (Stage) imgCamera.getScene().getWindow();
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			lblResult.setText("⚠️ Lỗi khi chuyển màn hình: " + e.getMessage());
		}
	}

	/**
	 * Public cleanup method that can be called when leaving this screen
	 */
	public void cleanup() {
		System.out.println("FaceRecognitionController cleanup called");
		running = false;
		if (cameraHelper != null) {
			cameraHelper.stopCamera();
		}
	}
}