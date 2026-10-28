package com.rpay.v2.core.utils.presentation


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.rpay.v2.R
import pl.droidsonroids.gif.GifImageView

object GifDialogHelper {

    fun showSuccessDialog(context: Context, message: String = "Game triggered successfully!") {
        showGifDialog(context, R.drawable.success, message, true)
    }

    fun showFailureDialog(context: Context, message: String = "Failed to trigger game") {
        showGifDialog(context, R.drawable.failure, message, false)
    }

    private fun showGifDialog(context: Context, gifResource: Int, message: String, isSuccess: Boolean) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        // Make dialog background transparent
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Inflate custom layout
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_gif_result, null)

        // Set up views
        val gifImageView = dialogView.findViewById<GifImageView>(R.id.gifImageView)
        val messageTextView = dialogView.findViewById<TextView>(R.id.messageTextView)
        val cardView = dialogView.findViewById<CardView>(R.id.cardView)

        // Set GIF
        gifImageView.setImageResource(gifResource)

        // Set message
        messageTextView.text = message

        // Remove card background color setting - GIF already provides visual feedback
        // cardView.setCardBackgroundColor() removed

        // Set dialog content
        dialog.setContentView(dialogView)

        // Show dialog
        dialog.show()

        // Auto-dismiss after 3 seconds
        dialogView.postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 3000)
    }
}