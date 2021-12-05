package ru.tim.gesturerecognition

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
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
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.max


/** Main activity of MediaPipe Hands app.  */
class MainActivity : AppCompatActivity() {
    private var k: Int = 0;

    private lateinit var tflite : Interpreter
    private lateinit var tflitemodel : ByteBuffer

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

        try{
            tflitemodel = loadModelFile(this.assets, "model.tflite")
            tflite = Interpreter(tflitemodel)
        } catch (ex: Exception){
            ex.printStackTrace()
        }

        setupLiveDemoUiComponents()
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
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

        var resBitmap: Bitmap? = null
        resBitmap = drawSkeleton(result)

        //val stream = ByteArrayOutputStream()
        //resBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        //val byteArray: ByteArray = stream.toByteArray()
        val input: ByteBuffer = getScaledMatrix(resBitmap, intArrayOf(224, 224, 3))

        try {
            // Data format conversion takes too long
            // Log.d("inputData", Arrays.toString(inputData));
            val labelProbArray = Array(1) { FloatArray(28) }
            val start = System.currentTimeMillis()
            // get predict result
            tflite.run(input, labelProbArray)
            val end = System.currentTimeMillis()
            val time = end - start
            val results = FloatArray(labelProbArray[0].size)
            System.arraycopy(labelProbArray[0], 0, results, 0, labelProbArray[0].size)
            // show predict result and time
            val r: Int = getMaxResult(results)
            //name：${resultLabel.get(r)}
            val showText = """
                 result：$r
                 probability：${results[r]}
                 time：${time}ms
                 """.trimIndent()
            Log.i("AAAAAAA", showText)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        //drawSkeleton(result)
            //val imageView: ImageView = findViewById(R.id.imageView);
            //stopCurrentPipeline()
        /*var resBitmap: Bitmap? = null
        if(++k % 14 == 0) {
            resBitmap = drawSkeleton(result)
        }
        Handler(Looper.getMainLooper()).post {
            if(k % 14 == 0) {
                val imageView: ImageView = findViewById(R.id.imageView)
                val bitmap: Bitmap = resBitmap!!
                //bitmap.eraseColor(Color.BLUE)
                imageView.setImageBitmap(bitmap)
            }
        }*/
            //bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            //bitmap!!.eraseColor(Color.BLUE)
            //imageView.setImageBitmap(bitmap)

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

    // TensorFlow model，get predict data
    private fun getScaledMatrix(bitmap: Bitmap, ddims: IntArray): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(ddims[0] * ddims[1] * ddims[2] * 4)
        imgData.order(ByteOrder.nativeOrder())
        // get image pixel
        val pixels = IntArray(ddims[0] * ddims[1])
        val bm = Bitmap.createScaledBitmap(bitmap, ddims[0], ddims[1], false)
        bm.getPixels(pixels, 0, bm.width, 0, 0, ddims[0], ddims[1])
        var pixel = 0
        for (i in 0 until ddims[0]) {
            for (j in 0 until ddims[1]) {
                val `val` = pixels[pixel++]
                imgData.putFloat(((`val` shr 16 and 0xFF) - 128f) / 128f)
                imgData.putFloat(((`val` shr 8 and 0xFF) - 128f) / 128f)
                imgData.putFloat(((`val` and 0xFF) - 128f) / 128f)
            }
        }
        if (bm.isRecycled) {
            bm.recycle()
        }
        return imgData
    }

    // get max probability label
    private fun getMaxResult(result: FloatArray): Int {
        var probability = result[0]
        var r = 0
        for (i in result.indices) {
            if (probability < result[i]) {
                probability = result[i]
                r = i
            }
        }
        return r
    }

    companion object {
        private const val TAG = "MainActivity"

        // Run the pipeline and the model inference on GPU or CPU.
        private const val RUN_ON_GPU = true
    }
}