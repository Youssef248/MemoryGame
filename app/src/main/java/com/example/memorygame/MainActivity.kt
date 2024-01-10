package com.example.memorygame

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryGame
import com.example.memorygame.models.UserImageList
import com.example.memorygame.utils.EXTRA_BOARD_SIZE
import com.example.memorygame.utils.EXTRA_GAME_NAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 194
    }

    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var toolbar: Toolbar

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages: List<String>? = null
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var boardSize: BoardSize = BoardSize.EASY

    private lateinit var someActivityResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Check if the user is already signed in
        if (auth.currentUser == null) {
            // If not, sign in anonymously or with a predefined user account
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d(TAG, "signInAnonymously:success")
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                    }
                }
        }

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)



        // Initialize the result launcher
        someActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle the result from the launched activity
                    val data: Intent? = result.data
                    // Process the result as needed
                }
            }


        // Set up the game board
        setupBoard()
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu layout
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.mi_refresh -> {
                // Check if there are moves left or if the game is won
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    // Display a confirmation dialog to quit the current game
                    showAlertDialog("Quit your current game?", null, View.OnClickListener {
                        // Set up a new game board if the user chooses to quit
                        setupBoard()
                    })
                }else{
                    // Set up a new game board if there are no moves left or if the game is won
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size -> {
                // Display a dialog to choose a new game size
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom -> {
                // Display a dialog to create a custom game
                showCreationDialog()
                return true
            }

            R.id.mi_download -> {
                // Display a dialog to download a game
                showDownloadDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Handle the result of an activity launched for result (e.g., CreateActivity)
        if (resultCode == Activity.RESULT_OK) {
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if (customGameName == null){
                Log.e(TAG, "Got null custom game from CreateActivity")
                return
            }
            // Download the selected custom game
            downloadGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun showDownloadDialog() {
        // Display a dialog for downloading a game
        val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board, null)
        showAlertDialog("Fetch memory game", boardDownloadView, View.OnClickListener {
            // Retrieve the game name entered by the user
            val etDownloadGame = boardDownloadView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload = etDownloadGame.text.toString().trim()
            // Download the specified game
            downloadGame(gameToDownload)
        })
    }

    private fun downloadGame(customGameName: String) {
        // Retrieve a custom game from Firestore
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            val userImageList = document.toObject(UserImageList::class.java)
            if (userImageList?.images == null) {
                // Log an error if the custom game data is invalid
                Log.e(TAG, "Invalid custom game data from Firestore")
                // Show a Snackbar message indicating the failure
                Snackbar.make(clRoot, "Sorry, we couldn't find any such game, '$gameName'", Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            // Calculate the number of cards based on the number of images
            val numCards = userImageList.images.size * 2
            // Set the board size based on the number of cards
            boardSize = BoardSize.getByValue(numCards)
            customGameImages = userImageList.images
            // Fetch images using Picasso
            for (imageUrl in userImageList.images) {
                Picasso.get().load(imageUrl).fetch()
            }
            // Show a Snackbar message indicating the success
            Snackbar.make(clRoot, "You're now playing '$customGameName'!", Snackbar.LENGTH_LONG).show()
            gameName = customGameName
            // Set up the game board with the downloaded custom game
            setupBoard()
        }.addOnFailureListener { exception ->
            // Log an error if the game retrieval fails
            Log.e(TAG, "Exception when retrieving game", exception)
        }
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Create your own memory board", boardSizeView, View.OnClickListener {
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // Navigate to a new activity using registerForActivityResult
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            someActivityResultLauncher.launch(intent)
        })
    }

    private fun showNewSizeDialog() {
        // Display a dialog for choosing a new game size
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        // Check the radio button corresponding to the current game size
        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            // Retrieve the new board size from the selected radio button
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // Reset game name and custom images as the board size changed
            gameName = null
            customGameImages = null
            // Set up the game board with the new size
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        // Display a generic alert dialog with a title, custom view, and positive click listener
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                // Execute the positive click listener
                positiveClickListener.onClick(null)
            }
            .create()
            .show()
    }

    private fun setupBoard() {
        // Set up the game board based on the selected size and custom images
        toolbar.title = gameName ?: getString(R.string.app_name)
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy: 4 x 2"
                tvNumPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Medium: 6 x 3"
                tvNumPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Hard: 6 x 4"
                tvNumPairs.text = "Pairs: 0 / 12"
            }
        }
        // Set text color for the number of pairs
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        // Initialize the MemoryGame instance
        memoryGame = MemoryGame(boardSize, customGameImages)
        // Initialize the MemoryBoardAdapter
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object : MemoryBoardAdapter.CardClickListener {
            override fun onCardClicker(position: Int) {
                // Handle the click event on a card
                updateGameWithFlip(position)
            }
        })
        // Set up the RecyclerView
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {
        // Error checking
        if (memoryGame.haveWonGame()) {
            // Alert user of an invalid move if the game is already won
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            // Alert user of an invalid move if the card is already face up
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show()
            return
        }
        // Actually flip the card and check for a match
        if (memoryGame.flipCard(position)) {
            // Log information about the match
            Log.i(TAG, "Found a match! Num pairs found: ${memoryGame.numPairsFound}")
            // Calculate progress color based on the number of pairs found
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int
            // Update the number of pairs text and color
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            // Check if the game is won and show a Snackbar message
            if (memoryGame.haveWonGame()) {
                Snackbar.make(clRoot, "You have won! Congratulations.", Snackbar.LENGTH_LONG).show()
            }
        }
        // Update the number of moves text
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        // Notify the adapter of the data set changes
        adapter.notifyDataSetChanged()
    }
}