package com.gk.chatapp.p2p;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.YuvConverter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.graphics.ImageFormat.NV21;

/**
 * SnapshotVideoRenderer demonstrates how to implement a custom {@link VideoRenderer}. Caches the
 * last frame rendered and will update the provided image view any time {@link #takeSnapshot()} is
 * invoked.
 */
public class SnapshotVideoRenderer implements VideoRenderer.Callbacks {
    private static final String TAG = SnapshotVideoRenderer.class.getSimpleName();
    private final HandlerThread renderThread;
    private final Handler renderThreadHandler;
    private final String outputFile;
    private final int outputFileWidth;
    private final int outputFileHeight;
    private final int outputFrameSize;
    private final ByteBuffer outputFrameBuffer;
    private EglBase eglBase;
    private YuvConverter yuvConverter;
    private final AtomicBoolean snapshotRequsted = new AtomicBoolean(false);

    public SnapshotVideoRenderer(String outputFile, int outputFileWidth, int outputFileHeight,
                                 final EglBase.Context sharedContext) {
        if ((outputFileWidth % 2) == 1 || (outputFileHeight % 2) == 1) {
            throw new IllegalArgumentException("Does not support uneven width or height");
        }

        this.outputFile = outputFile;
        this.outputFileWidth = outputFileWidth;
        this.outputFileHeight = outputFileHeight;

        outputFrameSize = outputFileWidth * outputFileHeight * 3 / 2;
        outputFrameBuffer = ByteBuffer.allocateDirect(outputFrameSize);

        renderThread = new HandlerThread(TAG);
        renderThread.start();
        renderThreadHandler = new Handler(renderThread.getLooper());

        ThreadUtils.invokeAtFrontUninterruptibly(renderThreadHandler, new Runnable() {
            @Override
            public void run() {
                eglBase = EglBase.create(sharedContext, EglBase.CONFIG_PIXEL_BUFFER);
                eglBase.createDummyPbufferSurface();
                eglBase.makeCurrent();
                yuvConverter = new YuvConverter();
            }
        });
    }

