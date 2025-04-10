package com.example.crimeintelcompanion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.mobileconnectors.s3.transferutility.*;
import java.io.File;

public class GeoCameraActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 22;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;

    Button btnpicture;
    ImageView imageView;
    String currentPhotoPath;
    double latitude;
    double longitude;
    String address, date, time, timeZone;
    ImageButton yesUpload, erase;
    Uri imageUri;
    File imageFile;

    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonS3Client s3Client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_geo_camera);

        checkCameraAndStoragePermissions();

        btnpicture = findViewById(R.id.cameraBtn);
        imageView = findViewById(R.id.capimageview);
        yesUpload = findViewById(R.id.yesUpload);
        erase = findViewById(R.id.erase);

        btnpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(GeoCameraActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GeoCameraActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    getLocationAndTakePicture();
                }
            }
        });

        yesUpload.setOnClickListener(v -> {
            if (imageFile != null) {
                uploadImageToS3(imageFile);
            } else {
                Toast.makeText(this, "Capture an image first", Toast.LENGTH_SHORT).show();
            }
        });

        erase.setOnClickListener(v -> eraseImage());

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:270e7253-126f-4e3f-bdbe-de12745362da", // Cognito Identity Pool ID
                Regions.US_EAST_1
        );

        // ✅ Create S3 client with credentials
        s3Client = new AmazonS3Client(credentialsProvider);
    }

    private static final int CAMERA_PERMISSION_CODE = 102;

    private void checkCameraAndStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, CAMERA_PERMISSION_CODE);
        }
    }

    private void getLocationAndTakePicture() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                address = getAddressFromLocation(latitude, longitude);
                // Get current date, time, and time zone
                date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                timeZone = TimeZone.getDefault().getDisplayName();
            }
            dispatchTakePictureIntent();
        }
    }

    private String getAddressFromLocation(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown location";
    }

    private void dispatchTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.crimeintelcompanion.fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            setPic();
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void setPic() {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        bitmap = rotateImageIfRequired(bitmap, currentPhotoPath);

        // Add overlay with geotagging information
        bitmap = addGeotagOverlay(bitmap);

        // Display the image in the ImageView
        imageView.setImageBitmap(bitmap);

        // Save the final bitmap with overlay to storage
        saveBitmapWithOverlay(bitmap);
    }

    private Bitmap addGeotagOverlay(Bitmap originalBitmap) {
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);  // Set text color to white
        paint.setTextSize(40);  // Adjust text size as needed
        paint.setAntiAlias(true);
        paint.setAlpha(200);  // Set some transparency for the text

        // Prepare the geotag information
        String geotagText = "Latitude: " + latitude + "\n" +
                "Longitude: " + longitude + "\n\n" +
                "Address: " + address + "\n\n" +
                "Date: " + date + "\n" +
                "Time: " + time + "\n" +
                "Time Zone: " + timeZone;

        // Calculate text bounds and determine where to draw
        Rect textBounds = new Rect();
        paint.getTextBounds(geotagText, 0, geotagText.length(), textBounds);

        // Calculate line height
        float lineHeight = paint.getTextSize() * 1.2f;

        // Max width for text (image width - padding)
        int maxWidth = mutableBitmap.getWidth() - 40;  // 20 padding on both sides

        // Wrap text to avoid overflowing
        String[] lines = wrapText(geotagText, paint, maxWidth);

        // Calculate height for the text box so it doesn't overflow the image
        int textBoxHeight = (int) (lines.length * lineHeight) + 40;  // Add padding

        // Position the text 20px from the bottom of the image
        int startY = mutableBitmap.getHeight() - textBoxHeight;

        // Draw semi-transparent background for readability
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setAlpha(150);  // Set transparency for background
        canvas.drawRect(20, startY, mutableBitmap.getWidth() - 20, mutableBitmap.getHeight() - 20, backgroundPaint);

        // Draw the wrapped text
        int y = startY + 20;  // Padding from the top of the text box
        for (String line : lines) {
            canvas.drawText(line, 20, y, paint);
            y += lineHeight;  // Move to next line
        }

        return mutableBitmap;
    }

    private String[] wrapText(String text, Paint paint, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.toString() + word + " ";
            if (paint.measureText(testLine) < maxWidth) {
                line.append(word).append(" ");
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word + " ");
            }
        }

        // Add the last line if any
        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines.toArray(new String[0]);
    }

    private Bitmap rotateImageIfRequired(Bitmap img, String photoPath) {
        ExifInterface ei;
        try {
            ei = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
            return img;
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void saveBitmapWithOverlay(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_" + timeStamp + "_overlay.jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  // Save as JPEG with high quality
            Toast.makeText(this, "Image saved with overlay: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
        imageFile = file;
    }

    public void uploadImageToS3(File imageFile) {
        TransferUtility transferUtility = TransferUtility.builder()
                .context(getApplicationContext())
                .awsConfiguration(null) // Not using awsconfiguration.json
                .s3Client(s3Client)
                .build();

        TransferObserver uploadObserver = transferUtility.upload(
                "crimeintel-evidence",          // bucket name
                "evidence/" + imageFile.getName(), // key (path in S3)
                imageFile
        );

        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    Log.d("S3 Upload", "Upload successful!");
                    // 🔁 Call Lambda here if needed
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;
                Log.d("S3 Upload", "Progress: " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("S3 Upload", "Upload error", ex);
            }
        });
    }


    private void eraseImage() {
        imageView.setImageDrawable(null);  // Clear the ImageView
        if (imageFile != null && imageFile.exists()) {
            if (imageFile.delete()) {
                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        }
        imageFile = null;
        currentPhotoPath = null;
    }
}
