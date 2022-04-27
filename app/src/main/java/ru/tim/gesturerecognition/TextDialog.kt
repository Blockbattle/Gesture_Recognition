package ru.tim.gesturerecognition

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.tim.gesturerecognition.Camera.Companion.mode
import ru.tim.gesturerecognition.WordGenerator.Companion.text

/**
 * Класс для создания диалогового окна.
 */
class TextDialog {
    companion object {
        /**
         * Создаёт диалоговое окно с текстом сообщения, которое хочет донести пользователь собеседнику.
         * @param activity активность в которой будет показано диалоговое окно.
         */
        fun showDialog(activity: AppCompatActivity) {
            val dialog = Dialog(activity)
            dialog.setContentView(R.layout.dialog)

            val speech: TextView = dialog.findViewById(R.id.textDialog)
            val buttonOk: Button = dialog.findViewById(R.id.buttonOk)

            speech.text = text

            buttonOk.setOnClickListener {
                if (mode == Camera.Mode.NONE_C) {
                    mode = Camera.Mode.MIC
                    val intent = Intent(activity, Microphone::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
                else if (mode == Camera.Mode.NONE_M) {
                    mode = Camera.Mode.CAMERA
                    val intent = Intent(activity, Camera::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
                dialog.dismiss()
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            dialog.show()
        }
    }
}