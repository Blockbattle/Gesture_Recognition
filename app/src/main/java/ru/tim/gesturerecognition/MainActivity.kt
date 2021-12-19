package ru.tim.gesturerecognition

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.framework.TextureFrame
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.hands.HandLandmark
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.max

/** Main activity of MediaPipe Hands app.  */
class MainActivity : AppCompatActivity() {
    private var k = 0
    //private val resultQueue: Queue<List<LandmarkProto.NormalizedLandmark>> = ConcurrentLinkedQueue()
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

        //predictText()
        //Log.i("res", khttp.get("http://server-gesturecognition-dev.apps.sandbox-m2.ll9k.p1.openshiftapps.com/").text)

        setupLiveDemoUiComponents()
    }

   /* @DelicateCoroutinesApi
    private fun predictText() {
        GlobalScope.launch() {
            while (true) {
                while (!resultQueue.isEmpty()) {
                    GlobalScope.launch() {
                        Log.i("res", fetchWebsiteContents(resultQueue.poll()!!))
                    }
                }
                delay(50)
            }

            //Log.i("res", fetchWebsiteContents(resultQueue.poll()!!))
            /*while (true) {
                if (resultQueue.isEmpty())
                    delay(50)
                else
                    Log.i("res", fetchWebsiteContents(resultQueue.poll()!!))
            }*/
        }
    }*/

    private fun fetchWebsiteContents(landmarks: List<LandmarkProto.NormalizedLandmark>):String {
        var url = "http://server-gesturecognition-dev.apps.sandbox-m2.ll9k.p1.openshiftapps.com/letter?"
        for (i in landmarks.indices) {
            url += if (i == 0)
                "x=${landmarks[i].x}&y=${landmarks[i].y}"
            else
                "&x=${landmarks[i].x}&y=${landmarks[i].y}"
        }
        return khttp.get(url).text
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
                4000, 3000)
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

        //это сделал не я
        Handler(Looper.getMainLooper()).post {
            k = (k + 1) % 2
            if (k == 0) {
                GlobalScope.launch() {
                    //Log.i("res", fetchWebsiteContents(result.multiHandLandmarks()[0].landmarkList))
                    val s = fetchWebsiteContents(result.multiHandLandmarks()[0].landmarkList)
                    val subs = s.split(" ")
                    var confidence: Float = 0f
                    var res: Int = -1
                    for ((i, sub) in subs.withIndex()) {
                        if (sub.toFloat() > confidence) {
                            confidence = sub.toFloat()
                            res = i
                        }
                    }
                    Handler(Looper.getMainLooper()).post {
                        val textView: TextView = findViewById(R.id.textView)
                        textView.text = LETTERS[res]
                    }
                }
            }
            //resultQueue.add(result.multiHandLandmarks()[0].landmarkList)
        }

        //resultQueue.add(result.multiHandLandmarks()[0].landmarkList)
        //Log.i("res", fetchWebsiteContents(result.multiHandLandmarks()[0].landmarkList))

        val wristLandmark = result.multiHandLandmarks()[0].landmarkList[HandLandmark.WRIST]
        // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
        if (showPixelValues) {//showPixelValues
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

    private fun drawSkeleton(result: HandsResult) : Bitmap {
        val bitmap = result.inputBitmap()
        val resBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        resBitmap.eraseColor(Color.WHITE)
        val margin = 60

        val landmarkList = result.multiHandLandmarks()[0].landmarkList
        val minX = Collections.min(landmarkList, kotlin.Comparator { t, t2 -> t.x.compareTo(t2.x) }).x
        val maxX = Collections.max(landmarkList, kotlin.Comparator { t, t2 -> t.x.compareTo(t2.x) }).x
        val minY = Collections.min(landmarkList, kotlin.Comparator { t, t2 -> t.y.compareTo(t2.y) }).y
        val maxY = Collections.max(landmarkList, kotlin.Comparator { t, t2 -> t.y.compareTo(t2.y) }).y

        val width = maxX - minX
        val height = maxY - minY

        val maxLength = max(width, height)

        val canvas = Canvas(resBitmap)
        var p = Paint()

        //Draw connections
        for ((i, c) in Hands.HAND_CONNECTIONS.withIndex()) {
            val start: LandmarkProto.NormalizedLandmark = landmarkList[c.start()]
            val end: LandmarkProto.NormalizedLandmark = landmarkList[c.end()]

            val startX = (start.x - minX) * (resBitmap.width - 2 * margin) / maxLength + margin
            val startY = (start.y - minY) * (resBitmap.height - 2 * margin) / maxLength + margin
            val endX = (end.x - minX) * (resBitmap.width - 2 * margin) / maxLength + margin
            val endY = (end.y - minY) * (resBitmap.height - 2 * margin) / maxLength + margin

            p.strokeWidth = 2F

            when {
                i == 0 || i == 4 || i == 8 || i == 12 || i == 16 || i == 17 -> {
                    p.color = Color.rgb(128, 128, 128)
                    p.strokeWidth = 3F
                }
                i < 4 -> p.color = Color.rgb(255, 229, 180)
                i < 8 -> p.color = Color.rgb(128, 64, 128)
                i < 12 -> p.color = Color.rgb(255, 204, 0)
                i < 16 -> p.color = Color.rgb(48, 255, 48)
                i < 20 -> p.color = Color.rgb(21, 101, 192)
            }

            canvas.drawLine(startX, startY, endX, endY, p)
        }

        p = Paint()

        //Draw points
        for ((i, landmark) in landmarkList.withIndex()) {
            val pixelX = (landmark.x - minX) * (resBitmap.width - 2 * margin) / maxLength + margin
            val pixelY = (landmark.y - minY) * (resBitmap.height - 2 * margin) / maxLength + margin
            //resBitmap.setPixel(pixelX.toInt(), pixelY.toInt(), Color.BLACK)

            p.style = Paint.Style.FILL
            p.color = Color.rgb(224, 224, 224)
            canvas.drawCircle(pixelX, pixelY, 6F, p)

            when {
                i < 2 || i == 5 || i == 9 || i == 13 || i == 17 -> p.color = Color.rgb(255, 48, 48)
                i < 5 -> p.color = Color.rgb(255, 229, 180)
                i < 9 -> p.color = Color.rgb(128, 64, 128)
                i < 13 -> p.color = Color.rgb(255, 204, 0)
                i < 17 -> p.color = Color.rgb(48, 255, 48)
                i < 21 -> p.color = Color.rgb(21, 101, 192)
            }

            canvas.drawCircle(pixelX, pixelY, 5F, p)
        }

        return resBitmap
    }

    companion object {
        private const val TAG = "MainActivity"
        private val LETTERS = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z nothing space".split(" ")

        // Run the pipeline and the model inference on GPU or CPU.
        private const val RUN_ON_GPU = true
    }
}