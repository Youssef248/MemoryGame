package com.example.memorygame

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import kotlin.math.min

// Define the ImagePickerAdapter class, extending RecyclerView.Adapter.
class ImagePickerAdapter(
    private val context: Context,
    private val imageUris: List<Uri>,
    private val boardSize: BoardSize,
    private val imageClickListener: ImageClickListener
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    // Define an interface for the ImageClickListener.
    interface ImageClickListener {
        fun onPLaceholderClicked()
    }

    // Override the onCreateViewHolder method to create and return a ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for the card_image and set its dimensions based on boardSize.
        val view = LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        val cardWidth = parent.width / boardSize.getWidth()
        val cardHeight = parent.height / boardSize.getHeight()
        val cardSideLength = min(cardWidth, cardHeight)
        val layoutParams = view.findViewById<ImageView>(R.id.ivCustomImage).layoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        return ViewHolder(view)
    }

    // Override the getItemCount method to specify the number of items in the adapter.
    override fun getItemCount() = boardSize.getNumPairs()

    // Override the onBindViewHolder method to bind data to ViewHolder at a given position.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < imageUris.size) {
            // Bind an image URI to the ViewHolder if it exists in the list.
            holder.bind(imageUris[position])
        } else {
            // Bind a placeholder if the position exceeds available image URIs.
            holder.bind()
        }
    }

    // Define the ViewHolder class that represents each item in the RecyclerView.
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Reference to the ImageView inside the card_image layout.
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage)

        // Function to bind an image URI to the ViewHolder.
        fun bind(uri: Uri) {
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)
        }

        // Function to bind a placeholder and set the click listener.
        fun bind() {
            ivCustomImage.setOnClickListener {
                imageClickListener.onPLaceholderClicked()
            }
        }
    }
}
