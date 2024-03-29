package com.example.memorygame

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.databinding.ActivityMainBinding
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryGame
import com.example.memorygame.utils.EXTRA_BOARD_SIZE
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG="MainActivity"
        private const val CREATE_REQUEST_CODE =248
    }
    lateinit var playIB: ImageButton
    lateinit var pauseIB: ImageButton
    private lateinit var timertext: TextView
    //lateinit var mediaPlayer: MediaPlayer
    var mMediaPlayer: MediaPlayer? = null
    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvHamleS: TextView
    private lateinit var tvEsS: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var  binding: ActivityMainBinding
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var boardSize: BoardSize= BoardSize.EASY
    private lateinit var timer: CountDownTimer




    @SuppressLint("SuspiciousIndentation", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playSound()
        clRoot=findViewById(R.id.clRoot)
        rvBoard= findViewById(R.id.rvBoard)
        tvHamleS= findViewById(R.id.tvHamleS)
        tvEsS=findViewById(R.id.tvEsS)
        playIB=findViewById((R.id.btn_sound))
        pauseIB=findViewById(R.id.btn_pause)
        timertext=findViewById(R.id.tw)
        playIB.setOnClickListener{
            playSound()

            /*val intent =Intent(this,LoginActivity::class.java)
            startActivity(intent)*/
        }
        pauseIB.setOnClickListener{
            stopSound()

        }
        timer= object : CountDownTimer(4_5000,1) {
            override fun onTick(remaining: Long) {

                timertext.text=(remaining/1000).toString()
            }

            override fun onFinish() {
                timertext.text="Bitti"
                setupBoard()
                stopSound()
                playSound4()

               // timer.start()
            }
        }


        /*val intent =Intent(this,LoginActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE,BoardSize.EASY)
        startActivity(intent)*/

        setupBoard()
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.refresh ->{
                timer.start()
                stopSound()
                playSound()
                if(memoryGame.getNumMoves()>0 && !memoryGame.haveWonGame()){
                    timer.cancel()
                    showAlertDialog("Oyun Yeniden Başlatılacak. Onaylıyor musunuz?",null,View.OnClickListener {
                        setupBoard()
                        timer.start()
                    })
                }else {
                    setupBoard()

                }
                return true
            }
            R.id.start ->{
                timer.start()
            }
            R.id.zorlukS->{
                showNewSizeDialog()
                return true
            }
            R.id.custom ->{
                showCreationDialog()
                return true
            }
            R.id.signin ->{
                val intent =Intent(this,LoginActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.signout ->{
                firebaseAuth= Firebase.auth
                firebaseAuth.signOut()
                val intent =Intent(this,LoginActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize= boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        showAlertDialog("Kendi oyununuzu oluşturun",boardSizeView,View.OnClickListener {
            val desiredBoardSize =when (radioGroupSize.checkedRadioButtonId){
                R.id.rbkolay -> BoardSize.EASY
                R.id.rborta -> BoardSize.MEDIUM
                else-> BoardSize.HARD
            }
            //navigate islemi
            val intent = Intent(this,CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize= boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbkolay)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rborta)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbzor)
        }
        showAlertDialog("Zorluk Seviyesini Seciniz",boardSizeView,View.OnClickListener {
            boardSize =when (radioGroupSize.checkedRadioButtonId){
                R.id.rbkolay -> BoardSize.EASY
                R.id.rborta -> BoardSize.MEDIUM
                else-> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?,positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Vazgeç",null)
            .setPositiveButton("Tamam"){ _,_->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun setupBoard() {
        when(boardSize){
            BoardSize.EASY -> {
                tvHamleS.text = "Kolay: 2x2"
                tvEsS.text = "Eş Kartlar: 0/4"
            }
            BoardSize.MEDIUM -> {
                tvHamleS.text = "Orta: 4x4"
                tvEsS.text = "Eş Kartlar: 0/16"
            }
            BoardSize.HARD -> {
                tvHamleS.text = "Zor: 6x6"
                tvEsS.text = "Eş Kartlar: 0/36"
            }
        }
        tvEsS.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
        memoryGame= MemoryGame(boardSize)
        adapter= MemoryBoardAdapter(this,boardSize, memoryGame.cards, object :MemoryBoardAdapter.CardClickedListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        rvBoard.adapter=adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager= GridLayoutManager(this,boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {
        if(memoryGame.haveWonGame()){
            Snackbar.make(clRoot,"Zaten Kazandınız!",Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            Snackbar.make(clRoot,"Geçersiz Hamle!",Snackbar.LENGTH_SHORT).show()
            return
        }
        if(memoryGame.flipCard(position)){
            stopSound()
            playSound3()
            Log.i(TAG,"Eşleşme Doğru! Eşleşen Kart Sayısı: ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full),
                ) as Int
            tvEsS.setTextColor(color)
            tvEsS.text= "Eşleşen Kart Sayısı: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"

            if( memoryGame.haveWonGame()){
                Snackbar.make(clRoot,"Tebrikler! Kazandınız..",Snackbar.LENGTH_LONG).show()
                timer.cancel()
                stopSound()
                playSound2()


            }
        }
        tvHamleS.text = "Hamle Sayısı: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
    // 1. Plays the water sound
    fun playSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.prologue)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }
    fun playSound2() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.congratulations)
            mMediaPlayer!!.isLooping = false
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }
    fun playSound3() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.wheels)
            mMediaPlayer!!.isLooping = false
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }
    fun playSound4() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.finish)
            mMediaPlayer!!.isLooping = false
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    // 2. Pause playback
    fun pauseSound() {
        if (mMediaPlayer?.isPlaying == true) mMediaPlayer?.pause()
    }

    // 3. Stops playback
    fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    override fun onStart() {
        super.onStart()
            /*val intent =Intent(this,LoginActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,BoardSize.EASY)
            startActivity(intent)
            finish()*/

        timertext.text="Oyunu Başlat!"
    }

    private fun reload() {

    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
    }


}