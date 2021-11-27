package ru.tim.gesturerecognition

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mediapipe.framework.TextureFrame
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.hands.HandLandmark
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult

/** Main activity of MediaPipe Hands app.  */
class MainActivity : AppCompatActivity() {
    private var hands: Hands? = null

    private enum class InputSource {
        UNKNOWN, CAMERA
    }

    private var inputSource = InputSource.UNKNOWN

    // Live camera demo UI and camera components.
    private var cameraInput: CameraInput? = null
    private var glSurfaceView: SolutionGlSurfaceView<HandsResult>? = null
    private var cameraFacing = CameraInput.CameraFacing.BACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLiveDemoUiComponents()
    }

    override fun onResume() {
        super.onResume()
        if (inputSource == InputSource.CAMERA) {
            // Restarts the camera and the opengl surface rendering.
            cameraInput = CameraInput(this)
            cameraInput!!.setNewFrameListener { textureFrame: TextureFrame? -> hands!!.send(textureFrame) }
            glSurfaceView!!.post { startCamera() }
            glSurfaceView!!.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        if (inputSource == InputSource.CAMERA) {
            glSurfaceView!!.visibility = View.GONE
            cameraInput!!.close()
        }
    }

    /** Sets up the UI components for the live demo with camera input.  */
    private fun setupLiveDemoUiComponents() {
        val switchButton = findViewById<FloatingActionButton>(R.id.switchButton)
        switchButton.setOnClickListener {
            cameraFacing =
                    if (cameraFacing == CameraInput.CameraFacing.FRONT) CameraInput.CameraFacing.BACK
                    else CameraInput.CameraFacing.FRONT
            glSurfaceView!!.post { startCamera() }
        }
        if (inputSource == InputSource.CAMERA) {
            return
        }
        stopCurrentPipeline()
        setupStreamingModePipeline(InputSource.CAMERA)
    }

    /** Sets up core workflow for streaming mode.  */
    private fun setupStreamingModePipeline(inputSource: InputSource) {
        this.inputSource = inputSource
        // Initializes a new MediaPipe Hands solution instance in the streaming mode.
        hands = Hands(
                this,
                HandsOptions.builder()
                        .setStaticImageMode(false)
                        .setMaxNumHands(1)
                        .setRunOnGpu(RUN_ON_GPU)
                        .build())
        hands!!.setErrorListener { message: String, _: RuntimeException? -> Log.e(TAG, "MediaPipe Hands error:$message") }
        if (inputSource == InputSource.CAMERA) {
            cameraInput = CameraInput(this)
            cameraInput!!.setNewFrameListener { textureFrame: TextureFrame? -> hands!!.send(textureFrame) }
        }

        // Initializes a new Gl surface view with a user-defined HandsResultGlRenderer.
        glSurfaceView = SolutionGlSurfaceView(this, hands!!.glContext, hands!!.glMajorVersion)
        glSurfaceView!!.setSolutionResultRenderer(HandsResultGlRenderer())
        glSurfaceView!!.setRenderInputImage(true)
        hands!!.setResultListener { handsResult: HandsResult ->
            logWristLandmark(handsResult, false)
            glSurfaceView!!.setRenderData(handsResult)
            glSurfaceView!!.requestRender()
        }

        // The runnable to start camera after the gl surface view is attached.
        // For video input source, videoInput.start() will be called when the video uri is available.
        if (inputSource == InputSource.CAMERA) {
            glSurfaceView!!.post { startCamera() }
        }

        // Updates the preview layout.
        val constraintLayout = findViewById<ConstraintLayout>(R.id.preview_display_layout)
        //constraintLayout.removeAllViewsInLayout();
        constraintLayout.addView(glSurfaceView)
        glSurfaceView!!.visibility = View.VISIBLE
        constraintLayout.requestLayout()
    }

    private fun startCamera() {
        cameraInput!!.start(
                this,
                hands!!.glContext,
                cameraFacing,
                1920, 1080)
    }

    private fun stopCurrentPipeline() {
        if (cameraInput != null) {
            cameraInput!!.setNewFrameListener(null)
            cameraInput!!.close()
        }
        if (glSurfaceView != null) {
            glSurfaceView!!.visibility = View.GONE
        }
        if (hands != null) {
            hands!!.close()
        }
    }

    private fun logWristLandmark(result: HandsResult, showPixelValues: Boolean) {
        if (result.multiHandLandmarks().isEmpty()) {
            return
        }
        val wristLandmark = result.multiHandLandmarks()[0].landmarkList[HandLandmark.WRIST]
        // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
        if (showPixelValues) {
            val width = result.inputBitmap().width
            val height = result.inputBitmap().height
            Log.i(
                    TAG, String.format(
                    "MediaPipe Hand wrist coordinates (pixel values): x=%f, y=%f",
                    wristLandmark.x * width, wristLandmark.y * height))
        } else {
            Log.i(
                    TAG, String.format(
                    "MediaPipe Hand wrist normalized coordinates (value range: [0, 1]): x=%f, y=%f",
                    wristLandmark.x, wristLandmark.y))
        }
        if (result.multiHandWorldLandmarks().isEmpty()) {
            return
        }
        val wristWorldLandmark = result.multiHandWorldLandmarks()[0].landmarkList[HandLandmark.WRIST]
        Log.i(
                TAG, String.format("MediaPipe Hand wrist world coordinates (in meters with the origin at the hand's"
                + " approximate geometric center): x=%f m, y=%f m, z=%f m",
                wristWorldLandmark.x, wristWorldLandmark.y, wristWorldLandmark.z))
    }

    companion object {
        private const val TAG = "MainActivity"

        // Run the pipeline and the model inference on GPU or CPU.
        private const val RUN_ON_GPU = true
    }
}