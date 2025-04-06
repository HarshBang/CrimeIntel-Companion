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
import android.view.View;
import android.widget.Button;
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

public class GeoCameraActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 22;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;

    Button btnpicture;
    ImageView imageView;
    String currentPhotoPath;
    double latitude;
    double longitude;
    String address, date, time, timeZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_geo_camera);

        btnpicture = findViewById(R.id.cameraBtn);
        imageView = findViewById(R.id.capimageview);

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
    }

    private void getLocationAndTakePicture() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "final_image_with_overlay.jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  // Save as JPEG with high quality
            Toast.makeText(this, "Image saved with overlay: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }
}
