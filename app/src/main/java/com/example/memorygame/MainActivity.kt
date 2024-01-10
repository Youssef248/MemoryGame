package com.example.memorygame

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
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
import java.util.Locale


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



        setupBoard()
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.mi_refresh -> {
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog(getString(R.string.quit_game), null, View.OnClickListener {
                        setupBoard()
                    })
                }else{
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom -> {
                showCreationDialog()
                return true
            }

            R.id.mi_download -> {
                showDownloadDialog()
                return true
            }
            R.id.mi_change_language -> {
                switchLanguage()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun switchLanguage() {
        val currentLanguage = Locale.getDefault().language

        // Toggle between English and Dutch
        val newLanguage = if (currentLanguage == "en") "nl" else "en"

        // Set the new language and recreate the activity
        LocaleHelper.setLocale(this, newLanguage)
        recreate()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if (customGameName == null){
                Log.e(TAG, "Got null custom game from CreateActivity")
                return
            }
            downloadGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun showDownloadDialog() {
        val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board, null)
        showAlertDialog(getString(R.string.fetch), boardDownloadView, View.OnClickListener {
            val etDownloadGame = boardDownloadView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload = etDownloadGame.text.toString().trim()
            downloadGame(gameToDownload)
        })
    }

    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            val userImageList = document.toObject(UserImageList::class.java)
            if(userImageList?.images == null){
                Log.e(TAG, "Invalid custom game data from Firestore")
                Snackbar.make(clRoot, getString(R.string.sorry, gameName), Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val numCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            customGameImages = userImageList.images
            for(imageUrl in userImageList.images){
                Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot, getString(R.string.now, customGameName), Snackbar.LENGTH_LONG).show()
            gameName = customGameName
            setupBoard()
        }.addOnFailureListener{exception->
            Log.e(TAG, "Exception when retrieving game", exception)
        }
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog(getString(R.string.create_memory_board), boardSizeView, View.OnClickListener {
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
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog(getString(R.string.choose_new_size), boardSizeView, View.OnClickListener {
            boardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName = null
            customGameImages = null
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton("OK"){_, _ ->
                positiveClickListener.onClick(null)
            }
            .create()
            .show()

    }

    private fun setupBoard() {
        toolbar.title = gameName ?: getString(R.string.app_name)
        when(boardSize){
            BoardSize.EASY -> {
                tvNumMoves.text = getString(R.string.easy)
                tvNumPairs.text = getString(R.string.easy_pairs)
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = getString(R.string.medium)
                tvNumPairs.text = getString(R.string.medium_pairs)
            }
            BoardSize.HARD -> {
                tvNumMoves.text = getString(R.string.hard)
                tvNumPairs.text = getString(R.string.hard_pairs)
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize, customGameImages)

        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicker(position: Int) {
                updateGameWithFlip(position)
            }
        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {
        //error checking
        if(memoryGame.haveWonGame()){
            Snackbar.make(clRoot, getString(R.string.you_already_won), Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            Snackbar.make(clRoot, getString(R.string.invalid_move), Snackbar.LENGTH_SHORT).show()
            return
        }
        //actually flipping the card

        if(memoryGame.flipCard(position)){
            Log.i(TAG, "Found a macth! Num pairs found: ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            )as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "${R.string.pairs}: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if(memoryGame.haveWonGame()){
                Snackbar.make(clRoot, R.string.you_won, Snackbar.LENGTH_LONG).show()
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame. getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}