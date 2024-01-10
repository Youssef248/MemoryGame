package com.example.memorygame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import android.util.Log;
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryCard
import com.squareup.picasso.Picasso
import kotlin.math.min

// Define the MemoryBoardAdapter class, extending RecyclerView.Adapter.
class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener: CardClickListener
) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    // Companion object with constants for margin size and a log tag.
    companion object {
        private const val MARGIN_SIZE = 10
        const val TAG = "MemoryBoardAdapter"
    }

    // Interface for the CardClickListener.
    interface CardClickListener {
        fun onCardClicker(position: Int)
    }

    // Override the onCreateViewHolder method to create and return a ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Calculate card dimensions based on boardSize and apply layout parameters.
        val cardWidth = parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength = min(cardWidth, cardHeight)
        val view = LayoutInflater.from(context).inflate(R.layout.memory_card, parent, false)
        val layoutParams = view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    // Override the getItemCount method to specify the number of items in the adapter.
    override fun getItemCount() = boardSize.numCards

    // Override the onBindViewHolder method to bind data to ViewHolder at a given position.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Call the bind method in ViewHolder to set up the content for the item.
        holder.bind(position)
    }

    // Define the ViewHolder class that represents each item in the RecyclerView.
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Reference to the ImageButton inside the memory_card layout.
        private val imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)

        // Function to bind data to the ViewHolder at a given position.
        fun bind(position: Int) {
            // Retrieve the MemoryCard at the specified position.
            val memoryCard = cards[position]

            // Check if the card is face up and load the image or set the default background.
            if (memoryCard.isFaceUp) {
                if (memoryCard.imageUrl != null) {
                    Picasso.get().load(memoryCard.imageUrl).into(imageButton)
                } else {
                    imageButton.setImageResource(memoryCard.identifier)
                }
            } else {
                imageButton.setImageResource(R.drawable.ic_launcher_background)
            }

            // Adjust the alpha and background tint based on whether the card is matched.
            imageButton.alpha = if (memoryCard.isMatched) .4f else 1.0f
            val colorStateList = if (memoryCard.isMatched) ContextCompat.getColorStateList(context, R.color.color_gray) else null
            ViewCompat.setBackgroundTintList(imageButton, colorStateList)

            // Set a click listener for the ImageButton to handle card clicks.
            imageButton.setOnClickListener {
                Log.i(TAG, "Clicked on position $position")
                cardClickListener.onCardClicker(position)
            }
        }
    }
}
