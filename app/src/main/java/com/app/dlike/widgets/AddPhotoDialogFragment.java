package com.app.dlike.widgets;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.dlike.R;
import com.app.dlike.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * Created by moses on 8/31/18.
 */

public class AddPhotoDialogFragment extends BottomSheetDialogFragment {

    private static final int REQUEST_CODE_OPEN_CAMERA = 1012;
    private static final int REQUEST_CODE_OPEN_GALLERY = 1013;
    private File file;
    private View cameraView;
    private View galleryView;
    private View removePhotoView;
    private LinearLayout parentView;
    private ImageChooseListener imageChooseListener;
    private boolean hasPhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraView = view.findViewById(R.id.photoFromCamera);
        galleryView = view.findViewById(R.id.photoFromGallery);
        removePhotoView = view.findViewById(R.id.removePhoto);
        parentView = view.findViewById(R.id.mainLayout);
        if(!hasPhoto){
            removePhotoView.setVisibility(View.GONE);
            parentView.setWeightSum(2);
        }

        cameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Tools.isPermissionGranted(getContext())) {
                    Intent intent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    file = new File(android.os.Environment.getExternalStorageDirectory(), "img" + System.currentTimeMillis() + ".jpg");

                    if (android.os.Build.VERSION.SDK_INT >= 24) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file));
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    }

                    startActivityForResult(intent, REQUEST_CODE_OPEN_CAMERA);
                } else {
                    Toast.makeText(getContext(), "Please enable Camera and Storage permissions from setting to continue", Toast.LENGTH_SHORT).show();
                }
            }
        });

        galleryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Tools.isPermissionGranted(getContext())) {
                    Intent pictureActionIntent = null;

                    pictureActionIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pictureActionIntent, REQUEST_CODE_OPEN_GALLERY);
                } else {
                    Toast.makeText(getContext(), "Please enable Storage permission from setting to continue", Toast.LENGTH_SHORT).show();
                }
            }
        });

        removePhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageChooseListener != null) {
                    imageChooseListener.onRemoveImage();
                }
                dismiss();
            }
        });
    }

    public void setImageChooseListener(ImageChooseListener imageChooseListener) {
        this.imageChooseListener = imageChooseListener;
    }

    public ImageChooseListener getImageChooseListener() {
        return imageChooseListener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_OPEN_CAMERA:
                    try {
                        ExifInterface exif = new ExifInterface(file.getPath());
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        int angle = 0;

                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                            angle = 90;
                        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                            angle = 180;
                        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                            angle = 270;
                        }

                        Matrix mat = new Matrix();
                        mat.postRotate(angle);
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inSampleSize = 2;
                        Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(file), null, bmOptions);
                        Bitmap correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                        FileOutputStream out = new FileOutputStream(file);
                        correctBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);

                        if (imageChooseListener != null) {
                            imageChooseListener.onImageChosen(file);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_CODE_OPEN_GALLERY:
                    if (data == null) {
                        return;
                    }
                    try {
                        Uri selectedImage = data.getData();
                        String[] filePath = {MediaStore.Images.Media.DATA};
                        Cursor c = getContext().getContentResolver().query(selectedImage, filePath,
                                null, null, null);
                        c.moveToFirst();
                        int columnIndex = c.getColumnIndex(filePath[0]);
                        String path = c.getString(columnIndex);
                        c.close();

                        file = new File(path);

                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inSampleSize = 2;
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.close();

                        if (imageChooseListener != null) {
                            imageChooseListener.onImageChosen(file);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        dismiss();
    }

    public void show(FragmentManager manager, boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
        super.show(manager, AddPhotoDialogFragment.class.getSimpleName());
    }

    public interface ImageChooseListener {
        void onImageChosen(File file);

        void onRemoveImage();
    }
}