    @Override
    public void renderFrame(final I420Frame frame) {
        // Capture bitmap
        if (snapshotRequsted.compareAndSet(true, false)) {
            renderThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    long time = new Date().getTime();
                    snapshot(frame, "snapshot_" + time + ".jpeg");

                    // Frames must be released after rendering to free the native memory
                    VideoRenderer.renderFrameDone(frame);
                }
            });
        } else {
            VideoRenderer.renderFrameDone(frame);
        }
    }

    /**
     * Request a snapshot on the rendering thread.
     */
    public void takeSnapshot() {
        snapshotRequsted.set(true);
    }

    /**
     * Release all resources. All already posted frames will be rendered first.
     */
    public void release() {
        final CountDownLatch cleanupBarrier = new CountDownLatch(1);
        renderThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                yuvConverter.release();
                eglBase.release();
                renderThread.quit();
                cleanupBarrier.countDown();
            }
        });
        ThreadUtils.awaitUninterruptibly(cleanupBarrier);
    }

    private void snapshot(I420Frame i420Frame, String fileName) {
        YuvImage yuvImage;
        if (!i420Frame.yuvFrame) {
            final float frameAspectRatio = (float) i420Frame.rotatedWidth() / (float) i420Frame.rotatedHeight();

            final float[] rotatedSamplingMatrix =
                    RendererCommon.rotateTextureMatrix(i420Frame.samplingMatrix, i420Frame.rotationDegree);
            final float[] layoutMatrix = RendererCommon.getLayoutMatrix(
                    false, frameAspectRatio, (float) outputFileWidth / outputFileHeight);
            final float[] texMatrix = RendererCommon.multiplyMatrices(rotatedSamplingMatrix, layoutMatrix);

            yuvConverter.convert(outputFrameBuffer, outputFileWidth, outputFileHeight, outputFileWidth,
                    i420Frame.textureId, texMatrix);

            byte[] yuv420p = new byte[outputFileWidth * outputFileHeight * 3 / 2];
            int stride = outputFileWidth;
            byte[] data = outputFrameBuffer.array();
            int offset = outputFrameBuffer.arrayOffset();

            // Write Y
            System.arraycopy(data, offset, yuv420p, 0, outputFileWidth * outputFileHeight);

            // Write U
            for (int r = 0; r < outputFileHeight / 2; ++r) {
                System.arraycopy(data, offset + (r + outputFileHeight) * stride,
                        yuv420p, outputFileWidth * outputFileHeight + r * stride / 2, stride / 2);
            }

            // Write V
            for (int r = 0; r < outputFileHeight / 2; ++r) {
                System.arraycopy(data, offset + (r + outputFileHeight) * stride + stride / 2,
                        yuv420p, outputFileWidth * outputFileHeight * 5 / 4 + r * stride / 2, stride / 2);
            }

            byte[] nv21 = new byte[yuv420p.length];
            if (yuv420p != null && yuv420p.length != 0) {
                // 截屏很坎坷，YuvConverter出来的格式是YUV420P，YuvImage需要的格式是NV21
                swapYUVImage(yuv420p, nv21, outputFileWidth, outputFileHeight);
            }
            yuvImage = new YuvImage(nv21, NV21, outputFileWidth, outputFileHeight, null);
        } else {
            yuvImage = i420ToYuvImage(i420Frame);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Rect rect = new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight());

        // Compress YuvImage to jpeg
        yuvImage.compressToJpeg(rect, 100, stream);
        saveSnapshot(stream, fileName);
    }

    private void saveSnapshot(ByteArrayOutputStream baos, String fileName) {
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + outputFile + File.separator + fileName;
        File f = new File(path);
        if(!f.mkdirs()){
            return;
        }
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            baos.writeTo(fos);
        } catch (IOException e) {
            Log.i(TAG, "save snapshot error!");
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                Log.i(TAG, "save snapshot error!");
            }
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                Log.i(TAG, "save snapshot error!");
            }
        }
    }

    private void swapYUVImage(byte[] inBytes, byte[] outBytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;

        System.arraycopy(inBytes, 0, outBytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            outBytes[nLenY + 2 * i + 1] = inBytes[nLenY + i];
            outBytes[nLenY + 2 * i] = inBytes[nLenY + nLenU + i];
        }
    }

    private YuvImage i420ToYuvImage(I420Frame i420Frame) {
        if (i420Frame.yuvStrides[0] != i420Frame.width) {
            return fastI420ToYuvImage(i420Frame);
        }
        if (i420Frame.yuvStrides[1] != i420Frame.width / 2) {
            return fastI420ToYuvImage(i420Frame);
        }
        if (i420Frame.yuvStrides[2] != i420Frame.width / 2) {
            return fastI420ToYuvImage(i420Frame);
        }

        byte[] bytes = new byte[i420Frame.yuvStrides[0] * i420Frame.height +
                i420Frame.yuvStrides[1] * i420Frame.height / 2 +
                i420Frame.yuvStrides[2] * i420Frame.height / 2];
        ByteBuffer tmp = ByteBuffer.wrap(bytes, 0, i420Frame.width * i420Frame.height);
        copyPlane(i420Frame.yuvPlanes[0], tmp);

        byte[] tmpBytes = new byte[i420Frame.width / 2 * i420Frame.height / 2];
        tmp = ByteBuffer.wrap(tmpBytes, 0, i420Frame.width / 2 * i420Frame.height / 2);

        copyPlane(i420Frame.yuvPlanes[2], tmp);
        for (int row = 0; row < i420Frame.height / 2; row++) {
            for (int col = 0; col < i420Frame.width / 2; col++) {
                bytes[i420Frame.width * i420Frame.height + row * i420Frame.width + col * 2]
                        = tmpBytes[row * i420Frame.width / 2 + col];
            }
        }
        copyPlane(i420Frame.yuvPlanes[1], tmp);
        for (int row = 0; row < i420Frame.height / 2; row++) {
            for (int col = 0; col < i420Frame.width / 2; col++) {
                bytes[i420Frame.width * i420Frame.height + row * i420Frame.width + col * 2 + 1] =
                        tmpBytes[row * i420Frame.width / 2 + col];
            }
        }
        return new YuvImage(bytes, NV21, i420Frame.width, i420Frame.height, null);
    }

    private YuvImage fastI420ToYuvImage(I420Frame i420Frame) {
        byte[] bytes = new byte[i420Frame.width * i420Frame.height * 3 / 2];
        int i = 0;
        for (int row = 0; row < i420Frame.height; row++) {
            for (int col = 0; col < i420Frame.width; col++) {
                bytes[i++] = i420Frame.yuvPlanes[0].get(col + row * i420Frame.yuvStrides[0]);
            }
        }
        for (int row = 0; row < i420Frame.height / 2; row++) {
            for (int col = 0; col < i420Frame.width / 2; col++) {
                bytes[i++] = i420Frame.yuvPlanes[2].get(col + row * i420Frame.yuvStrides[2]);
                bytes[i++] = i420Frame.yuvPlanes[1].get(col + row * i420Frame.yuvStrides[1]);
            }
        }
        return new YuvImage(bytes, NV21, i420Frame.width, i420Frame.height, null);
    }

    private void copyPlane(ByteBuffer src, ByteBuffer dst) {
        src.position(0).limit(src.capacity());
        dst.put(src);
        dst.position(0).limit(dst.capacity());
    }
}