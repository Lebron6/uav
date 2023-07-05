package com.mg.uav.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.mg.uav.R;
import com.mg.uav.app.DJIApp;
import com.mg.uav.app.UAVApp;
import com.mg.uav.base.BaseActivity;
import com.mg.uav.tools.RecyclerViewHelper;
import com.mg.uav.ui.adapter.FileListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseProduct;
import dji.sdk.media.FetchMediaTask;
import dji.sdk.media.FetchMediaTaskContent;
import dji.sdk.media.FetchMediaTaskScheduler;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

/**
 * 图库
 */
public class GalleryActivity extends BaseActivity {
    private FileListAdapter mListAdapter;
    private List<MediaFile> mediaFileList = new ArrayList<MediaFile>();
    private MediaManager mMediaManager;
    private MediaManager.FileListState currentFileListState = MediaManager.FileListState.UNKNOWN;
    RecyclerView recycle;
    private FetchMediaTaskScheduler scheduler;


    public static void actionStart(Context context) {
        Intent intent = new Intent(context, GalleryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMediaManager();
    }

    private void initMediaManager() {
        if (DJIApp.getProductInstance() == null) {
            mediaFileList.clear();
            setResultToToast("Product disconnected");
            return;
        } else {
            if (null != DJIApp.getCameraInstance() && DJIApp.getCameraInstance().isMediaDownloadModeSupported()) {
                mMediaManager = DJIApp.getCameraInstance().getMediaManager();
                scheduler = mMediaManager.getScheduler();

                if (null != mMediaManager) {
                    if (isMavicAir2() || isM300()) {
                        DJIApp.getCameraInstance().enterPlayback(djiError -> {
                            if (djiError == null) {
                                DJILog.e(TAG, "Set cameraMode success");
                                getFileList();
                            } else {
                                setResultToToast("Set cameraMode failed");
                            }
                        });
                    } else {
                        DJIApp.getCameraInstance().setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, error -> {
                            if (error == null) {
                                DJILog.e(TAG, "Set cameraMode success");
                                getFileList();
                            } else {
                                setResultToToast("Set cameraMode failed");
                            }
                        });
                    }

                    if (mMediaManager.isVideoPlaybackSupported()) {
                        DJILog.e(TAG, "Camera support video playback!");
                    } else {
                        setResultToToast("Camera does not support video playback!");
                    }
                }

            } else if (null != DJIApp.getCameraInstance()
                    && !DJIApp.getCameraInstance().isMediaDownloadModeSupported()) {
                setResultToToast("Media Download Mode not Supported");
            }
        }
        return;
    }

    private void getFileList() {
        mMediaManager = DJIApp.getCameraInstance().getMediaManager();
        if (mMediaManager != null) {

            if ((currentFileListState == MediaManager.FileListState.SYNCING) || (currentFileListState == MediaManager.FileListState.DELETING)) {
                DJILog.e(TAG, "Media Manager is busy.");
            } else {
                mMediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, djiError -> {
                    if (null == djiError) {
                        mediaFileList = mMediaManager.getSDCardFileListSnapshot();
                        if (mediaFileList != null) {
                            Collections.sort(mediaFileList, (lhs, rhs) -> {
                                if (lhs.getTimeCreated() < rhs.getTimeCreated()) {
                                    return 1;
                                } else if (lhs.getTimeCreated() > rhs.getTimeCreated()) {
                                    return -1;
                                }
                                return 0;
                            });
                            scheduler.resume(new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError error) {
                                    if (error == null) {
                                        getThumbnails();
                                    }
                                }
                            });
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mListAdapter.setData(mediaFileList);
//                                    RecyclerViewHelper.initRecyclerViewG(GalleryActivity.this, recycle, mListAdapter, 3);
//                                }
//                            });

                        }
                    } else {
                        setResultToToast("Get Media File List Failed:" + djiError.getDescription());
                    }
                });
            }
        }
    }

    private FetchMediaTask.Callback taskCallback = new FetchMediaTask.Callback() {
        @Override
        public void onUpdate(MediaFile file, FetchMediaTaskContent option, DJIError error) {
            if (null == error) {
                if (option == FetchMediaTaskContent.PREVIEW) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mListAdapter.setData(mediaFileList);

//                            mListAdapter.notifyDataSetChanged();
                        }
                    });
                }
                if (option == FetchMediaTaskContent.THUMBNAIL) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mListAdapter.setData(mediaFileList);

//                            mListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            } else {
                DJILog.e(TAG, "Fetch Media Task Failed" + error.getDescription());
            }
        }
    };
    private void getThumbnails() {
        if (mediaFileList.size() <= 0) {
            setResultToToast("No File info for downloading thumbnails");
            return;
        }
        for (int i = 0; i < mediaFileList.size(); i++) {
            getThumbnailByIndex(i);
        }
    }
    private void getThumbnailByIndex(final int index) {
        FetchMediaTask task = new FetchMediaTask(mediaFileList.get(index), FetchMediaTaskContent.THUMBNAIL, taskCallback);
        scheduler.moveTaskToEnd(task);
    }

    private void initView() {
        recycle = findViewById(R.id.recycle);
        mListAdapter = new FileListAdapter(this);
        RecyclerViewHelper.initRecyclerViewG(GalleryActivity.this, recycle, mListAdapter, 3);

    }




    @Override
    public boolean useEventBus() {
        return false;
    }

    private void setResultToToast(final String result) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(GalleryActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isMavicAir2() {
        BaseProduct baseProduct = DJIApp.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MAVIC_AIR_2;
        }
        return false;
    }

    private boolean isM300() {
        BaseProduct baseProduct = DJIApp.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MATRICE_300_RTK;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (mMediaManager != null) {
            mMediaManager.stop(null);
            mMediaManager.exitMediaDownloading();
            if (scheduler != null) {
                scheduler.removeAllTasks();
            }
        }if (DJIApp.getCameraInstance()!=null){
            DJIApp.getCameraInstance().exitPlayback(djiError -> {
                if (djiError == null) {
                    DJILog.e(TAG, "exitPlayback success");
                    getFileList();
                } else {
                    setResultToToast("exitPlayback failed");
                }
            });
        }


        if (mediaFileList != null) {
            mediaFileList.clear();
        }
        super.onDestroy();
    }
}
