package com.example.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.createChooser
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    var drawingView: drawingView? = null
    var ibPen : ImageButton? = null
    var mColorChosen : ImageButton? = null
    var imageSelector : ImageButton? = null
    var undoButton : ImageButton? = null
    var saveBTN : ImageButton? = null
    var progressBar : Dialog? = null
    var returnBitmap : Bitmap? = null
    var result : String? = null

    var openGalleryLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK && result.data!=null){
            val ivBackground : ImageView = findViewById(R.id.iv_background)
            ivBackground.setImageURI(result.data?.data)
        }
    }

    var mresultActivityResultLauncher : ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted){
                Toast.makeText(this, "thank you", Toast.LENGTH_SHORT).show()
                val openGalIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(openGalIntent)
            }
            else{
                if (permissionName==Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this, "is required for further process", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        ibPen = findViewById(R.id.ib_pen)
        ibPen?.setOnClickListener {
            seeBrushSizeSetDialog()
        }

        val linearLayout = findViewById<LinearLayout>(R.id.ll_color_palate)
        mColorChosen = linearLayout[3] as ImageButton

        undoButton = findViewById(R.id.ib_undo)
        undoButton!!.setOnClickListener {
            drawingView?.UndoFunctionality()
        }

        imageSelector = findViewById(R.id.ib_image_selector)
        imageSelector!!.setOnClickListener {
            requestStoragePermission()
        }

        saveBTN = findViewById(R.id.ib_save)
        saveBTN!!.setOnClickListener {
            progressBarBuild()

            if(isReadStoragePermissionGiven()){
                lifecycleScope.launch {
                    val imageView : FrameLayout = findViewById(R.id.frame_layout)
                    saveImage(getBitmapFromView(imageView))
                }
            }
        }
    }

    private fun isReadStoragePermissionGiven():Boolean{
        val result = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show()
        } else {
            mresultActivityResultLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    fun seeBrushSizeSetDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.brush_size)
        brushDialog.setTitle("brush size:")
        brushDialog.show()
        val SmallBtn:ImageButton = brushDialog.findViewById(R.id.ib_small)
        SmallBtn.setOnClickListener {
            drawingView?.setStrokeBrushSize(10.toFloat())
            brushDialog.dismiss()
        }
        val MediumBtn:ImageButton = brushDialog.findViewById(R.id.ib_medium)
        MediumBtn.setOnClickListener {
            drawingView?.setStrokeBrushSize(20.toFloat())
            brushDialog.dismiss()
        }
        val LargeBtn:ImageButton = brushDialog.findViewById(R.id.ib_large)
        LargeBtn.setOnClickListener {
            drawingView?.setStrokeBrushSize(30.toFloat())
            brushDialog.dismiss()
        }
    }

    private fun getBitmapFromView(view:View):Bitmap{
        returnBitmap = createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnBitmap!!)

        val bgDrawable = view.background
        if (bgDrawable!=null){
            bgDrawable.draw(canvas)
        }
        else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnBitmap!!
    }

    private suspend fun saveImage(mBitmap:Bitmap?):String{

        withContext(Dispatchers.IO){
            if (mBitmap!=null){
                try{
                    val ByteArray = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,ByteArray)
                    val f = File(externalCacheDir?.absoluteFile.toString() + File.separator + "com.example.drawingapp" + System.currentTimeMillis()/1000
                            + "png")
                    val fo = FileOutputStream(f)
                    fo.write(ByteArray.toByteArray())
                    fo.close()

                    result = f.absolutePath
                    shareSomething(result)
                    runOnUiThread {
                        progressBarEnd()
                        if (result!!.isNotEmpty()){
                            Toast.makeText(this@MainActivity, result,Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@MainActivity, "smthg went wrong", Toast.LENGTH_SHORT).show()
                        }
                    }
                }catch (e:Exception){
                    result=""
                    e.printStackTrace()
                }
            }
        }
        return result!!
    }

    fun Clickcolor(view: View){

        if(view!==mColorChosen){
            val ImageButton = view as ImageButton
            val colorString = ImageButton.tag.toString()
            drawingView?.coloor(colorString)

            ImageButton.setImageDrawable(ContextCompat.getDrawable(
                this,
                R.drawable.selected_color_palate_layout
            ))

            mColorChosen!!.setImageDrawable(ContextCompat.getDrawable(
                this,
                R.drawable.color_palate_layout
            ))

            mColorChosen = view
        }
    }

    fun progressBarBuild(){
        progressBar = Dialog(this)
        progressBar!!.setContentView(R.layout.loading)
        progressBar!!.show()
    }

    fun progressBarEnd(){
        if(progressBar!=null){
            progressBar!!.dismiss()
        }
    }

    fun shareSomething(result:String?){
        MediaScannerConnection.scanFile(this, arrayOf(result),null){
                _, URI ->
            val shareIntent = Intent()
            shareIntent.action = ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,URI)
            shareIntent.type = "image/*"
            startActivity(createChooser(shareIntent,"SHARE"))
        }
    }

}