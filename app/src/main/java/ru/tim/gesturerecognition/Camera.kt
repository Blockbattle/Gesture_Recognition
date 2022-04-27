package ru.tim.gesturerecognition

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.tim.gesturerecognition.GestureRecognition.Companion.getLetter
import ru.tim.gesturerecognition.WordGenerator.Companion.letters
import ru.tim.gesturerecognition.WordGenerator.Companion.text
import ru.tim.gesturerecognition.WordGenerator.Companion.wordGeneration
import ru.tim.gesturerecognition.WordGenerator.Companion.wordProcessing

 /**
  * Активность для работы с камерой.
  */
 class Camera : AppCompatActivity() {
    private var k = 0
    private var hands: Hands? = null

    private enum class InputSource {
        UNKNOWN, CAMERA
    }

    enum class Mode {
        MIC, CAMERA, NONE_M, NONE_C
    }

    private var inputSource = InputSource.UNKNOWN

    private var cameraInput: CameraInput? = null
    private var glSurfaceView: SolutionGlSurfaceView<HandsResult>? = null
    private var cameraFacing = CameraInput.CameraFacing.BACK

     /**
      * Создаёт активность обрабатывает нажатия кнопок.
      * @param savedInstanceState сохранённое состояние активности
      */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera)

        val linearLayout = findViewById<LinearLayout>(R.id.layout)
        linearLayout.setOnClickListener {
            val intent = Intent(this, TextEdit::class.java)
            startActivity(intent)
        }

        val checkText = findViewById<FloatingActionButton>(R.id.checkText)
        checkText.setOnClickListener {
            GlobalScope.launch() {
                text = wordProcessing()

                Handler(Looper.getMainLooper()).post {
                    val textView: TextView = findViewById(R.id.textView)
                    textView.text = text
                }
            }
        }

        val switchMode = findViewById<FloatingActionButton>(R.id.switchMode)
        switchMode.setOnClickListener {
            mode = Mode.NONE_C

            GlobalScope.launch() {
                text = wordProcessing()

                Handler(Looper.getMainLooper()).post {
                    val textView: TextView = findViewById(R.id.textView)
                    textView.text = ""
                    TextDialog.showDialog(this@Camera)
                    text = ""
                }
            }
        }

        setupLiveDemoUiComponents()
    }

    override fun onResume() {
        super.onResume()

        val textView = findViewById<TextView>(R.id.textView)
        textView.text = text

        if (inputSource == InputSource.CAMERA) {
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

    /**
     * Настраивает компоненты пользовательского интерфейса для демонстрации
     * в реальном времени с помощью ввода с камеры.
     * */
    private fun setupLiveDemoUiComponents() {
        val switchButton = findViewById<FloatingActionButton>(R.id.switchCamera)
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

     /**
      * Настраивает основной рабочий процесс для режима потоковой передачи.
      * @param inputSource
      */
    private fun setupStreamingModePipeline(inputSource: InputSource) {
        this.inputSource = inputSource
        // Инициализирует новый экземпляр решения MediaPipe Hands в потоковом режиме.
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

        glSurfaceView = SolutionGlSurfaceView(this, hands!!.glContext, hands!!.glMajorVersion)
        glSurfaceView!!.setSolutionResultRenderer(HandsResultGlRenderer())
        glSurfaceView!!.setRenderInputImage(true)
        hands!!.setResultListener { handsResult: HandsResult ->
            recognizeGesture(handsResult)
            glSurfaceView!!.setRenderData(handsResult)
            glSurfaceView!!.requestRender()
        }


        // Запуск камеры после подключения GLSurfaceView.
        if (inputSource == InputSource.CAMERA) {
            glSurfaceView!!.post { startCamera() }
        }

        // Обновляем layout предварительного просмотра.
        val constraintLayout = findViewById<ConstraintLayout>(R.id.preview_display_layout)
        //constraintLayout.removeAllViewsInLayout();
        constraintLayout.addView(glSurfaceView)
        glSurfaceView!!.visibility = View.VISIBLE
        constraintLayout.requestLayout()
    }

     /**
      * Запускает камеру.
      */
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

     /**
      * Отправляет координаты скелета кисти на распознавание.
      * Создаёт дополнительный поток.
      * @param result результат распознавания руки (координаты скелета)
      */
    private fun recognizeGesture(result: HandsResult) {
        if (result.multiHandLandmarks().isEmpty() || mode == Mode.MIC || mode == Mode.NONE_M || mode == Mode.NONE_C) {
            return
        }

        logWristLandmark(result)

        Handler(Looper.getMainLooper()).post {
            //отсеиваем половину кадров так как сервер не справляется с 13-ю кадрами
            k = (k + 1) % 2
            if (k == 0) {
                GlobalScope.launch() {
                    val res = getLetter(result.multiHandLandmarks()[0].landmarkList)
                    if (res != -1) {
                        Handler(Looper.getMainLooper()).post {
                            val textView: TextView = findViewById(R.id.textView)
                            if (LETTERS[res] != "nothing")
                                letters.add(LETTERS[res])
                            wordGeneration()

                            textView.text = text
                        }
                    }
                }
            }
        }
    }

     /**
      * Выводит информацию о распознанном скелете руки в логи.
      * @param result результат распознавания руки (координаты скелета)
      */
    private fun logWristLandmark(result: HandsResult) {
        val wristLandmark = result.multiHandLandmarks()[0].landmarkList[HandLandmark.WRIST]
        Log.i(
                TAG, String.format(
                "MediaPipe Hand wrist normalized coordinates (value range: [0, 1]): x=%f, y=%f",
                wristLandmark.x, wristLandmark.y)
        )

        if (result.multiHandWorldLandmarks().isEmpty()) {
            return
        }
        val wristWorldLandmark = result.multiHandWorldLandmarks()[0].landmarkList[HandLandmark.WRIST]
        Log.i(
                TAG, String.format("MediaPipe Hand wrist world coordinates (in meters with the origin at the hand's"
                + " approximate geometric center): x=%f m, y=%f m, z=%f m",
                wristWorldLandmark.x, wristWorldLandmark.y, wristWorldLandmark.z)
        )
    }

    companion object {
        var mode = Mode.CAMERA

        private const val TAG = "MainActivity"
        private val LETTERS = "a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z/nothing/ ".split("/")

        // Запуск с использованием графического процессора.
        private const val RUN_ON_GPU = false
    }
}